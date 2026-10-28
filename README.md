# 🖥️ Live UniPDS - Introdução ao Apache Camel

Este projeto foi desenvolvido para a Live de Introdução ao **Apache Camel** na pós-graduação **Java + IA** da **UniPDS**. 

O objetivo prático é integrar três microserviços independentes de uma loja online de Ebooks chamada **Cotubify**, evoluindo de uma arquitetura com sistemas isolados para uma arquitetura orientada a eventos usando **RabbitMQ** e o framework de integração **Apache Camel** (com abordagens tanto em **Spring Boot** quanto em **Quarkus**).

---

## 🏗️ Arquitetura

O sistema é composto por 3 aplicações distintas que representam o ecossistema da loja **Cotubify**:

1. **`cotubify-loja`** (Porta `8080`): API em Spring Boot para consulta de catálogo e recepção de pedidos. Persiste as informações em um banco MySQL.
2. **`cotubify-notas`** (Porta `8081`): Serviço em Quarkus responsável por receber os dados de um pedido e simular a geração de uma Nota Fiscal eletrônica em formato XML.
3. **`cotubify-gerador`** (Porta `8082`): API em Spring Boot que simula o pipeline de compilação do Ebook (geração dos arquivos `.pdf` e `.epub` a partir de uma fonte Markdown em repositório Git).

### Ponto de partida (branch `main`)

Os sistemas estão isolados. O pedido é feito na loja e persistido no banco de dados, mas as notas fiscais não são geradas automaticamente e os ebooks não são compilados.

```mermaid
graph TD
    Client[Cliente / Postman] -->|POST /api/orders| Loja[cotubify-loja :8080]
    Loja -->|Persistência| MySQL[(MySQL)]
    
    Notas[cotubify-notas :8081]
    Gerador[cotubify-gerador :8082]

    classDef integrated fill:#ff9,stroke:#333,stroke-width:2px;
    class Loja integrated;

    classDef unintegrated fill:#ff9,stroke:#333,stroke-width:2px,stroke-dasharray: 5 5;
    class Notas,Gerador unintegrated;
```

## 🛠️ Tecnologias e Versões Utilizadas

| Componente | Framework / Core | Versão | Java | Principais Bibliotecas & Starters Camel |
| :--- | :--- | :--- | :--- | :--- |
| **`cotubify-loja`** | Spring Boot | `4.1.0` | `25` | `camel-spring-boot-starter`<br>`camel-spring-rabbitmq-starter`<br>`camel-jackson-starter`<br>`spring-boot-starter-amqp` |
| **`cotubify-notas`** | Quarkus | `3.36.3` | `25` | `camel-quarkus-amqp`<br>`camel-quarkus-jackson` |
| **`cotubify-gerador`**| Spring Boot | `4.1.0` | `25` | `camel-spring-boot-starter`<br>`camel-spring-rabbitmq-starter`<br>`camel-jackson-starter`<br>`camel-aws2-s3-starter`<br>`camel-mail-starter`<br>`spring-boot-starter-amqp`<br>`html2pdf` (iText) `6.3.1`<br>`epublib-core` `3.1` |
| **MySQL (Docker)** | MySQL | `9.0` | N/A | Banco de dados relacional da loja |
| **RabbitMQ (Docker)**| RabbitMQ | `4.2` | N/A | Broker de mensageria |
| **LocalStack (Docker)**| LocalStack | `community-archive` | N/A | S3 local para armazenar os ebooks gerados |
| **Mailpit (Docker)** | Mailpit | `latest` | N/A | Servidor SMTP de teste + UI web para ver os e-mails |

> [!NOTE]
> Os projetos Spring Boot (`cotubify-loja` e `cotubify-gerador`) importam o BOM `camel-spring-boot-bom` na versão `4.21.0`. O `cotubify-notas` usa o `quarkus-camel-bom`, alinhado à versão da plataforma Quarkus.

> [!NOTE]
> O `epublib-core` não está no Maven Central. Por isso o `pom.xml` do `cotubify-gerador` declara o repositório `psiegman-repo` (`https://github.com/psiegman/mvn-repo/raw/master/releases`). Ele também exclui o `slf4j-simple` que vem transitivamente, para não conflitar com o Logback do Spring Boot.

---

## 📂 Estrutura e Classes de Domínio

