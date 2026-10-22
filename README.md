# Desafio Solicitação de Cartões

Implementação colaborativa do desafio da agência fictícia Aurora, inspirada na agência bancária Quarkus. JDK **25.0.2**, Quarkus **3.40.1**, Maven, JPA com EntityManager, JDBC, JWT HS256 e BCrypt.

A API implementa cadastro, autenticação, consulta de tipos, criação e acompanhamento de solicitações, cancelamento, emissão e listagem de cartões. As nove rotas do enunciado foram preservadas. A aplicação usa H2 por padrão e oferece um perfil opcional PostgreSQL.

**Estado de validação:** compilação e testes automatizados existentes concluídos com sucesso (`.\mvnw.cmd clean test` no Windows). Foram realizados testes manuais de autenticação, autorização e regras de negócio. Ainda faltam evidências automatizadas específicas de concorrência, rollback forçado e execução no PostgreSQL.

O enunciado para os alunos está em `../documentos/Desafio_Solicitacao_de_Cartoes.docx`.

## História e escopo

A agência fictícia Aurora quer permitir que seus clientes solicitem um cartão, acompanhem o pedido e simulem a emissão. Cada usuário só consulta e altera seus próprios pedidos.

Não é um produto bancário real. Não há análise de crédito, aprovação administrativa, pagamento, número de cartão, CVV, bandeira ou integração externa. A emissão pelo próprio titular é uma simplificação pedagógica.

## Implementação realizada

| Área | Implementação |
| --- | --- |
| API REST | 5 controllers e 9 rotas, preservando os contratos HTTP do desafio |
| Segurança | Cadastro com senha BCrypt, login JWT HS256 e sete rotas protegidas |
| Persistência | JPA com EntityManager para usuários, solicitações e cartões; JDBC para listar tipos disponíveis |
| Regras de negócio | Titularidade, validação de tipo, prevenção de pendência duplicada, cancelamento e emissão |
| Concorrência | Bloqueio pessimista na criação por usuário e nas transições de cancelamento/emissão por solicitação |
| Arquitetura | Separação entre domínio, casos de uso, portas, infraestrutura e apresentação, com concessão didática de anotações JPA no modelo |
| Testes | Testes unitários existentes aprovados; cenários manuais de API executados |

## Rodar sem Docker

Dentro desta pasta:

```bash
java -version
./mvnw quarkus:dev
```

No Windows PowerShell: `./mvnw.cmd quarkus:dev`. JDK homologado: 25.0.2. No primeiro uso, Maven precisa de internet para baixar dependências.

- API: http://localhost:8082
- Swagger: http://localhost:8082/swagger-ui/
- OpenAPI: http://localhost:8082/openapi

O **H2 é o padrão**, em memória: a aplicação cria tabelas e carga; os dados se perdem quando o processo termina. Ela também abre um servidor TCP H2 em **9093**, apenas para acesso local.

Conexão no DBeaver ou IntelliJ:
- Driver H2; URL: `jdbc:h2:tcp://localhost:9093/mem:cartoes`
- Usuário: `sa`; senha vazia.
- Conectar **depois** que a API iniciar. Não usar outra conexão `jdbc:h2:mem:cartoes` fora do processo: seria outro banco.
- Não há console web H2 neste exercício. PostgreSQL/pgAdmin não se conectam a H2.
- Se a porta estiver ocupada: `./mvnw quarkus:dev -Dapp.h2.tcp.port=9094`; ajuste a URL do cliente.
- Para não abrir TCP: `-Dapp.h2.tcp.enabled=false`. O H2 embutido continua funcionando.

### PostgreSQL local

A API **não instala nem inicia um servidor PostgreSQL**. Inicie seu PostgreSQL local. No pgAdmin ou DBeaver, conectado ao banco `postgres`, execute **isoladamente**, com autocommit, o arquivo `sql/00_criar_database_postgres.sql`. Não execute CREATE DATABASE dentro de BEGIN.

Depois execute:

```bash
./mvnw quarkus:dev -Dquarkus.profile=postgres
```

Padrão do perfil: `jdbc:postgresql://localhost:5432/solicitacao_cartoes`, usuário `postgres`, senha `postgres`. Ajuste no arquivo `src/main/resources/application.properties` ou por ambiente:

```bash
export DB_URL='jdbc:postgresql://localhost:5432/solicitacao_cartoes'
export DB_USER='postgres'
export DB_PASSWORD='sua-senha-local'
./mvnw quarkus:dev -Dquarkus.profile=postgres
```

