
package br.edu.fiap.cartoes.usecase;

import br.edu.fiap.cartoes.dto.LoginRequest;
import br.edu.fiap.cartoes.dto.TokenResponse;
import br.edu.fiap.cartoes.entity.Usuario;
import br.edu.fiap.cartoes.exception.CredenciaisInvalidasException;
import br.edu.fiap.cartoes.repository.UsuarioRepository;
import br.edu.fiap.cartoes.security.JwtService;
import br.edu.fiap.cartoes.security.SenhaBCrypt;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Coordena a autenticacao do usuario.
 */
@ApplicationScoped
public class AutenticacaoUseCase {

    private final UsuarioRepository repository;
    private final SenhaBCrypt senha;
    private final JwtService token;

    public AutenticacaoUseCase(
            UsuarioRepository repository,
            SenhaBCrypt senha,
            JwtService token
    ) {
        this.repository = repository;
        this.senha = senha;
        this.token = token;
    }

    public TokenResponse login(LoginRequest request) {

        Usuario usuario = repository.buscarPorEmail(request.email())
                .orElseThrow(CredenciaisInvalidasException::new);

        if (!senha.confere(request.senha(), usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }

        return token.emitir(usuario);
    }
}
