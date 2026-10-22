package br.edu.fiap.cartoes.application.usecase;

import br.edu.fiap.cartoes.domain.exception.EmailJaCadastradoException;
import br.edu.fiap.cartoes.presentation.dto.UsuarioRequest;
import br.edu.fiap.cartoes.presentation.dto.UsuarioResponse;
import br.edu.fiap.cartoes.domain.model.Usuario;
import br.edu.fiap.cartoes.application.port.UsuarioRepository;
import br.edu.fiap.cartoes.infrastructure.security.SenhaBCrypt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Locale;

@ApplicationScoped
public class UsuarioUseCase {

    private final UsuarioRepository usuario;
    private final SenhaBCrypt senhas;

    @Inject
    public UsuarioUseCase(
            UsuarioRepository usuario,
            SenhaBCrypt senhas
    ) {
        this.usuario = usuario;
        this.senhas = senhas;
    }

    /**
     * Normaliza o e-mail, verifica duplicidade,
     * gera o hash BCrypt, persiste o usuario
     * e retorna a resposta sem a senha.
     */
    public UsuarioResponse cadastrar(UsuarioRequest request) {

        String emailNormalizado = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (usuario.buscarPorEmail(emailNormalizado).isPresent()) {
            throw new EmailJaCadastradoException(emailNormalizado);
        }

        String hash = senhas.criarHash(request.senha());

        Usuario novoUsuario = new Usuario(
                request.nome(),
                emailNormalizado,
                hash
        );

        Usuario usuarioSalvo = usuario.salvar(novoUsuario);

        return UsuarioResponse.de(usuarioSalvo);
    }
}