### 🛒 `cotubify-loja` (Spring Boot)
Gerencia o banco de dados via JPA/Hibernate e inicializa os dados usando Flyway.
- **Entidades de Banco (`br.com.unipds.cotubify.loja.entity`):**
  - [Ebook.java](file:///Users/alexandreaquiles/Documents/unipds/projetos-lives/live-apache-camel/cotubify-loja/src/main/java/br/com/unipds/cotubify/loja/entity/Ebook.java): Contém título, preço, ISBN, e a URL do repositório Git de origem.
  - [Author.java](file:///Users/alexandreaquiles/Documents/unipds/projetos-lives/live-apache-camel/cotubify-loja/src/main/java/br/com/unipds/cotubify/loja/entity/Author.java): Dados do autor (Nome, mini-bio).
  - [Order.java](file:///Users/alexandreaquiles/Documents/unipds/projetos-lives/live-apache-camel/cotubify-loja/src/main/java/br/com/unipds/cotubify/loja/entity/Order.java): Pedido mapeado contendo cliente, CPF, e endereço.
  - [OrderItem.java](file:///Users/alexandreaquiles/Documents/unipds/projetos-lives/live-apache-camel/cotubify-loja/src/main/java/br/com/unipds/cotubify/loja/entity/OrderItem.java): Itens comprados, preço histórico de venda e desconto aplicado.
- **DTOs (`br.com.unipds.cotubify.loja.dto`):**
  - `OrderRequestDTO` e `OrderItemRequestDTO`.
- **Integração (`br.com.unipds.cotubify.loja.integration`):**
  - [OrderRoute.java](file:///Users/alexandreaquiles/Documents/unipds/projetos-lives/live-apache-camel/cotubify-loja/src/main/java/br/com/unipds/cotubify/loja/integration/OrderRoute.java): serializa o pedido em JSON e publica na exchange **fanout** `cotubify.orders`.

### 🧾 `cotubify-notas` (Quarkus)
Serviço simples que opera como gerador de Nota Fiscal.
- **DTOs de Entrada (`br.com.unipds.cotubify.notas.dto`):**
  - [PedidoDTO.java](file:///Users/alexandreaquiles/Documents/unipds/projetos-lives/live-apache-camel/cotubify-notas/src/main/java/br/com/unipds/cotubify/notas/dto/PedidoDTO.java): Modelo simplificado contendo ID do pedido, dados do cliente e lista de itens.
  - [ItemPedidoDTO.java](file:///Users/alexandreaquiles/Documents/unipds/projetos-lives/live-apache-camel/cotubify-notas/src/main/java/br/com/unipds/cotubify/notas/dto/ItemPedidoDTO.java): Itens com o preço de compra e desconto.
- **Serviço:**
  - [NotaFiscalService.java](file:///Users/alexandreaquiles/Documents/unipds/projetos-lives/live-apache-camel/cotubify-notas/src/main/java/br/com/unipds/cotubify/notas/service/NotaFiscalService.java): Calcula o valor total líquido (Preço - Desconto), monta um payload XML e faz a saída no console.
- **Integração (`br.com.unipds.cotubify.notas.integration`):**
  - [NotaFiscalRoute.java](file:///Users/alexandreaquiles/Documents/unipds/projetos-lives/live-apache-camel/cotubify-notas/src/main/java/br/com/unipds/cotubify/notas/integration/NotaFiscalRoute.java): consome a `notas.queue` via AMQP 1.0 e traduz o JSON da loja para o `PedidoDTO` (incluindo a lista de itens).

### ⚙️ `cotubify-gerador` (Spring Boot)
API REST que simula a pipeline de criação de arquivos físicos `.pdf` e `.epub`.
- **DTOs de Entrada (`br.com.unipds.cotubify.gerador.dto`):**
  - [GeracaoEbookRequest.java](file:///Users/alexandreaquiles/Documents/unipds/projetos-lives/live-apache-camel/cotubify-gerador/src/main/java/br/com/unipds/cotubify/gerador/dto/GeracaoEbookRequest.java): ID do pedido, cliente, título do ebook e a URL Git a ser clonada.
- **Serviço:**
  - [GeradorEbookService.java](file:///Users/alexandreaquiles/Documents/unipds/projetos-lives/live-apache-camel/cotubify-gerador/src/main/java/br/com/unipds/cotubify/gerador/service/GeradorEbookService.java): Gera **de verdade** os binários do ebook a partir de um HTML montado com os dados do pedido — `gerarPdf` (via iText `HtmlConverter`) e `gerarEpub` (via `epublib`), ambos devolvendo `byte[]`. O método `gerarPdfEEpub` continua existindo para o endpoint REST manual e grava os dois arquivos em `target/ebooks/{pedidoId}/`.
- **Integração (`br.com.unipds.cotubify.gerador.integration`):**
  - [GeradorEbookRoute.java](file:///Users/alexandreaquiles/Documents/unipds/projetos-lives/live-apache-camel/cotubify-gerador/src/main/java/br/com/unipds/cotubify/gerador/integration/GeradorEbookRoute.java): consome a `gerador.queue`, guarda os dados do pedido em headers, faz o **Splitter** dos itens e, para cada ebook, gera o PDF e o EPUB e envia ambos para o **S3** (LocalStack) usando **Claim Check**. No fim do split, um **Aggregator** junta os ebooks gerados e dispara **um único e-mail HTML** por pedido com todos os links de download. Falhas são retentadas com backoff exponencial e, se persistirem, o pedido original vai para um **Dead Letter Channel** (`cotubify.dlq`).
  - [EmailService.java](file:///Users/alexandreaquiles/Documents/unipds/projetos-lives/live-apache-camel/cotubify-gerador/src/main/java/br/com/unipds/cotubify/gerador/integration/EmailService.java): recebe a `List<GeracaoEbookRequest>` agregada e monta o corpo HTML do e-mail, com uma linha (PDF | EPUB) por ebook.

---

## ⚡ Como Executar o Projeto

### Passo 1: Subir a Infraestrutura (Docker)
Na pasta raiz e inicie os containers do MySQL, RabbitMQ, LocalStack e Mailpit:
```bash
docker compose up -d
```
O RabbitMQ já sobe com a exchange, as filas e os bindings criados — veja [Topologia do RabbitMQ](#-topologia-do-rabbitmq-importada-automaticamente) logo abaixo. O LocalStack já sobe com o bucket S3 criado — veja [S3 local e e-mails](#-s3-local-localstack-e-e-mails-mailpit).

> [!NOTE]
> * **RabbitMQ UI:** acesse em `http://localhost:15672` (Usuário: `admin` / Senha: `senha123`).
> * **MySQL:** exposto na porta `3306` (Usuário: `admin` / Senha: `senha123`, Banco de dados: `cotubify`).
> * **LocalStack (S3):** endpoint em `http://localhost:4566`, bucket `cotubify-ebooks`.
> * **Mailpit:** SMTP na porta `1025` e **UI web** em `http://localhost:8025`.

#### 📨 Topologia do RabbitMQ (importada automaticamente)

Nenhuma das rotas declara a topologia (o consumidor do gerador usa `autoDeclare=false`), então a exchange, as filas e os bindings precisam existir **antes** de subir as aplicações. Essa configuração está versionada no [rabbitmq/definitions-rabbit.json](file:///Users/alexandreaquiles/Documents/unipds/projetos-lives/live-apache-camel/rabbitmq/definitions-rabbit.json) e é carregada pelo broker **a cada boot** — não há passo manual:

| Recurso | Nome | Tipo | Observação |
| :--- | :--- | :--- | :--- |
| Exchange | `cotubify.orders` | `fanout` | Publish-Subscribe Channel — faz broadcast do pedido |
| Fila | `notas.queue` | `classic` | Consumida pelo `cotubify-notas` (AMQP 1.0) |
| Fila | `gerador.queue` | `classic` | Consumida pelo `cotubify-gerador` (spring-rabbitmq) |
| Binding | `cotubify.orders` → `notas.queue` | — | Sem routing key (fanout ignora) |
| Binding | `cotubify.orders` → `gerador.queue` | — | Sem routing key (fanout ignora) |
| Exchange | `cotubify.dlq` | `fanout` | **Dead Letter Channel** do gerador — recebe os pedidos que falharam mesmo depois dos retries |
| Fila | `gerador.dlq` | `classic` | Onde os pedidos com falha ficam parados, esperando análise ou reprocessamento |
| Binding | `cotubify.dlq` → `gerador.dlq` | — | Sem routing key (fanout ignora) |

Os arquivos de configuração do broker ficam na pasta [rabbitmq/](file:///Users/alexandreaquiles/Documents/unipds/projetos-lives/live-apache-camel/rabbitmq). O `docker-compose.yml` monta três arquivos no container e um volume nomeado para os dados do broker:

```yaml
volumes:
  # conf.d preserva o rabbitmq.conf gerado pela imagem a partir das variáveis de ambiente
  - ./rabbitmq/rabbitmq.conf:/etc/rabbitmq/conf.d/20-load-definitions.conf:ro
  - ./rabbitmq/definitions-rabbit.json:/etc/rabbitmq/definitions.json:ro
  - ./rabbitmq/enabled_plugins:/etc/rabbitmq/enabled_plugins:ro
  - rabbitmq_data:/var/lib/rabbitmq
```

O [rabbitmq/enabled_plugins](file:///Users/alexandreaquiles/Documents/unipds/projetos-lives/live-apache-camel/rabbitmq/enabled_plugins) liga, além dos plugins que a imagem `-management` já traz (`rabbitmq_management` e `rabbitmq_prometheus`), o **Shovel** (`rabbitmq_shovel` e `rabbitmq_shovel_management`). É ele que habilita a seção **Move messages** na página de uma fila na UI — usada para devolver as mensagens da `gerador.dlq` para a `gerador.queue` depois de corrigir o problema (veja [Dead Letter Channel](#6-cotubify-gerador-dead-letter-channel-e-retries)).

O [rabbitmq/rabbitmq.conf](file:///Users/alexandreaquiles/Documents/unipds/projetos-lives/live-apache-camel/rabbitmq/rabbitmq.conf) tem uma linha só — `load_definitions = /etc/rabbitmq/definitions.json` — e é montado dentro de `conf.d/` justamente para não sobrescrever o `rabbitmq.conf` que a imagem gera a partir do `RABBITMQ_DEFAULT_USER`/`RABBITMQ_DEFAULT_PASS`.

Para conferir, acesse a **RabbitMQ UI** em `http://localhost:15672` (`admin` / `senha123`): na aba **Exchanges** a `cotubify.orders` deve aparecer como `fanout` e, ao clicar nela, as duas filas devem estar listadas em *Bindings*. Pela linha de comando:

```bash
curl -s -u admin:senha123 http://localhost:15672/api/exchanges/%2F/cotubify.orders/bindings/source
```

Se você **editar** o `definitions-rabbit.json`, basta reiniciar o broker (`docker compose restart rabbit`) para reimportar. Também dá para importar manualmente pela UI em **Overview → Import definitions** ou via API:

```bash
curl -u admin:senha123 -X POST http://localhost:15672/api/definitions \
  -H "Content-Type: application/json" \
  --data-binary @rabbitmq/definitions-rabbit.json
```

> [!IMPORTANT]
> Quando há definições para carregar, o RabbitMQ **não** cria o usuário padrão (`Will not seed default virtual host and user: have definitions to load...`). O usuário `admin` passa a vir do próprio `definitions-rabbit.json`, que contém o hash da senha `senha123`. Ou seja: alterar `RABBITMQ_DEFAULT_USER`/`RABBITMQ_DEFAULT_PASS` no compose não muda mais as credenciais — é preciso regerar o arquivo de definições.

> [!NOTE]
> O arquivo foi exportado de um broker de desenvolvimento e inclui o usuário `admin` e suas permissões. Por isso ele só serve para o ambiente local desta live; não reaproveite em outros ambientes.

#### 🪣 S3 local (LocalStack) e e-mails (Mailpit)

O `cotubify-gerador` não guarda mais os ebooks só em disco: ele **sobe os binários para o S3** e envia um e-mail com os links. Para não depender da AWS de verdade, o `docker-compose.yml` traz dois containers:

| Serviço | Imagem | Portas | Para quê |
| :--- | :--- | :--- | :--- |
| `localstack` | `localstack/localstack:community-archive` | `4566` | Emula o S3 localmente (`SERVICES: s3`) |
| `mailpit` | `axllent/mailpit:latest` | `1025` (SMTP) / `8025` (UI) | Captura os e-mails enviados, sem sair da máquina |

O bucket é criado automaticamente no boot pelo script [localstack/init/01-cria-bucket.sh](file:///Users/alexandreaquiles/Documents/unipds/projetos-lives/live-apache-camel/localstack/init/01-cria-bucket.sh), montado em `/etc/localstack/init/ready.d`:

```bash
awslocal s3 mb s3://cotubify-ebooks

# deixa os objetos legíveis sem assinatura, para o link do e-mail funcionar no navegador
awslocal s3api put-bucket-policy --bucket cotubify-ebooks --policy '{ ... "Action": "s3:GetObject" ... }'
```

A `bucket-policy` de leitura pública é o que permite clicar no link do e-mail e baixar o arquivo direto do navegador, sem assinatura AWS.

As coordenadas ficam no `application.properties` do gerador, com credenciais fake (`test`/`test`) e `overrideEndpoint` apontando para o LocalStack:

```properties
cotubify.s3.bucket=cotubify-ebooks
cotubify.s3.uri=aws2-s3://cotubify-ebooks?accessKey=test&secretKey=test&region=us-east-1&overrideEndpoint=true&uriEndpointOverride=http://localhost:4566&forcePathStyle=true
cotubify.s3.url-publica=http://localhost:4566/cotubify-ebooks

cotubify.smtp.uri=smtp://localhost:1025?from=contato@cotubify.com.br&contentType=RAW(text/html;charset=UTF-8)&useHeaderRecipients=true&useHeaderSubject=true
```

> [!NOTE]
> O `forcePathStyle=true` é obrigatório com LocalStack: sem ele o SDK monta a URL no estilo *virtual-host* (`cotubify-ebooks.localhost:4566`), que não resolve.

Para conferir os arquivos gerados:

```bash
# lista tudo que foi para o bucket
aws --endpoint-url http://localhost:4566 s3 ls s3://cotubify-ebooks/pedidos/ --recursive

# ou simplesmente abra o link no navegador
open http://localhost:4566/cotubify-ebooks/pedidos/1/1.pdf
```

E os e-mails aparecem na UI do Mailpit em `http://localhost:8025`.

### Passo 2: Rodar os Microserviços
Execute cada um dos projetos em terminais separados usando os comandos de desenvolvimento:

* **Cotubify Loja (Spring Boot):**
  ```bash
  cd cotubify-loja
  ./mvnw spring-boot:run
  ```
  *(Porta padrão: `8080`)*

* **Cotubify Notas (Quarkus):**
  ```bash
  cd cotubify-notas
  ./mvnw quarkus:dev
  ```
  *(Porta padrão: `8081`)*

* **Cotubify Gerador (Spring Boot):**
  ```bash
  cd cotubify-gerador
  ./mvnw spring-boot:run
  ```
  *(Porta padrão: `8082`)*

---

## 🔌 Exemplos de Requisições das APIs (REST)

Cada microserviço contém sua respectiva collection do Postman na raiz do seu diretório. Abaixo estão os exemplos formatados para testes rápidos via `curl`:

### 🛒 1. Cotubify Loja (Porta 8080)
#### Listar Ebooks Cadastrados (GET)
```bash
curl -X GET http://localhost:8080/api/ebooks
```
**Resposta esperada (JSON):**
```json
[
  {
    "id": 1,
    "title": "Enterprise Integration Patterns",
    "description": "O livro definitivo sobre padrões de integração de sistemas corporativos.",
    "price": 249.90,
    "gitRepoUrl": "git@github.com:cotubify-internal/eip-book.git",
    "authors": ["Gregor Hohpe", "Bobby Woolf"]
  },
  {
    "id": 2,
    "title": "Refactoring",
    "description": "Melhorando o design de códigos existentes de forma segura e pragmática.",
    "price": 199.90,
    "gitRepoUrl": "git@github.com:cotubify-internal/refactoring-book.git",
    "authors": ["Martin Fowler"]
  }
]
```

#### Criar Novo Pedido (POST)
```bash
curl -X POST http://localhost:8080/api/orders \
-H "Content-Type: application/json" \
-d '{
    "clientName": "Giovana Daiane Simone Ribeiro",
    "cpf": "408.339.952-00",
    "email": "giovana.daiane.ribeiro@fictor.com.br",
    "address": "Avenida Tailândia, 681, Cabralzinho, Macapá / AP",
    "items": [
        { "ebookId": 1, "discount": 50.0 },
        { "ebookId": 2, "discount": 0.0 }
    ]
}'
```

> [!TIP]
> Este é o **único** endpoint que precisa ser chamado para exercitar toda a integração. Com os três serviços no ar, o pedido é publicado na exchange fanout e você verá, em paralelo: a nota fiscal em XML no console do `cotubify-notas` e **dois** ebooks sendo gerados (um por item) no `cotubify-gerador` — cada um com o PDF e o EPUB no bucket `cotubify-ebooks` do LocalStack — e **um** e-mail com os links dos dois ebooks aparecendo na UI do Mailpit (`http://localhost:8025`). Os endpoints abaixo servem para testar cada serviço isoladamente, sem passar pelo broker.

---

### 🧾 2. Cotubify Notas (Porta 8081)
#### Simular Nota Fiscal Manualmente (POST)
```bash
curl -X POST http://localhost:8081/api/notas \
-H "Content-Type: application/json" \
-d '{
  "id": 101,
  "nomeCliente": "João Silva",
  "cpfCliente": "111.222.333-44",
  "enderecoCliente": "Rua das Flores, 123",
  "dataCriacao": "2026-06-23T14:30:00",
  "itens": [
    {
      "ebookId": 1,
      "tituloEbook": "Enterprise Integration Patterns",
      "precoCompra": 250.00,
      "desconto": 50.00
    }
  ]
}'
```
**Resposta esperada (XML):**
```xml
<notaFiscal>
  <pedidoId>101</pedidoId>
  <cliente>
    <nome>João Silva</nome>
    <cpf>111.222.333-44</cpf>
    <endereco>Rua das Flores, 123</endereco>
  </cliente>
  <valorTotal>200.00</valorTotal>
  <descricao>
    - Enterprise Integration Patterns (1)
  </descricao>
</notaFiscal>
```

---

### ⚙️ 3. Cotubify Gerador (Porta 8082)
#### Acionar Geração de Ebook Manualmente (POST)
```bash
curl -X POST http://localhost:8082/api/gerador \
-H "Content-Type: application/json" \
-d '{
  "pedidoId": 101,
  "nomeCliente": "João Silva",
  "cpfCliente": "111.222.333-44",
  "ebookId": 1,
  "tituloEbook": "Enterprise Integration Patterns",
  "urlRepositorioGit": "git@github.com:cotubify-internal/eip-book.git"
}'
```

> [!NOTE]
> Esse endpoint chama o `gerarPdfEEpub` diretamente, **sem passar pela rota Camel**: os arquivos são gravados em `cotubify-gerador/target/ebooks/{pedidoId}/{ebookId}.pdf` e `.epub`, e nada vai para o S3 nem para o e-mail. Para exercitar o Claim Check, o upload e o envio de e-mail, use o `POST /api/orders` da loja.

---

## 🎯 Estado Atual da Integração

Os três microserviços estão integrados de forma assíncrona. A loja publica o pedido criado em uma exchange **fanout** (`cotubify.orders`) do RabbitMQ — um **Publish-Subscribe Channel**. A mesma mensagem é entregue para as duas filas:

* **`notas.queue`** → o `cotubify-notas` traduz o JSON para o seu próprio modelo e imprime a nota fiscal em XML.
* **`gerador.queue`** → o `cotubify-gerador` faz o **Splitter** dos itens do pedido e, para cada ebook, gera PDF e EPUB e publica os dois no **S3** usando **Claim Check**. Um **Aggregator** junta os ebooks gerados e envia **um** e-mail por pedido com todos os links de download. Se algo falhar, o Camel tenta de novo 3 vezes e, persistindo o erro, publica o pedido original no **Dead Letter Channel** (`cotubify.dlq` → `gerador.dlq`).

O ponto importante é que a loja **não sabe** quem consome o evento: adicionar um novo consumidor é só criar uma nova fila com binding na exchange, sem tocar no código do produtor.

```mermaid
graph TD
    Client[Cliente / Postman] -->|POST /api/orders| Loja[cotubify-loja :8080]
    Loja -->|1. Salva pedido| MySQL[(MySQL)]
    Loja -->|2. Envia para direct:new-order| CamelLoja[Camel Context]
    CamelLoja -->|3. Marshal JSON| Exchange[[RabbitMQ: cotubify.orders - fanout]]

    Exchange -->|broadcast| FilaNotas[[notas.queue]]
    Exchange -->|broadcast| FilaGerador[[gerador.queue]]

    FilaNotas -->|4a. Consume AMQP 1.0| CamelNotas[Camel Context Quarkus]
    CamelNotas -->|5a. Unmarshal & Translate| Service[NotaFiscalService]
    Service -->|6a. Imprime XML| ConsoleNotas[console cotubify-notas :8081]

    FilaGerador -->|4b. Consume spring-rabbitmq| CamelGerador[Camel Context Spring Boot]
    CamelGerador -->|5b. Headers + Splitter dos itens| SplitGerador[1 mensagem por ebook]
    SplitGerador -->|6b. Translate & Bean| ServiceGerador[GeradorEbookService]
    ServiceGerador -->|7b. Gera .pdf e .epub em memoria| ClaimCheck[Claim Check: Push / Pop]
    ClaimCheck -->|8b. Upload dos binarios| S3[(S3 - LocalStack :4566)]
    ClaimCheck -->|9b. Fim do split| Aggregator[Aggregator: List de ebooks do pedido]
    Aggregator -->|10b. 1 e-mail HTML com todos os links| SMTP[Mailpit :1025]
    SMTP --> MailUI[UI do Mailpit :8025]

    CamelGerador -.->|falhou apos 3 retries: mensagem original| DLX[[RabbitMQ: cotubify.dlq - fanout]]
    DLX -.-> FilaDLQ[[gerador.dlq]]

    classDef integrated fill:#ff9,stroke:#333,stroke-width:2px;
    class Loja,CamelNotas,CamelGerador integrated;

    classDef broker fill:#ff9,stroke:#333,stroke-width:2px;
    class Exchange,FilaNotas,FilaGerador,DLX,FilaDLQ broker;

    classDef infra fill:#ff9,stroke:#333,stroke-width:2px,stroke-dasharray: 5 5;
    class S3,SMTP,MailUI infra;
```

---

## 🔀 Detalhes da Integração Camel

O fluxo integrado funciona da seguinte forma:

### 1. Produtor: `cotubify-loja`
* **Controller:** No [OrderController.java](file:///Users/alexandreaquiles/Documents/unipds/projetos-lives/live-apache-camel/cotubify-loja/src/main/java/br/com/unipds/cotubify/loja/controller/OrderController.java), a dependência `ProducerTemplate camelTemplate` do Apache Camel é injetada. Após salvar o pedido no banco, o payload da entidade `Order` persistida é enviado para o canal interno do Camel:
  ```java
  Order savedOrder = orderRepository.save(order);
  camelTemplate.sendBody("direct:new-order", savedOrder);
  ```
* **Rota Camel:** No [OrderRoute.java](file:///Users/alexandreaquiles/Documents/unipds/projetos-lives/live-apache-camel/cotubify-loja/src/main/java/br/com/unipds/cotubify/loja/integration/OrderRoute.java), a rota intercepta o pedido, serializa a entidade JPA em JSON usando **Jackson** e publica na exchange **fanout**. Note que não há `routingKey` nem `queues`: o produtor não conhece os consumidores.
  ```java
  from("direct:new-order")
      .log("Camel recebeu um Order de id: ${body.id}")
      .marshal().json(JsonLibrary.Jackson)
      .to("spring-rabbitmq:cotubify.orders?exchangeType=fanout")
      .log("Mensagem produzida com sucesso para o RabbitMQ.");
  ```

### 2. Consumidor: `cotubify-notas` (Message Translator)
* **Configuração:** O arquivo de propriedades injeta a conexão AMQP no Broker de Mensagens.
* **Rota Camel:** No [NotaFiscalRoute.java](file:///Users/alexandreaquiles/Documents/unipds/projetos-lives/live-apache-camel/cotubify-notas/src/main/java/br/com/unipds/cotubify/notas/integration/NotaFiscalRoute.java), o Camel Quarkus consome as mensagens da fila usando AMQP 1.0, realiza a desserialização do JSON para `JsonNode` e executa uma **tradução estrutural de dados** (Pattern *Message Translator*) antes de repassar os dados limpos ao `NotaFiscalService`. A tradução percorre os `items` do pedido, achatando o `ebook` aninhado no `ItemPedidoDTO` do Quarkus:
  ```java
  from("amqp:queue:notas.queue")
      .log("Nova mensagem de pedido recebido do RabbitMQ")
      .unmarshal(new JacksonDataFormat(JsonNode.class))
      .process(exchange -> {
          // items[].ebook.{id,title} + items[].{purchasePrice,discount} -> ItemPedidoDTO
          // e o restante do JSON -> PedidoDTO
      })
      .bean(notaFiscalService, "gerarNotaFiscal");
  ```

### 3. Consumidor: `cotubify-gerador` (Splitter)
* **Configuração:** As credenciais do broker ficam em `spring.rabbitmq.*` no `application.properties`.
* **Rota Camel:** No [GeradorEbookRoute.java](file:///Users/alexandreaquiles/Documents/unipds/projetos-lives/live-apache-camel/cotubify-gerador/src/main/java/br/com/unipds/cotubify/gerador/integration/GeradorEbookRoute.java), o gerador consome a `gerador.queue` (com `autoDeclare=false`, já que a topologia é criada fora da aplicação). Como um pedido pode ter vários ebooks e o gerador trabalha **um ebook por vez**, a rota usa o **Splitter** para quebrar a lista de `items` em uma mensagem por item, com `parallelProcessing()`:
  ```java
  from("spring-rabbitmq:cotubify.orders?queues=gerador.queue&autoDeclare=false")
      .unmarshal().json(JsonLibrary.Jackson)

      // dados do pedido viram headers, pois o body será substituído pelo item
      .setHeader("pedidoId", simple("${body[id]}"))
      .setHeader("nomeCliente", simple("${body[clientName]}"))
      .setHeader("cpfCliente", simple("${body[cpf]}"))
      .setHeader("emailCliente", simple("${body[email]}"))

      .split(simple("${body[items]}")).parallelProcessing()
          .process(exchange -> {
              // item + headers do pedido -> GeracaoEbookRequest
          })
          .bean(geradorEbookService, "gerarPdfEEpub")
      .end()
      .log("Pedido ${header.pedidoId} processado com sucesso. Todos os ebooks gerados.");
  ```
* **Por que headers?** O Splitter troca o body pelo item corrente, então os dados do pedido (id, cliente, CPF) são promovidos a headers antes do split para sobreviverem a cada sub-mensagem — servindo como **Correlation Identifier** entre o pedido e os ebooks gerados. Depois do `.end()`, o Camel volta a ter o contexto do pedido inteiro. O que vira o body nesse ponto é decidido pela `AggregationStrategy` passada ao `split` — veja a [seção 5](#5-cotubify-gerador-aggregator--um-e-mail-por-pedido-camel-mail).

### 4. `cotubify-gerador`: Claim Check + upload para o S3

Dentro do split, cada ebook precisa virar **dois** arquivos (PDF e EPUB) e ir para o S3. O problema é que o componente `aws2-s3` sobe **o body** como conteúdo do objeto — ou seja, depois de gerar o PDF o body vira `byte[]`, e o `GeracaoEbookRequest` (necessário para gerar o EPUB logo em seguida) seria perdido.

A solução é o **Claim Check EIP**, que guarda o body num repositório interno do Camel e o devolve depois:

```java
.claimCheck(ClaimCheckOperation.Push)        // guarda o GeracaoEbookRequest

.bean(geradorEbookService, "gerarPdf")       // body vira byte[] do PDF
.setHeader(AWS2S3Constants.KEY, simple("pedidos/${header.pedidoId}/${header.ebookId}.pdf"))
.setHeader(AWS2S3Constants.CONTENT_TYPE, constant("application/pdf"))
.to("{{cotubify.s3.uri}}")
.log("PDF no S3: ${header.CamelAwsS3Key}")

.claimCheck(ClaimCheckOperation.Pop)         // body volta a ser o GeracaoEbookRequest

.claimCheck(ClaimCheckOperation.Push)
.bean(geradorEbookService, "gerarEpub")      // mesma dança para o EPUB
.setHeader(AWS2S3Constants.KEY, simple("pedidos/${header.pedidoId}/${header.ebookId}.epub"))
.setHeader(AWS2S3Constants.CONTENT_TYPE, constant("application/epub+zip"))
.to("{{cotubify.s3.uri}}")
.claimCheck(ClaimCheckOperation.Pop)
```

* `Push` empilha o body atual; `Pop` restaura o último empilhado. Por isso o par `Push` … `Pop` aparece duas vezes, uma para cada formato.
* A chave do objeto (`AWS2S3Constants.KEY`) é montada com os headers do pedido: `pedidos/{pedidoId}/{ebookId}.pdf`. É o **Correlation Identifier** valendo também para a organização do bucket.
* A URI do S3 vem de *property placeholder* (`{{cotubify.s3.uri}}`), então trocar o LocalStack pela AWS de verdade é mudar uma linha do `application.properties`. O mesmo vale para o SMTP (`{{cotubify.smtp.uri}}`), na seção seguinte.

### 5. `cotubify-gerador`: Aggregator + um e-mail por pedido (`camel-mail`)

Na Live 3 o e-mail era enviado **dentro** do split: um pedido com dois ebooks gerava **dois** e-mails para o mesmo cliente. O ideal é mandar **um** e-mail por pedido, com os links de todos os ebooks — ou seja, juntar de novo as sub-mensagens que o Splitter separou. Esse é o **Aggregator EIP**.

O Splitter do Camel já tem um Aggregator embutido: basta passar uma `AggregationStrategy` como segundo argumento do `split`. Com `AggregationStrategies.flexible().accumulateInCollection(ArrayList.class)`, o body de cada sub-mensagem ao final do split (o `GeracaoEbookRequest`, restaurado pelo último `Pop` do Claim Check) é acumulado numa lista, que vira o body depois do `.end()`:

```java
.split(simple("${body[items]}"),
        AggregationStrategies.flexible()
                .accumulateInCollection(ArrayList.class)
)
.parallelProcessing()
.shareUnitOfWork().stopOnException()

    // ... gera PDF/EPUB e sobe pro S3 com Claim Check (seção 4) ...

.end()   // aqui o body é uma List<GeracaoEbookRequest> com todos os ebooks do pedido

.setHeader("To",      simple("${header.emailCliente}"))
.setHeader("Subject", simple("Seus ebooks do pedido #${header.pedidoId} estão prontos!"))

.bean(emailService, "montarCorpoDoEmail")

.removeHeaders("*", "To", "Subject", "From", "pedidoId")

.to("{{cotubify.smtp.uri}}")
.log("E-mail enviado para ${header.To}")
```

* O corpo do e-mail saiu da rota e foi para o [EmailService.java](file:///Users/alexandreaquiles/Documents/unipds/projetos-lives/live-apache-camel/cotubify-gerador/src/main/java/br/com/unipds/cotubify/gerador/integration/EmailService.java): montar uma lista HTML percorrendo uma `List` com `simple()` ficaria ilegível, enquanto em Java é um `stream().map(...)` com *text block*. O Camel faz o binding do body (`List<GeracaoEbookRequest>`) para o parâmetro do método `montarCorpoDoEmail` e usa o retorno (`String`) como novo body.
* O `.removeHeaders("*", "To", "Subject", "From", "pedidoId")` remove todos os headers, exceto os listados. O `camel-mail` copia os headers da mensagem Camel para os headers do e-mail, então sem essa limpeza dados como `cpfCliente` e `nomeCliente` iriam parar no cabeçalho do e-mail enviado. O `pedidoId` fica porque é usado no log final.
* `shareUnitOfWork()` e `stopOnException()` são assunto do tratamento de erros — veja a [seção 6](#6-cotubify-gerador-dead-letter-channel-e-retries).

Assim como a do S3, a URI do SMTP fica fora do código, no `application.properties`:

```properties
cotubify.smtp.uri=smtp://localhost:1025?from=contato@cotubify.com.br&contentType=RAW(text/html;charset=UTF-8)&useHeaderRecipients=true&useHeaderSubject=true
```

* `useHeaderRecipients=true` e `useHeaderSubject=true` fazem o componente usar os headers `To` e `Subject` da mensagem em vez de valores fixos na URI — é o que permite mandar para o e-mail de cada cliente.
* `contentType=RAW(text/html;charset=UTF-8)` precisa do `RAW(...)` porque o valor contém `;` e `=`, que a URI do Camel interpretaria como separadores de opção.
* Com o placeholder, trocar o Mailpit por um SMTP real (ou mudar o remetente) não exige recompilar a rota.
* Os links apontam para a `cotubify.s3.url-publica` (injetada no `EmailService` via `@Value`), que só funciona porque o bucket recebeu a política de leitura pública no script de init do LocalStack.

### 6. `cotubify-gerador`: Dead Letter Channel e retries

O que acontece se o S3 ou o SMTP estiverem fora do ar? Sem tratamento, a exception sobe, o Camel loga o erro e o pedido se perde — o cliente pagou e nunca recebe o ebook. A solução é o **Dead Letter Channel EIP**: tentar de novo algumas vezes e, se não der, mandar a mensagem para um canal separado em vez de descartá-la.

No Camel isso é um `errorHandler`, declarado no início do `configure()` e válido para todas as rotas do `RouteBuilder`:

```java
errorHandler(deadLetterChannel("spring-rabbitmq:cotubify.dlq?exchangeType=fanout")
                .maximumRedeliveries(3)
                .redeliveryDelay(2000)
                .useExponentialBackOff()
                .backOffMultiplier(2)
                .retryAttemptedLogLevel(LoggingLevel.WARN)
                .useOriginalMessage());
```

* **Redelivery:** até 3 novas tentativas, com backoff exponencial — esperas de 2s, 4s e 8s. Cada tentativa é logada em `WARN`. O redelivery é feito pelo próprio Camel, **no ponto em que falhou** (não reprocessa a rota desde o início nem devolve a mensagem para o RabbitMQ).
* **Dead Letter Channel:** esgotadas as tentativas, a mensagem é publicada na exchange `cotubify.dlq`, que faz o fanout para a fila `gerador.dlq` (ambas criadas pelo [rabbitmq/definitions-rabbit.json](file:///Users/alexandreaquiles/Documents/unipds/projetos-lives/live-apache-camel/rabbitmq/definitions-rabbit.json)). Como o erro foi tratado, a mensagem é confirmada (*ack*) na `gerador.queue` — ela não fica em loop.
* **`useOriginalMessage()`:** no momento da falha o body já foi transformado (um `Map`, um `GeracaoEbookRequest`, um `byte[]` de PDF...). Com essa opção, o que vai para a DLQ é a **mensagem como chegou** do RabbitMQ — o JSON do pedido. Assim ela pode ser reprocessada sem nenhuma conversão.

E o Splitter? Cada item do split é uma sub-mensagem com sua própria *unit of work*. Por isso a rota usa:

* **`shareUnitOfWork()`**: a sub-mensagem compartilha a unit of work do pedido. É o que faz o `useOriginalMessage()` devolver o **pedido inteiro** (e não só o item que falhou) e faz a falha de um item ser tratada como falha do pedido.
* **`stopOnException()`**: se um item falhar, o split para e a exception é propagada, em vez de seguir com os demais e mandar um e-mail incompleto.

Para ver funcionando, derrube o LocalStack e faça um pedido:

```bash
docker compose stop localstack
```

O log do `cotubify-gerador` mostra as tentativas em `WARN` e, depois de ~14s, a mensagem aparece na `gerador.dlq` (aba **Queues** da RabbitMQ UI). Para reprocessar, suba o LocalStack de novo (`docker compose start localstack`), abra a `gerador.dlq` na UI e use **Move messages** (do plugin Shovel) com destino `gerador.queue`: o gerador consome o pedido original e segue o fluxo normalmente.

---

## 📽️ Conteúdo Abordado

### Live 1 — Introdução ao Apache Camel

A primeira live cobriu desde a fundação conceitual até a aplicação prática em arquitetura de microsserviços.

1. **Introdução Teórica aos Padrões de Integração Corporativa (EIPs)**
   * O que é o Apache Camel e por que ele é considerado o "canivete suíço" da integração de sistemas?
   * Os conceitos fundamentais: `CamelContext`, `Routes` (Rotas), `Endpoints`, `DSL` (Java/XML/YAML) e `Components`.
2. **Explorando a Loja Cotubify**
   * Análise do estado inicial (Branch `main`).
   * Inicialização da infraestrutura do Docker (MySQL + RabbitMQ).
3. **Mão na Massa: O Lado Produtor (Spring Boot + Camel)**
   * Adicionando as dependências do Camel Spring Boot.
   * Utilizando o `ProducerTemplate` no controller da loja.
   * Criando uma rota (`RouteBuilder`) para formatar e publicar em filas RabbitMQ.
4. **Mão na Massa: O Lado Consumidor (Quarkus + Camel)**
   * Entendendo o ecossistema Quarkus Extension para Camel.
   * Criando a rota de consumo de fila via AMQP 1.0.
   * **Message Transformation:** Mapeando as discrepâncias de formato entre a Loja (Spring) e o Notas (Quarkus) usando Camel DSL e `Processor`.
5. **Simulação Completa do Fluxo**
   * Enviar requisição para a loja, verificar o log de publicação, checar o console do RabbitMQ e visualizar a nota fiscal gerada em tempo real no console do Quarkus.

### Live 2 (29/07/2026) — Publish-Subscribe e Splitter

Integração do terceiro microsserviço (**`cotubify-gerador`**), com o cenário: o compilador de Ebooks também precisa receber os dados do pedido assim que uma venda for concluída, para iniciar a compilação do PDF/EPUB.

1. **O problema do ponto-a-ponto**
   * A rota da Loja apontava direto para a `notas.queue` (`routingKey=notas`). Adicionar um segundo consumidor exigiria mudar o produtor.
2. **Publish-Subscribe Channel**
   * Troca da exchange para o tipo **fanout** (`spring-rabbitmq:cotubify.orders?exchangeType=fanout`): a loja publica um evento e o broker faz o broadcast.
   * Criação da `gerador.queue` com binding na mesma exchange — nenhuma linha alterada no consumidor de notas. A topologia final está exportada em [definitions-rabbit.json](file:///Users/alexandreaquiles/Documents/unipds/projetos-lives/live-apache-camel/rabbitmq/definitions-rabbit.json).
3. **Camel no `cotubify-gerador` (Spring Boot)**
   * Adição do `camel-spring-boot-starter`, `camel-spring-rabbitmq-starter` e `camel-jackson-starter` (BOM `4.21.0`).
   * Rota consumindo com `autoDeclare=false`, já que a topologia é criada previamente no broker.
4. **Splitter EIP**
   * Um pedido tem N ebooks, mas o gerador processa um por vez: `.split(simple("${body[items]}")).parallelProcessing()`.
   * Uso de **headers** para preservar os dados do pedido (Correlation Identifier) durante o split, e log de conclusão após o `.end()`.
5. **Fechando o Message Translator do `cotubify-notas`**
   * Mapeamento da lista de `items` (com o `ebook` aninhado) para os `ItemPedidoDTO`, completando o cálculo do valor total da nota.

### Live 3 (26/08/2026) — Claim Check, S3 e notificação por e-mail

O `cotubify-gerador` deixou de ser uma simulação: agora ele **gera os binários de verdade**, guarda os arquivos num S3 e avisa o cliente por e-mail. O cenário: terminada a compilação, o ebook precisa ficar disponível para download e o comprador precisa receber o link.

1. **Gerando os arquivos de verdade**
   * Fim do `Thread.sleep(2000)`. O `GeradorEbookService` monta um HTML com os dados do pedido e o converte para **PDF** (iText `html2pdf`) e **EPUB** (`epublib`), devolvendo `byte[]` em `gerarPdf` e `gerarEpub`.
   * Detalhes de dependência: o `epublib-core` vem de um repositório fora do Maven Central (`psiegman-repo`) e precisa da exclusão do `slf4j-simple`.
2. **Infra local: LocalStack e Mailpit**
   * `localstack` (`SERVICES: s3`) na porta `4566`, com o bucket `cotubify-ebooks` criado no boot pelo script em `localstack/init/ready.d` — inclusive com `bucket-policy` de leitura pública, para o link do e-mail abrir no navegador.
   * `mailpit` como SMTP de teste (`1025`) com UI web (`8025`), para ver os e-mails sem mandar nada para fora.
3. **Claim Check EIP**
   * O problema: o componente `aws2-s3` sobe **o body**, então gerar o PDF destrói o `GeracaoEbookRequest` que ainda seria necessário para o EPUB.
   * A solução: `.claimCheck(ClaimCheckOperation.Push)` antes de transformar o body e `.claimCheck(ClaimCheckOperation.Pop)` depois do upload — duas vezes, uma por formato.
4. **Componente `aws2-s3`**
   * `camel-aws2-s3-starter`, URI vinda de *property placeholder* (`{{cotubify.s3.uri}}`) com credenciais fake, `overrideEndpoint`/`uriEndpointOverride` para o LocalStack e `forcePathStyle=true`.
   * Chave do objeto montada com os headers do pedido: `pedidos/{pedidoId}/{ebookId}.pdf`.
5. **Componente `camel-mail`**
   * `camel-mail-starter`, corpo HTML montado com `simple()` e envio via `{{cotubify.smtp.uri}}` (Mailpit em `smtp://localhost:1025`).
   * `useHeaderRecipients` / `useHeaderSubject` para usar os headers `To` e `Subject`, e `RAW(...)` no `contentType` por causa dos caracteres especiais na URI.
6. **Simulação completa**
   * Um `POST /api/orders` com dois itens → nota fiscal em XML no `cotubify-notas`, dois PDFs e dois EPUBs no bucket do LocalStack e dois e-mails na UI do Mailpit.

### Live 4 (28/10/2026) — Aggregator e Dead Letter Channel

Dois problemas do `cotubify-gerador` que ficaram da Live 3: o cliente recebia **um e-mail por ebook** e, se o S3 ou o SMTP falhassem, o pedido **se perdia**.

1. **Aggregator EIP**
   * O problema: o e-mail era enviado dentro do split, então um pedido com dois ebooks gerava dois e-mails.
   * O Aggregator embutido no Splitter: `.split(expressão, AggregationStrategies.flexible().accumulateInCollection(ArrayList.class))` junta os `GeracaoEbookRequest` numa `List`, que vira o body depois do `.end()`.
   * O envio do e-mail saiu de dentro do split: agora é **um e-mail por pedido**, com um link de PDF e EPUB por ebook.
2. **`EmailService` e bean binding**
   * O HTML do e-mail saiu do `simple()` e foi para um `@Service` que recebe a `List<GeracaoEbookRequest>` e devolve a `String` do corpo, chamado com `.bean(emailService, "montarCorpoDoEmail")`.
   * `.removeHeaders("*", "To", "Subject", "From", "pedidoId")` para não vazar os headers internos da rota (como o CPF) no cabeçalho do e-mail.
3. **Dead Letter Channel EIP**
   * `errorHandler(deadLetterChannel("spring-rabbitmq:cotubify.dlq?exchangeType=fanout"))` com 3 redeliveries, backoff exponencial (2s, 4s, 8s) e log em `WARN`.
   * `useOriginalMessage()` para a DLQ receber o JSON do pedido como chegou, pronto para ser reprocessado.
   * `shareUnitOfWork()` e `stopOnException()` no split, para a falha de um item virar falha do pedido inteiro.
4. **Topologia e plugins do RabbitMQ**
   * Arquivos do broker movidos para a pasta `rabbitmq/`.
   * Exchange `cotubify.dlq` (fanout) e fila `gerador.dlq` no `definitions-rabbit.json`.
   * `enabled_plugins` com o **Shovel**, para mover as mensagens da `gerador.dlq` de volta para a `gerador.queue` pela UI.
5. **Simulação completa**
   * Um `POST /api/orders` com dois itens → dois PDFs e dois EPUBs no bucket e **um** e-mail no Mailpit.
   * Com o LocalStack parado → retries no log, pedido na `gerador.dlq`; LocalStack de volta + **Move messages** → pedido reprocessado e e-mail enviado.

---

## 🏆 Desafio: Idempotent Consumer

A Live 4 criou um jeito fácil de **reprocessar** mensagens: mover da `gerador.dlq` para a `gerador.queue`. Mas reprocessar abre uma porta perigosa: e se a mesma mensagem chegar duas vezes?

Isso acontece mais do que parece:

* O RabbitMQ garante entrega **at-least-once**: se o `cotubify-gerador` cair depois de mandar o e-mail mas antes de confirmar (*ack*) a mensagem, o broker a entrega de novo quando ele voltar.
* Alguém pode mover para a `gerador.queue` uma mensagem da DLQ que, na verdade, já tinha sido reprocessada.
* A loja pode publicar o mesmo pedido duas vezes (um retry no produtor, por exemplo).

Para ver o problema hoje, abra a `gerador.queue` na RabbitMQ UI e use **Publish message** para publicar duas vezes o mesmo JSON de pedido: o Mailpit vai mostrar **dois e-mails idênticos** para o cliente. Os uploads no S3 sobrescrevem a mesma chave e não causam estrago, mas o e-mail duplicado é um efeito colateral visível.

A solução é o **Idempotent Consumer EIP** (no livro do Hohpe e Woolf, [Idempotent Receiver](https://www.enterpriseintegrationpatterns.com/patterns/messaging/IdempotentReceiver.html)): o consumidor guarda um identificador de cada mensagem já processada e descarta as repetidas. No Camel, é o [`idempotentConsumer`](https://camel.apache.org/components/latest/eips/idempotentConsumer-eip.html), que recebe uma expressão para a chave e um `IdempotentRepository`.

**O desafio:** fazer o `cotubify-gerador` processar cada pedido **uma única vez**.

Algumas perguntas para guiar a solução:

1. **Qual é a chave?** O `pedidoId` já é o nosso Correlation Identifier e está num header logo depois do `unmarshal`. Ele é suficiente?
2. **Onde colocar o `idempotentConsumer` na rota?** Antes do split? Envolvendo o quê, até onde vai o `.end()`?
3. **Qual repositório usar?** O `MemoryIdempotentRepository` é o mais simples, mas o que acontece com as chaves quando o `cotubify-gerador` reinicia — justamente o cenário do redelivery do RabbitMQ? Compare com um repositório persistente, como o `FileIdempotentRepository`, um JDBC (o MySQL já está no `docker-compose.yml`) ou um Redis.
4. **E a interação com o Dead Letter Channel?** Se um pedido falhar e for para a `gerador.dlq`, a chave dele fica no repositório? Ao mover a mensagem de volta para a `gerador.queue`, ela será reprocessada ou descartada como duplicada? Investigue as opções `eager` e `removeOnFailure` do `idempotentConsumer` e teste com o LocalStack parado.
5. **Duplicata é erro?** Quer só ignorar em silêncio, ou registrar um log? Veja a opção `skipDuplicate` e a propriedade `CamelDuplicateMessage` da exchange.

Para validar: publique o mesmo pedido duas vezes na `gerador.queue` e confira que só **um** e-mail chegou no Mailpit — inclusive com um restart do `cotubify-gerador` entre as duas publicações.
