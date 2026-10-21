package br.edu.fiap.cartoes.exception;

/**
 * Representa conflitos com as regras de negocio.
 *
 * Exemplos:
 * - Tipo de cartao indisponivel.
 * - Solicitacao pendente duplicada.
 *
 * Pode ser associada ao status HTTP 409
 * pela camada de tratamento de excecoes.
 */
public class ConflitoException extends RegraNegocioException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
