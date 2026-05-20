package com.bolt.tecnhical.challenger.integration.viacep;

import com.bolt.tecnhical.challenger.domain.Endereco;
import com.bolt.tecnhical.challenger.exception.BusinessException;
import com.bolt.tecnhical.challenger.util.CepUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class ViaCepClient {

	private static final String BASE_URL = "https://viacep.com.br/ws";

	private final RestClient restClient;

	public ViaCepClient() {
		this.restClient = RestClient.create();
	}

	public Endereco buscarEndereco(String cepInformado, String complementoInformado) {
		String cep = CepUtil.normalizar(cepInformado);
		ViaCepResponse response = consultarCep(cep);
		if (response == null || response.cepInvalido()) {
			throw new BusinessException("CEP não encontrado: " + cep, HttpStatus.BAD_REQUEST);
		}
		Endereco endereco = new Endereco();
		endereco.setCep(CepUtil.normalizar(response.cep()));
		endereco.setLogradouro(response.logradouro());
		endereco.setBairro(response.bairro());
		endereco.setCidade(response.cidade());
		endereco.setUf(response.uf());
		endereco.setComplemento(resolverComplemento(complementoInformado, response.complemento()));
		return endereco;
	}

	private ViaCepResponse consultarCep(String cep) {
		try {
			return restClient.get()
					.uri(BASE_URL + "/{cep}/json", cep)
					.retrieve()
					.body(ViaCepResponse.class);
		}
		catch (RestClientException ex) {
			throw new BusinessException("Falha ao consultar o ViaCEP", HttpStatus.BAD_GATEWAY);
		}
	}

	private String resolverComplemento(String informado, String doViaCep) {
		if (informado != null && !informado.isBlank()) {
			return informado.trim();
		}
		if (doViaCep != null && !doViaCep.isBlank()) {
			return doViaCep.trim();
		}
		return null;
	}

}
