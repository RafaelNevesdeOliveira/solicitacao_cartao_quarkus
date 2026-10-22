package br.edu.fiap.cartoes.infrastructure.security;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class SenhaBCrypt {

    private static final int MIN_CARACTERES = 8;
    private static final int MAX_CARACTERES = 72;
    private static final int MAX_BYTES_UTF8 = 72;

    @ConfigProperty(name = "security.password.bcrypt-cost")
    int custo;

    public String criarHash(String senha) {
        validarSenha(senha);
        return BcryptUtil.bcryptHash(senha, custo);
    }

    public boolean confere(String senha, String hash) {
        if (senha == null || hash == null) {
            return false;
        }

        return BcryptUtil.matches(senha, hash);
    }

    private void validarSenha(String senha) {
        if (senha == null
                || senha.length() < MIN_CARACTERES
                || senha.length() > MAX_CARACTERES) {
            throw new IllegalArgumentException(
                    "A senha deve conter entre 8 e 72 caracteres."
            );
        }

        if (senha.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES_UTF8) {
            throw new IllegalArgumentException(
                    "A senha não pode ultrapassar 72 bytes em UTF-8."
            );
        }
    }
}
