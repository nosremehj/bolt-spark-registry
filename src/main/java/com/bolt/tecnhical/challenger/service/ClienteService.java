package com.bolt.tecnhical.challenger.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import com.bolt.tecnhical.challenger.domain.Cliente;
import com.bolt.tecnhical.challenger.domain.Endereco;
import com.bolt.tecnhical.challenger.domain.UnidadeConsumidora;
import com.bolt.tecnhical.challenger.exception.BusinessException;
import com.bolt.tecnhical.challenger.exception.ResourceNotFoundException;
import com.bolt.tecnhical.challenger.integration.viacep.ViaCepClient;
import com.bolt.tecnhical.challenger.messaging.AnaliseClienteMgNotifier;
import com.bolt.tecnhical.challenger.repository.ClienteRepository;
import com.bolt.tecnhical.challenger.repository.UnidadeConsumidoraRepository;
import com.bolt.tecnhical.challenger.util.DocumentoUtil;
import com.bolt.tecnhical.challenger.util.EstadoUtil;
import com.bolt.tecnhical.challenger.web.dto.ClienteRequest;
import com.bolt.tecnhical.challenger.web.dto.ClienteResponse;
import com.bolt.tecnhical.challenger.web.dto.EnderecoRequest;
import com.bolt.tecnhical.challenger.web.dto.UnidadeConsumidoraRequest;

@Service
public class ClienteService {

	private static final int LIMITE_ULTIMOS_CLIENTES = 20;

	private final ClienteRepository clienteRepository;
	private final UnidadeConsumidoraRepository unidadeConsumidoraRepository;
	private final ViaCepClient viaCepClient;
	private final EntityManager entityManager;
	private final AnaliseClienteMgNotifier analiseClienteMgNotifier;

	public ClienteService(
			ClienteRepository clienteRepository,
			UnidadeConsumidoraRepository unidadeConsumidoraRepository,
			ViaCepClient viaCepClient,
			EntityManager entityManager,
			AnaliseClienteMgNotifier analiseClienteMgNotifier) {
		this.clienteRepository = clienteRepository;
		this.unidadeConsumidoraRepository = unidadeConsumidoraRepository;
		this.viaCepClient = viaCepClient;
		this.entityManager = entityManager;
		this.analiseClienteMgNotifier = analiseClienteMgNotifier;
	}

	@Transactional
	public ClienteResponse cadastrar(ClienteRequest request) {
		String documento = DocumentoUtil.normalizar(request.documento());
		validarDocumentoUnico(documento, null);
		validarNumerosInstalacaoUnicos(request.unidadesConsumidoras(), null);

		Cliente cliente = new Cliente();
		cliente.setNome(request.nome().trim());
		cliente.setDocumento(documento);
		cliente.setEndereco(resolverEndereco(request.endereco()));
		adicionarUnidades(cliente, request.unidadesConsumidoras());

		Cliente salvo = clienteRepository.save(cliente);
		analiseClienteMgNotifier.notificarSeNecessario(salvo);
		return ClienteResponse.from(salvo);
	}

	@Transactional
	public ClienteResponse atualizar(Long id, ClienteRequest request) {
		Cliente cliente = buscarClienteAtivo(id);
		String documento = DocumentoUtil.normalizar(request.documento());
		validarDocumentoUnico(documento, id);
		validarNumerosInstalacaoUnicos(request.unidadesConsumidoras(), id);

		cliente.setNome(request.nome().trim());
		cliente.setDocumento(documento);
		cliente.setEndereco(resolverEndereco(request.endereco()));
		substituirUnidadesConsumidoras(cliente, request.unidadesConsumidoras());

		Cliente salvo = clienteRepository.save(cliente);
		analiseClienteMgNotifier.notificarSeNecessario(salvo);
		return ClienteResponse.from(salvo);
	}

	@Transactional
	public void remover(Long id) {
		Cliente cliente = buscarClienteAtivo(id);
		cliente.setAtivo(false);
		clienteRepository.save(cliente);
	}

	@Transactional(readOnly = true)
	public ClienteResponse buscarPorId(Long id) {
		return ClienteResponse.from(buscarClienteAtivoComUnidades(id));
	}

	@Transactional(readOnly = true)
	public List<ClienteResponse> listarTodos() {
		return clienteRepository.findAllAtivosWithUnidades().stream()
				.map(ClienteResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<ClienteResponse> listarUltimosCadastrados() {
		PageRequest page = PageRequest.of(0, LIMITE_ULTIMOS_CLIENTES);
		return clienteRepository.findUltimosAtivosWithUnidades(page).stream()
				.map(ClienteResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<ClienteResponse> listarInativos() {
		return clienteRepository.findAllInativosWithUnidades().stream()
				.map(ClienteResponse::from)
				.toList();
	}

	private Cliente buscarClienteAtivo(Long id) {
		return clienteRepository.findByIdAndAtivoTrue(id)
				.orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado: " + id));
	}

	private Cliente buscarClienteAtivoComUnidades(Long id) {
		return clienteRepository.findByIdAndAtivoTrueWithUnidades(id)
				.orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado: " + id));
	}

	private void validarDocumentoUnico(String documento, Long clienteId) {
		boolean duplicado = clienteId == null
				? clienteRepository.existsByDocumento(documento)
				: clienteRepository.existsByDocumentoAndIdNot(documento, clienteId);
		if (duplicado) {
			throw new BusinessException("Já existe cliente cadastrado com este documento", HttpStatus.CONFLICT);
		}
	}

	private void validarNumerosInstalacaoUnicos(List<UnidadeConsumidoraRequest> unidades, Long clienteId) {
		Set<String> numerosNaRequisicao = new HashSet<>();
		for (UnidadeConsumidoraRequest unidade : unidades) {
			String numero = unidade.numeroInstalacao().trim();
			if (!numerosNaRequisicao.add(numero)) {
				throw new BusinessException(
						"Número de instalação duplicado na requisição: " + numero,
						HttpStatus.BAD_REQUEST);
			}
			boolean emUso = clienteId == null
					? unidadeConsumidoraRepository.existsByNumeroInstalacao(numero)
					: unidadeConsumidoraRepository.existsByNumeroInstalacaoAndClienteIdNot(numero, clienteId);
			if (emUso) {
				throw new BusinessException(
						"Número de instalação já cadastrado para outro cliente: " + numero,
						HttpStatus.CONFLICT);
			}
		}
	}

	private void substituirUnidadesConsumidoras(Cliente cliente, List<UnidadeConsumidoraRequest> unidades) {
		cliente.getUnidadesConsumidoras().clear();
		entityManager.flush();
		adicionarUnidades(cliente, unidades);
	}

	private void adicionarUnidades(Cliente cliente, List<UnidadeConsumidoraRequest> unidades) {
		for (UnidadeConsumidoraRequest request : unidades) {
			Endereco endereco = resolverEndereco(request.endereco());
			EstadoUtil.validarUnidadeConsumidora(endereco.getUf());

			UnidadeConsumidora unidade = new UnidadeConsumidora();
			unidade.setNome(request.nome().trim());
			unidade.setNumeroInstalacao(request.numeroInstalacao().trim());
			unidade.setEndereco(endereco);
			cliente.adicionarUnidade(unidade);
		}
	}

	private Endereco resolverEndereco(EnderecoRequest request) {
		return viaCepClient.buscarEndereco(request.cep(), request.complemento());
	}

}
