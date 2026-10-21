package br.edu.fiap.cartoes.usecase;

import br.edu.fiap.cartoes.dto.UsuarioRequest;
import br.edu.fiap.cartoes.dto.UsuarioResponse;
import br.edu.fiap.cartoes.entity.Usuario;
import br.edu.fiap.cartoes.exception.EmailJaCadastradoExcpetion;
import br.edu.fiap.cartoes.repository.UsuarioRepository;
import br.edu.fiap.cartoes.security.SenhaBCrypt;
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
            throw new EmailJaCadastradoExcpetion();
        }

        String hash = senhas.criarHash(request.senha());

        Usuario novoUsuario = Usuario.novo(
                request.nome(),
                emailNormalizado,
                hash
        );

        Usuario usuarioSalvo = usuario.salvar(novoUsuario);

        return UsuarioResponse.de(usuarioSalvo);
    }
}
