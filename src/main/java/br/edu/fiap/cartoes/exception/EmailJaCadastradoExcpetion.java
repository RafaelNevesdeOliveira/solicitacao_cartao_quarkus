package br.edu.fiap.cartoes.exception;

public class EmailJaCadastradoExcpetion extends RuntimeException {
    public EmailJaCadastradoExcpetion() { super("E-mail já cadastrado"); }
}
