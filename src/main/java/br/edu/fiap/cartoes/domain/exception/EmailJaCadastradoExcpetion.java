package br.edu.fiap.cartoes.domain.exception;

public class EmailJaCadastradoExcpetion extends RuntimeException {
    public EmailJaCadastradoExcpetion() { super("E-mail já cadastrado"); }
}