No PowerShell: `$env:DB_URL='jdbc:postgresql://localhost:5432/solicitacao_cartoes'`, `$env:DB_USER='postgres'`, `$env:DB_PASSWORD='sua-senha-local'`.

**Escolha um banco por execução.** H2 e PostgreSQL não são réplicas nem recebem gravações simultâneas. No perfil PostgreSQL, o TCP H2 fica desabilitado. Compile novamente com o perfil adequado ao empacotar: `./mvnw package -Dquarkus.profile=postgres`.

### SQL pronto

1. `sql/00_criar_database_postgres.sql`: manual, somente PostgreSQL, uma vez.
2. `src/main/resources/db/01_criar_tabelas.sql`: automático no startup.
3. `src/main/resources/db/02_carga_inicial.sql`: automático no startup, sem redefinir registros existentes.
4. `sql/03_carga_relacionamentos_opcional.sql`: manual, opcional, depende do cadastro aluno@example.com.
5. `sql/04_consultas_conferencia.sql`: SELECTs para verificar os relacionamentos.

O `DatabaseInitializer` executa 01 e 02 no banco ativo, em transação. Não usa Hibernate para criar tabelas e não precisa de Docker. É um inicializador de laboratório, **não** um mecanismo de evolução de esquema em produção. IF NOT EXISTS não corrige tabelas antigas.

Carga em uma base vazia: **2 usuários, 3 tipos, 3 solicitações e 1 cartão**. Usuários fictícios:
- `ana@example.com`
- `bruno@example.com`
- Senha pública de ambos: `Cartoes@123` (hash BCrypt custo 12 na carga).
- O login e a verificação BCrypt estão implementados.

A carga usa e-mail/nome para relacionar os registros e não presume IDs. Use um novo usuário no roteiro do grupo para não disputar pedidos da demonstração.

## As nove rotas

Todas retornam JSON. Use `Authorization: Bearer <accessToken>` nas sete rotas protegidas.

| Nº | Método e rota | Acesso | Persistência | Sucesso |
| --- | --- | --- | --- | --- |
| 1 | POST /api/usuarios | Pública | JPA | 201 |
| 2 | POST /api/auth/login | Pública | JPA na consulta de usuário | 200 |
| 3 | GET /api/tipos-cartao | Protegida | **JDBC**, única rota | 200 |
| 4 | POST /api/solicitacoes | Protegida | JPA | 201 |
| 5 | GET /api/solicitacoes?status=PENDENTE | Protegida | JPA com JPQL própria | 200 |
| 6 | GET /api/solicitacoes/{id} | Protegida | JPA | 200 |
| 7 | PATCH /api/solicitacoes/{id}/cancelar | Protegida | JPA | 200 |
| 8 | POST /api/solicitacoes/{id}/emitir | Protegida | JPA | 201 |
| 9 | GET /api/cartoes | Protegida | JPA com JPQL própria | 200 |

Sem token, token inválido ou expirado: **401**. Não há perfis administrador/cliente. Autenticação é comum; a autorização por **titularidade** é aplicada nos casos de uso e repositórios.

Pedidos inexistentes **e** de outro titular retornam **404**. Não aceitar usuarioId no JSON: ele vem do claim assinado. Listas sem resultados retornam 200 com `[]`.

## Entidades e comportamentos

Quatro entidades, sem entidade adicional de endereço ou item.

| Entidade | Propriedades, incluindo ID | Comportamentos |
| --- | --- | --- |
| Usuario | id Long, nome String, email String, senhaHash String, criadoEm Instant | alterarNome; atualizarSenhaHash |
| TipoCartao | id Long, nome String, descricao String, disponivel boolean | alterarDisponibilidade |
| SolicitacaoCartao | id Long, usuario Usuario, tipoCartao TipoCartao, solicitadaEm Instant, status StatusSolicitacao | cancelar; marcarEmitida |
| Cartao | id Long, solicitacao SolicitacaoCartao, emitidoEm Instant, status StatusCartao | estaAtivo |

Usuario 1:N SolicitacaoCartao; TipoCartao 1:N SolicitacaoCartao; SolicitacaoCartao 1:0..1 Cartao. As FKs ficam nos lados de solicitação e cartão. A FK solicitacao_id do cartão é UNIQUE.

Os construtores de cadastro já atribuem propriedades: usuário recebe hash, solicitação nasce PENDENTE e cartão nasce ATIVO. As validações de negócio são aplicadas nos comportamentos e casos de uso. O construtor protegido sem argumentos existe para o JPA.

