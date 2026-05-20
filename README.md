# Cadastro de Clientes 

API REST para cadastro, manutenção, remoção lógica e consulta de clientes com unidades consumidoras.

## Tecnologias

- Java 17
- Spring Boot 4
- Spring Data JPA / Hibernate
- Flyway + H2 (embarcado)
- Spring Validation
- SpringDoc OpenAPI (Swagger)
- Spring Security (JWT stateless, mock de usuários)
- Apache Kafka (apenas produtor)
- Maven

## Campo documento

O campo `documento` representa **CPF** (11 dígitos numéricos) ou **CNPJ** (14 caracteres), único por cliente.

- **CPF:** apenas números (máscara opcional).
- **CNPJ numérico (legado):** 14 dígitos.
- **CNPJ alfanumérico (novas inscrições a partir de jul/2026):** 12 primeiros caracteres alfanuméricos (`0-9`, `A-Z`) + 2 dígitos verificadores numéricos. Armazenado em maiúsculas, sem pontuação.

## Como executar

Pré-requisitos: JDK 17+, Maven (ou `./mvnw`) e [Docker](https://www.docker.com/) para o Kafka local.

### 1. Subir o Kafka

```bash
docker compose up -d
```

O broker fica em `localhost:9092`. O tópico `analise_cliente_mg` é criado automaticamente na primeira publicação.

### 2. Subir a API

```bash
./mvnw spring-boot:run
```

Variáveis opcionais:

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Brokers Kafka |
| `APP_KAFKA_ENABLED` | `true` | `false` desliga o produtor (útil sem broker) |
| `JWT_SECRET` | (valor em `application.properties`) | Segredo HS256 — mínimo 32 bytes em UTF-8; **altere em produção** |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | `admin` / `admin123` | Usuário mock com perfil **admin** |
| `CLIENTE_USERNAME` / `CLIENTE_PASSWORD` | `cliente` / `cliente123` | Usuário mock com perfil **cliente** |
| `APP_SECURITY_ENABLED` | `true` | `false` desliga JWT e libera todos os endpoints (útil em testes locais) |

A aplicação sobe na porta **8082**.

### Autenticação (JWT)

1. `POST /api/auth/login` com corpo `{ "username": "...", "password": "..." }`.
2. Resposta: `{ "accessToken", "tokenType": "Bearer", "expiresInSeconds": 3600 }` (token válido por **1 hora**).
3. Envie `Authorization: Bearer <accessToken>` nas rotas `/api/clientes/**`.

**Perfis mock**

| Usuário | Senha padrão | Permissões |
|---------|----------------|------------|
| `admin` | `admin123` | Todas as rotas (listar todos, recentes, inativos, buscar por id, cadastrar, atualizar, remover). |
| `cliente` | `cliente123` | **POST** cadastrar, **GET/PUT/DELETE** `/api/clientes/{id}` apenas para clientes que **ele próprio** cadastrou nesta execução (rastreio em memória). Não acessa listagens globais (`/api/clientes`, `/recentes`, `/inativos`). |

No Swagger: **Authorize** → esquema `bearer-jwt` → cole o token após o login.

- Swagger UI: http://localhost:8082/swagger-ui.html
- OpenAPI JSON: http://localhost:8082/v3/api-docs
- Console H2: http://localhost:8082/h2-console (JDBC URL: `jdbc:h2:mem:challenger`)

## Testes

```bash
./mvnw test
```

## API

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/auth/login` | Obter JWT (corpo: `username`, `password`) |
| POST | `/api/clientes` | Cadastrar |
| PUT | `/api/clientes/{id}` | Atualizar |
| DELETE | `/api/clientes/{id}` | Remoção lógica (`ativo = false`); retorna `{ "mensagem": "Cliente removido com sucesso" }` |
| GET | `/api/clientes` | Listar ativos |
| GET | `/api/clientes/inativos` | Listar excluídos logicamente (`ativo = false`) |
| GET | `/api/clientes/{id}` | Buscar por ID (somente ativos) |
| GET | `/api/clientes/recentes` | Últimos 20 cadastros ativos (ordem decrescente) |

Os endereços (cliente e unidades) são resolvidos via [ViaCEP](https://viacep.com.br/): envie o CEP no corpo da requisição.

## Regras de negócio

- Documento único por cliente
- Número de instalação único no sistema (não pode pertencer a outro cliente)
- Unidades em **SP, RS ou PR** não são permitidas
- Exclusão apenas lógica; consulta de inativos em `/api/clientes/inativos`
- Cliente com unidade consumidora em **MG** dispara publicação **síncrona** no tópico Kafka `analise_cliente_mg` ainda **dentro da mesma transação** do cadastro ou atualização (se o broker falhar, a transação é revertida — ver secção Kafka abaixo)

## Kafka (`analise_cliente_mg`)

Fluxo enxuto, apenas **produtor** (o enunciado não exige consumer na aplicação):

1. `ClienteService` chama `AnaliseClienteMgNotifier` após salvar o cliente (na mesma transação).
2. Se houver UC em MG, `KafkaAnaliseClienteMgNotifier` publica de forma **síncrona** via `AnaliseClienteMgKafkaProducer` (timeout configurável).
3. Se o broker estiver indisponível, a API responde **503** e a transação é revertida (cliente não fica persistido sem mensagem).
4. Payload JSON: `clienteId`, `documento`, `nome`, `numerosInstalacao` (UCs em MG), `publicadoEm`.

Propriedades úteis: `app.kafka.send-timeout-seconds` (padrão 5); o produtor usa `retries=0` e timeouts curtos para evitar bloqueio prolongado quando o Kafka está fora.

Para validar manualmente (com Kafka e API rodando), cadastre um cliente com endereço/UC em MG e consuma o tópico:

```bash
docker exec -it challenger-kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic analise_cliente_mg \
  --from-beginning
```

No Windows, o mesmo comando está em `TopicoKafka.bat` (atalho).

Nos testes automatizados o Kafka fica desabilitado (`app.kafka.enabled=false`).

## Postman

Importe a coleção em `postman/challenger-clientes.postman_collection.json`.

## Estrutura do projeto

```
domain/          → entidades JPA
repository/      → acesso a dados
service/         → regras de negócio
config/          → configuração global (ex.: OpenAPI com Bearer JWT)
web/             → controllers REST
web/dto/         → contratos de entrada e saída da API
integration/     → cliente ViaCEP
exception/       → tratamento global de erros
util/            → validações de documento, CEP e UF
messaging/       → contrato de notificação MG
messaging/kafka/ → produtor síncrono e notificador Kafka
security/        → JWT, propriedades, posse do cliente (mock), autorização por id (consulta e alteração)
```

## Justificativa de bibliotecas

- **Flyway**: versionamento explícito do schema, alinhado à avaliação de organização.
- **SpringDoc**: documentação interativa da API.
- **H2**: banco embarcado, sem dependências externas para executar.
- **Kafka (produtor)**: atende ao fluxo de notificação para análise de clientes com UC em MG; broker externo via Docker em desenvolvimento.
