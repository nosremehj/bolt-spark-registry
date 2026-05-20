package com.bolt.tecnhical.challenger.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.bolt.tecnhical.challenger.domain.Endereco;
import com.bolt.tecnhical.challenger.integration.viacep.ViaCepClient;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class ClienteControllerIT {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ViaCepClient viaCepClient;

	@BeforeEach
	void configurarViaCep() {
		when(viaCepClient.buscarEndereco(anyString(), any())).thenAnswer(invocation -> {
			String cep = invocation.getArgument(0, String.class).replaceAll("\\D", "");
			Endereco endereco = new Endereco();
			endereco.setCep(cep);
			endereco.setLogradouro("Rua Teste");
			endereco.setBairro("Centro");
			endereco.setCidade("Belo Horizonte");
			endereco.setUf("MG");
			endereco.setComplemento(invocation.getArgument(1));
			return endereco;
		});
	}

	@Test
	void deveCadastrarBuscarListarERemoverCliente() throws Exception {
		String payload = """
				{
				  "nome": "Maria Souza",
				  "documento": "529.982.247-25",
				  "endereco": { "cep": "30130010", "complemento": "Apto 1" },
				  "unidadesConsumidoras": [
				    {
				      "nome": "Minha casa",
				      "numeroInstalacao": "INST-1001",
				      "endereco": { "cep": "30130010" }
				    }
				  ]
				}
				""";

		String location = mockMvc.perform(post("/api/clientes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.documento").value("52998224725"))
				.andExpect(jsonPath("$.endereco.uf").value("MG"))
				.andExpect(jsonPath("$.endereco.complemento").value("Apto 1"))
				.andExpect(jsonPath("$.unidadesConsumidoras[0].endereco.complemento").value("Sem complemento"))
				.andReturn()
				.getResponse()
				.getContentAsString();

		Long id = objectMapper.readTree(location).get("id").asLong();

		mockMvc.perform(get("/api/clientes/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Maria Souza"));

		mockMvc.perform(get("/api/clientes"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(id))
				.andExpect(jsonPath("$[0].unidadesConsumidoras[0].endereco.complemento").value("Sem complemento"));

		mockMvc.perform(get("/api/clientes/recentes"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(id));

		String atualizacao = """
				{
				  "nome": "Maria Souza Atualizada",
				  "documento": "529.982.247-25",
				  "endereco": { "cep": "30130010" },
				  "unidadesConsumidoras": [
				    {
				      "nome": "Minha casa",
				      "numeroInstalacao": "INST-1001",
				      "endereco": { "cep": "30130010" }
				    }
				  ]
				}
				""";

		mockMvc.perform(put("/api/clientes/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(atualizacao))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Maria Souza Atualizada"));

		mockMvc.perform(delete("/api/clientes/{id}", id))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/clientes/{id}", id))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveRetornarErroQuandoCorpoNaoForEnviado() throws Exception {
		mockMvc.perform(post("/api/clientes")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("Bad Request"))
				.andExpect(jsonPath("$.message").value("Nenhum dado foi enviado no corpo da requisição"))
				.andExpect(jsonPath("$.details").isEmpty());
	}

	@Test
	void deveRetornarErroQuandoJsonForInvalido() throws Exception {
		mockMvc.perform(post("/api/clientes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{ nome: invalido }"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("Corpo da requisição inválido ou mal formatado"));
	}

}
