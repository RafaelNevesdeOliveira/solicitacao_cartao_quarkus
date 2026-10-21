# Matriz de Contratos - Dev 5

Objetivo: validar rapidamente se implementacoes dos Devs 1-4 respeitam o contrato HTTP do Swagger, do desafio e do `requests.http`.

## Checklist de validacao

- [ ] Todas as 9 rotas existem com os mesmos paths e metodos.
- [ ] As 7 rotas protegidas exigem JWT e retornam `401` sem token.
- [ ] Nenhuma rota obrigatoria retorna `501 TODO` apos entrega dos devs.
- [ ] Corpos e campos seguem os DTOs em `src/main/java/br/edu/fiap/cartoes/dto`.
- [ ] Erros de regra/404/entrada usam formato de `ErroResponse`.
- [ ] Nao ha senha/hash em respostas de cadastro/login/listagens.

## Matriz por rota

| # | Metodo e rota | Auth | Request esperado | Sucesso | Erros esperados | Validacao rapida |
|---|---|---|---|---|---|---|
| 1 | `POST /api/usuarios` | Publica | `UsuarioRequest` (`nome`, `email`, `senha`) | `201` + `UsuarioResponse` | `400`, `409` | Sem `senha`/`senhaHash` na resposta |
| 2 | `POST /api/auth/login` | Publica | `LoginRequest` (`email`, `senha`) | `200` + `TokenResponse` | `400`, `401` | `tokenType=Bearer`, `expiresIn`, `expiresAt` |
| 3 | `GET /api/tipos-cartao` | JWT | sem body | `200` + `List<TipoCartaoResponse>` | `401` | Lista apenas disponiveis, ordenada por nome |
| 4 | `POST /api/solicitacoes` | JWT | `SolicitacaoRequest` (`tipoCartaoId`) | `201` + `SolicitacaoResponse` | `400`, `401`, `404`, `409` | Nao aceitar `usuarioId` no body |
| 5 | `GET /api/solicitacoes?status=PENDENTE` | JWT | query `status` opcional | `200` + `List<SolicitacaoResponse>` | `400`, `401` | Retorna apenas do titular do token |
| 6 | `GET /api/solicitacoes/{id}` | JWT | `id` positivo | `200` + `SolicitacaoResponse` | `400`, `401`, `404` | Mesmo `404` para inexistente e outro titular |
| 7 | `PATCH /api/solicitacoes/{id}/cancelar` | JWT | `id` positivo | `200` + `SolicitacaoResponse` | `400`, `401`, `404`, `409` | Apenas `PENDENTE -> CANCELADA` |
| 8 | `POST /api/solicitacoes/{id}/emitir` | JWT | `id` positivo | `201` + `CartaoResponse` | `400`, `401`, `404`, `409` | Emissao atomica + sem segundo cartao |
| 9 | `GET /api/cartoes` | JWT | sem body | `200` + `List<CartaoResponse>` | `401` | Apenas cartoes do titular |

## Contratos de erro

Formato padrao de erro (via `GlobalExceptionMapper`):

- `timestamp`
- `status`
- `codigo`
- `mensagem`
- `path`

Mapeamento esperado:

- `400` entrada/parametro invalido
- `401` sem token/token invalido/credencial invalida
- `404` recurso inexistente ou sem titularidade
- `409` regra de negocio (estado invalido, duplicidade, tipo indisponivel)
- `500` falha inesperada sem vazar SQL/stacktrace

## Casos de validacao rapida (manual)

1. Cadastro valido -> `201` e sem senha no retorno.
2. Login invalido -> `401` com mensagem generica.
3. Rota protegida sem token -> `401`.
4. Pedido duplicado pendente (mesmo titular e tipo) -> `409`.
5. Token de usuario B lendo pedido de usuario A -> `404`.
6. Cancelar duas vezes o mesmo pedido -> `409` na segunda.
7. Emitir duas vezes a mesma solicitacao -> `409` na segunda.

## Relacao com testes atuais

- Integracao de seguranca ja implementada em `src/test/java/br/edu/fiap/cartoes/controller/SegurancaIntegracaoTest.java`.
- Contratos OpenAPI e validacao de entrada implementados em `src/test/java/br/edu/fiap/cartoes/controller/ContratoApiIntegracaoTest.java`.
- Testes de regra da entidade e use case estao preparados e desabilitados ate entrega dos Devs 3/4:
  - `src/test/java/br/edu/fiap/cartoes/entity/SolicitacaoCartaoTest.java`
  - `src/test/java/br/edu/fiap/cartoes/usecase/SolicitacaoUseCaseTest.java`

## Achado atual da casca

- Hoje, erros `400` de Bean Validation estao retornando no formato nativo `Constraint Violation` do Quarkus (`title`, `status`, `violations`) e nao no formato `ErroResponse`.
- Esse comportamento foi validado por teste de integracao e deve ser decidido pelo time: manter como esta ou padronizar para `ErroResponse` tambem nos `400` de validacao.

## Quando os outros devs entregarem

- Remover `@Disabled` dos testes de regra.
- Ajustar asserts de mensagens/codigos conforme implementacao final.
- Executar `mvn test` e confirmar `0` falhas.
- Rodar `requests.http` completo sem `501`.