Os setters foram entregues para acompanhar o padrão da turma, mas o use case **não pode** usá-los para contornar cancelar/marcarEmitida. Senha e hash nunca saem nos DTOs de resposta.

### Regras obrigatórias

1. Cadastro: nome não vazio, e-mail válido e único após trim/minúsculas; senha de 8 a 72 caracteres e no máximo 72 **bytes UTF-8**. Salvar BCrypt, nunca texto.
2. Login: buscar e-mail normalizado; usar matches; erro genérico 401 tanto para usuário ausente quanto para senha errada.
3. Tipos: listar apenas disponíveis, por nome, via JDBC.
4. Criar pedido: titular precisa existir, tipo precisa existir (404) e estar disponível (409).
5. Não permitir outro pedido **PENDENTE** para o mesmo titular e tipo (409). Depois de cancelado ou emitido, uma nova solicitação é permitida.
6. Listar e detalhar: retornar somente registros do usuário autenticado. Status inválido na query gera 400.
7. Cancelar: somente PENDENTE vira CANCELADA. Cancelar novamente ou cancelar EMITIDA gera 409.
8. Emitir: somente PENDENTE, do próprio titular. Criar cartão ATIVO e alterar pedido para EMITIDA na **mesma transação**. Erro em qualquer gravação deve desfazer ambas.
9. Uma solicitação só pode gerar um cartão. Repetir emissão gera 409.
10. Cartões: listar somente os próprios, trazendo nome do tipo por relacionamento, sem expor entidade inteira.

Para concorrência, a implementação usa bloqueio de escrita da solicitação ao emitir e cancelar, dentro da transação. Para impedir dois pedidos pendentes simultâneos, bloqueia o usuário antes de conferir duplicidade e inserir. O SQL já impede dois cartões para a mesma solicitação; tratem a violação esperada sem vazar SQL. Um catch genérico não deve esconder falhas desconhecidas como 409.

## Organização da implementação

O pacote-base é `br.edu.fiap.cartoes`. As responsabilidades foram distribuídas entre:

1. **Domínio:** entidades e transições de estado (`PENDENTE`, `CANCELADA`, `EMITIDA` e cartão `ATIVO`).
2. **Aplicação:** casos de uso que coordenam validações, titularidade e transações.
3. **Portas:** contratos de repositório e de consulta usados pelos casos de uso.
4. **Infraestrutura:** adaptadores JPA/JDBC, BCrypt, JWT e inicialização do banco.
5. **Apresentação:** controllers REST, DTOs e tratamento de erros HTTP.

A consulta de tipos disponíveis é feita via JDBC; a busca do tipo por ID usada na criação da solicitação permanece em JPA. A criação de solicitações bloqueia o registro do usuário; cancelamento e emissão bloqueiam a solicitação. Essas estratégias precisam ser verificadas também com testes concorrentes reais.

### O que observar nas queries

- JDBC: SQL usa tabelas e colunas; obter conexão do Agroal, bind de disponivel, ordenar por nome e fechar Connection/PreparedStatement/ResultSet.
- JPA: JPQL usa **nomes de classes e atributos**, não nomes de tabelas. Buscar a solicitação com o tipo usando relacionamento.
- Listagem de solicitações: filtro obrigatório de usuario.id e opcional de status; ordenar por solicitadaEm e id.
- Listagem de cartões: navegar cartao.solicitacao.usuario e cartao.solicitacao.tipoCartao; incluir somente o titular atual.
- Não concatenar entrada do cliente em SQL/JPQL. Usar parâmetros nomeados/posicionais.
- Mapear DTO enquanto a transação de leitura está ativa ou carregar os relacionamentos explicitamente. Não depender de serializar proxies LAZY.

## Segurança preparada

Os controllers protegidos usam `@Authenticated`; cadastro e login usam `@PermitAll`. `UsuarioAtual` lê usuarioId de um token verificado. A configuração rejeita JWT com assinatura/issuer/expiração inválidos.

A emissão de JWT e o BCrypt são implementados pelos serviços de segurança do projeto, sem filtro JWT manual. Configurações de custo 12 e validade 900 segundos são escolhas do laboratório, não números obrigatórios para toda produção.

No Swagger, execute o login, copie só o accessToken e cole em **Authorize**. A UI monta o Bearer. Use HTTPS em ambiente real. A chave de desenvolvimento no properties e no jwt-secret.jwk é pública e não deve ser reutilizada fora da aula.

