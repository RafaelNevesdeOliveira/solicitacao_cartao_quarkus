package br.edu.fiap.cartoes.domain.exception;

public class EmailJaCadastradoException extends RegraNegocioException {
    public EmailJaCadastradoException(String email) {super("O email " + email + " já está cadastrado");}
}
