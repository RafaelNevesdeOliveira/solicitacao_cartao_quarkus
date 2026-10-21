package br.edu.fiap.cartoes.usecase;
import br.edu.fiap.cartoes.entity.Usuario;
import br.edu.fiap.cartoes.exception.CredenciaisInvalidasException;
import br.edu.fiap.cartoes.repository.UsuarioRepository;
import br.edu.fiap.cartoes.security.JwtService;
import br.edu.fiap.cartoes.security.SenhaBCrypt;
import jakarta.enterprise.context.ApplicationScoped;
import br.edu.fiap.cartoes.dto.*;

/**
 * Camada de aplicação a implementar: coordena uma operação, suas regras e transação.
 * O controller já chama estas assinaturas. Na migração para Clean, substituam
 * a dependência de DTO HTTP por entradas/saídas da aplicação e portas de saída.
 */
@ApplicationScoped
public class AutenticacaoUseCase {
    /** Buscar e-mail normalizado, conferir BCrypt, negar genericamente ou emitir JWT. */
    private final UsuarioRepository repository;
    private final SenhaBCrypt senha;
    private final JwtService token;

    public AutenticacaoUseCase(UsuarioRepository repository, SenhaBCrypt senha, JwtService token) {
        this.repository = repository;
        this.senha = senha;
        this.token = token;
    }

    public TokenResponse login(LoginRequest request) {
        Usuario usuario = repository.buscaPorEmail(request.email())
                .orElseThrow(CredenciaisInvalidasException::new);

        if(!senha.confere(request.senha(), usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }

        return token.emitir(usuario);
    }
}
