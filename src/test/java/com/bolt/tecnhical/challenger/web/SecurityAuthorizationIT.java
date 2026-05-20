package com.bolt.tecnhical.challenger.web;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.bolt.tecnhical.challenger.domain.Endereco;
import com.bolt.tecnhical.challenger.integration.viacep.ViaCepClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.security.enabled=true")
class SecurityAuthorizationIT {

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
	void recursoProtegidoSemTokenRetorna401() throws Exception {
		mockMvc.perform(get("/api/clientes"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void loginComSenhaInvalidaRetorna401() throws Exception {
		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"admin\",\"password\":\"errada\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Usuário ou senha inválidos"));
	}

	@Test
	void adminPodeListarTodosClientes() throws Exception {
		String token = login("admin", "admin123");
		mockMvc.perform(get("/api/clientes")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());
	}

	@Test
	void clientePodeCadastrarBuscarAtualizarERemoverProprioRegistro() throws Exception {
		String token = login("cliente", "cliente123");
		long ts = System.currentTimeMillis();
		String body = """
				{
				  "nome": "Cliente API",
				  "documento": "39053344705",
				  "endereco": { "cep": "30130010" },
				  "unidadesConsumidoras": [
				    {
				      "nome": "UC",
				      "numeroInstalacao": "INST-S-%s",
				      "endereco": { "cep": "30130010" }
				    }
				  ]
				}
				""".formatted(ts);

		String json = mockMvc.perform(post("/api/clientes")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		long id = objectMapper.readTree(json).get("id").asLong();

		mockMvc.perform(get("/api/clientes/{id}", id)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id));

		String atualizar = """
				{
				  "nome": "Cliente API Atualizado",
				  "documento": "39053344705",
				  "endereco": { "cep": "30130010" },
				  "unidadesConsumidoras": [
				    {
				      "nome": "UC",
				      "numeroInstalacao": "INST-S-%s",
				      "endereco": { "cep": "30130010" }
				    }
				  ]
				}
				""".formatted(ts);
		mockMvc.perform(put("/api/clientes/{id}", id)
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(atualizar))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Cliente API Atualizado"));

		mockMvc.perform(delete("/api/clientes/{id}", id)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/clientes")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden());
	}

	@Test
	void clienteNaoPodeAlterarOuRemoverRegistroDeOutroUsuario() throws Exception {
		String adminToken = login("admin", "admin123");
		long ts = System.currentTimeMillis();
		String body = """
				{
				  "nome": "Apenas admin",
				  "documento": "08860050048",
				  "endereco": { "cep": "30130010" },
				  "unidadesConsumidoras": [
				    {
				      "nome": "UC",
				      "numeroInstalacao": "INST-X-%s",
				      "endereco": { "cep": "30130010" }
				    }
				  ]
				}
				""".formatted(ts);

		String json = mockMvc.perform(post("/api/clientes")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		long outroId = objectMapper.readTree(json).get("id").asLong();

		String atualizar = """
				{
				  "nome": "Tentativa",
				  "documento": "08860050048",
				  "endereco": { "cep": "30130010" },
				  "unidadesConsumidoras": [
				    {
				      "nome": "UC",
				      "numeroInstalacao": "INST-X-%s",
				      "endereco": { "cep": "30130010" }
				    }
				  ]
				}
				""".formatted(ts);
		String clienteToken = login("cliente", "cliente123");
		mockMvc.perform(put("/api/clientes/{id}", outroId)
						.header("Authorization", "Bearer " + clienteToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(atualizar))
				.andExpect(status().isForbidden());

		mockMvc.perform(delete("/api/clientes/{id}", outroId)
						.header("Authorization", "Bearer " + clienteToken))
				.andExpect(status().isForbidden());
	}

	@Test
	void clienteNaoPodeBuscarOutroCliente() throws Exception {
		String adminToken = login("admin", "admin123");
		long ts = System.currentTimeMillis();
		String body = """
				{
				  "nome": "Outro",
				  "documento": "11144477735",
				  "endereco": { "cep": "30130010" },
				  "unidadesConsumidoras": [
				    {
				      "nome": "UC",
				      "numeroInstalacao": "INST-O-%s",
				      "endereco": { "cep": "30130010" }
				    }
				  ]
				}
				""".formatted(ts);

		String json = mockMvc.perform(post("/api/clientes")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		long outroId = objectMapper.readTree(json).get("id").asLong();

		String clienteToken = login("cliente", "cliente123");
		mockMvc.perform(get("/api/clientes/{id}", outroId)
						.header("Authorization", "Bearer " + clienteToken))
				.andExpect(status().isForbidden());
	}

	private String login(String username, String password) throws Exception {
		String res = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								java.util.Map.of("username", username, "password", password))))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		JsonNode node = objectMapper.readTree(res);
		assertThat(node.has("accessToken")).isTrue();
		return node.get("accessToken").asText();
	}

}