O JWK pronto guarda os mesmos bytes do segredo em Base64URL. Se alterar o segredo de assinatura por JWT_SECRET, forneça também um JWK correspondente via JWT_VERIFY_KEY_LOCATION. Alterar só um lado faz o token ser rejeitado. O laboratório já vem com o par coerente, sem exigir alteração para começar.

## Migração obrigatória para Clean Architecture

O projeto parte de camadas convencionais e foi organizado em responsabilidades de Clean Architecture sem mudar as rotas. A estrutura conceitual de referência é:

```text
domain/model                  comportamentos e estados
application/usecase           coordenação das operações
application/port              contratos de persistência e consulta
application/model             entradas e resultados sem tipos HTTP
infrastructure/persistence    JDBC, JPA e mapeamentos
infrastructure/security       BCrypt e JWT SmallRye
infrastructure/config         banco, bootstrap e configuração
presentation/controller       recursos HTTP
presentation/dto              contratos JSON/Swagger
presentation/exception        conversão de erros para HTTP
```

Não basta mover pastas: use cases dependem de interfaces internas, não de classes JDBC/JPA/JWT/BCrypt concretas; o controller converte o DTO para a entrada da aplicação. O domínio não importa controller ou DTO. Os adaptadores implementam as portas.

Para manter o esforço adequado, **não é obrigatório duplicar todas as entidades**: o grupo pode manter as anotações JPA no modelo como concessão didática, registrando essa decisão. Isso não é isolamento estrito de persistência. Quem escolher isolamento estrito cria modelo JPA e mapeadores próprios, sem criar novas entidades de negócio.

A documentação e os testes devem permitir conferir os mesmos contratos antes e depois da refatoração. Não usar a migração para inserir SQL no use case ou regra no controller.

## Roteiro de aceitação

Use um usuário novo para evitar colisões com os dados da carga.

1. Cadastrar aluno@example.com e outro usuário; retorno 201 sem senha/hash.
2. Login errado retorna 401; login correto retorna token Bearer.
3. Sem token, GET tipos retorna 401. Com token, retorna ESSENCIAL e VIAGEM, não EMPRESARIAL.
4. Criar pedido para tipo disponível retorna PENDENTE; repetir gera 409; tipo indisponível gera 409 e tipo inexistente gera 404.
5. Listar por status e detalhar o pedido; o token do segundo usuário não deve enxergar nem alterar o pedido do primeiro (404 no detalhe).
6. Cancelar: PENDENTE -> CANCELADA; repetir gera 409.
7. Criar outro pedido e emitir: cartão ATIVO e solicitação EMITIDA; repetir gera 409, sem segundo cartão.
8. Listar cartões do titular, conferir IDs e FKs nos SELECTs.
9. Forçar falha na emissão durante desenvolvimento e demonstrar rollback; remover qualquer gancho artificial antes de entregar.
10. Após a migração Clean, repetir o roteiro sem mudar JSONs e rotas.

Os testes automatizados existentes em `src/test/java` foram executados com sucesso. Entre os cenários unitários verificados estão cancelamento permitido, rejeição de cancelamento após emissão, emissão de pedido pendente, rejeição de reemissão, rejeição de pedido cancelado, duplicidade pendente e titularidade. Testes específicos de concorrência simultânea, rollback forçado e PostgreSQL permanecem pendentes.

## Critérios de entrega e pendências de validação

- Nove rotas conforme contrato; uma rota JDBC e as demais JPA, sem implementar novos endpoints.
- Comportamentos preenchidos; nenhuma chamada obrigatória permanece em 501.
- Hash BCrypt salvo e ausente das respostas; login e verificação JWT funcionando.
- Sete rotas protegidas e acesso restrito ao titular.
- Emissão atômica, estados válidos, duplicidade tratada e exceções coerentes.
- Consultas JPQL customizadas e parâmetros JDBC demonstrados.
- Relações conferidas no banco; dois usuários usados no teste de isolamento.
- Clean Architecture com portas e dependências explicadas; concessões registradas.
- Conferir Swagger e atualizar `requests.http` com exemplos reproduzíveis, sem commitar tokens reais.
- README registra as decisões de implementação e as validações ainda pendentes.

## Referências oficiais

- Quarkus ORM/JPA: https://quarkus.io/guides/hibernate-orm
- Datasources: https://quarkus.io/guides/datasource
- JWT: https://quarkus.io/guides/security-jwt e https://quarkus.io/guides/security-jwt-build
- OpenAPI: https://quarkus.io/guides/openapi-swaggerui
- BCrypt: BcryptUtil, módulo quarkus-elytron-security-common (mesmo da agência).