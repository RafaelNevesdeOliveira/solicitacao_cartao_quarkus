
package br.edu.fiap.cartoes.application.usecase;

import br.edu.fiap.cartoes.presentation.dto.LoginRequest;
import br.edu.fiap.cartoes.presentation.dto.TokenResponse;
import br.edu.fiap.cartoes.domain.model.Usuario;
import br.edu.fiap.cartoes.domain.exception.CredenciaisInvalidasException;
import br.edu.fiap.cartoes.application.port.UsuarioRepository;
import br.edu.fiap.cartoes.infrastructure.security.JwtService;
import br.edu.fiap.cartoes.infrastructure.security.SenhaBCrypt;
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
