
# Bolt Spark Registry API

A aplicação é uma API REST de cadastro de clientes, com foco em clientes que possuem unidades consumidoras (UCs) — cenário típico de energia/utilities.


## Pré-requisitos 
Para executar o projeto, será necessário instalar os seguintes programas:

-JDK 17 -Maven 3.6.3 -Docker Desktop

```bash
 mvn clean install
```
O comando irá baixar todas as dependências do projeto e criar um diretório target  com os artefatos construídos, que incluem o arquivo jar do projeto. Além disso, serão executados os testes unitários, e se algum falhar, o Maven exibirá essa informação no console.

## Como executar

Subir o Kafka
```
docker compose up -d
```

Ele irá baixar a imagem e iniciar o componente.

O broker fica em localhost:9092. O tópico analise_cliente_mg é criado automaticamente na primeira publicação.

Subir a API
```
./mvnw spring-boot:run
```

Você também pode usar uma IDE que suporte o desenvolvimento de JAVA+SPRING para rodar a API. Basta ir ao arquivo "ChallengerApplication.Java" e clicar em "RUN".

Variáveis opcionais:

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Brokers Kafka |
| `APP_KAFKA_ENABLED` | `true` | `false` desliga o produtor (útil sem broker) |
| `JWT_SECRET` | (valor em `application.properties`) | Segredo HS256 — mínimo 32 bytes em UTF-8|
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | `admin` / `admin123` | Usuário mock com perfil **admin** |
| `CLIENTE_USERNAME` / `CLIENTE_PASSWORD` | `cliente` / `cliente123` | Usuário mock com perfil **cliente** |
| `APP_SECURITY_ENABLED` | `true` | `false` desliga JWT e libera todos os endpoints (útil em testes locais) |

A aplicação sobe na porta **8082**.

## Autenticação (JWT)

A API conta com o Spring Security para assegurar as rotas criadas. Também existe a diferença entre perfil de ADMIN e CLIENTE.
Sendo elas:

*admin*: Pode acessar e utilizar todas as rotas.

*cliente*: Pode acessar apenas rotas de criar, listar por id(apenas os ids que foram criados por ele), atualizar(apenas os dados que foram criados por ele) e o delete lógico apenas dos dados que ele criou. 

| Usuário | Senha padrão | Permissões |
|---------|----------------|------------|
| `admin` | `admin123` | Todas as rotas (listar todos, recentes, inativos, buscar por id, cadastrar, atualizar, remover). |
| `cliente` | `cliente123` | **POST** cadastrar, **GET/PUT/DELETE** `/api/clientes/{id}` apenas para clientes que **ele próprio** cadastrou nesta execução (rastreio em memória). Não acessa listagens globais (`/api/clientes`, `/recentes`, `/inativos`). |

Para os testes é necessário fazer a autenticação JWT para receber o token de acess. Atualmente a sessão tem uma duração de 1hora para fazer testes. 

    1. `POST /api/auth/login` com corpo `{ "username": "...", "password": "..." }`.
    2. Resposta: `{ "accessToken", "tokenType": "Bearer", "expiresInSeconds": 3600 }` (token válido por **1 hora**).
    3. Envie `Authorization: Bearer <accessToken>` nas rotas `/api/clientes/**`.

*Caso não tenha o postman instalado ou outro programa para testes de aplicação back end, pode se utilizar o Swagger que está implementando na API.*

*Para consultar o bando de dados acesse a url abaixo do banco H2.*

No Swagger: **Authorize** → esquema `bearer-jwt` → cole o token após o login.

- Swagger UI: http://localhost:8082/swagger-ui.html
- OpenAPI JSON: http://localhost:8082/v3/api-docs
- Console H2: http://localhost:8082/h2-console (JDBC URL: `jdbc:h2:mem:challenger`)

## Postman

Você pode importar a coleção que está dentro do projeto. 
Você vai abrir a página do postman, vai ir no canto superior esquerdo nos três "pontinhos". 

<img width="475" height="96" alt="image" src="https://github.com/user-attachments/assets/794d2b71-3782-4e94-9c8f-26cfe1cd41c2" />

Após isso vá em import, irá abrir uma janela. Você pode abrir o `postman/challenger-clientes.postman_collection.json` onde está o arquivo e arrastar para essa janela ou clicar em files e ir até o `postman/challenger-clientes.postman_collection.json`, selecionar e abrir. 

<img width="650" height="565" alt="image" src="https://github.com/user-attachments/assets/94e257e4-76b4-441d-bf6c-4548c18f2083" />

<img width="787" height="353" alt="image" src="https://github.com/user-attachments/assets/90675d27-a33f-45e4-bb33-0b280218166a" />


Após isso ele irá carregar toda a configuração da coleção e você conseguirá fazer os testes via postman. 

<img width="370" height="458" alt="image" src="https://github.com/user-attachments/assets/c0c0c8e6-5574-4968-a995-d76c2a01318a" />

## Um pouco sobre a API e o projeto

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

Fluxo enxuto, apenas **produtor**:

1. `ClienteService` chama `AnaliseClienteMgNotifier` após salvar o cliente (na mesma transação).
2. Se houver UC em MG, `KafkaAnaliseClienteMgNotifier` publica de forma **síncrona** via `AnaliseClienteMgKafkaProducer` (timeout configurável).
3. Se o broker estiver indisponível, a API responde **503** e a transação é revertida (cliente não fica persistido sem mensagem).
4. Payload JSON: `clienteId`, `documento`, `nome`, `numerosInstalacao` (UCs em MG), `publicadoEm`.

Propriedades úteis: `app.kafka.send-timeout-seconds` (padrão 5); o produtor usa `retries=0` e timeouts curtos para evitar bloqueio prolongado quando o Kafka está fora.

Para validar manualmente (com Kafka e API rodando), cadastre um cliente com endereço/UC em MG e consuma o tópico:

*Rode esse comando para visualizar o consumo do tópico.*

```bash
docker exec -it challenger-kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic analise_cliente_mg \
  --from-beginning
```
Caso você queira optar por práticidade, fiz um arquivo .bat que já está com esse comando e você pode rodar para testar.
`TopicoKafka.bat` (atalho).

*Imagens referentes ao teste:*

<img width="577" height="336" alt="image" src="https://github.com/user-attachments/assets/964acf6c-569d-4f32-9914-a77d2b64ab11" />

<img width="1142" height="117" alt="image" src="https://github.com/user-attachments/assets/f9735713-c143-4110-9ecc-00c79b58dd94" />



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
