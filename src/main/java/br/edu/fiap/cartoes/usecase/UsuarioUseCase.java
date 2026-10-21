package br.edu.fiap.cartoes.usecase;
import br.edu.fiap.cartoes.entity.Usuario;
import br.edu.fiap.cartoes.exception.RegraNegocioException;
import br.edu.fiap.cartoes.repository.UsuarioRepository;
import br.edu.fiap.cartoes.security.SenhaBCrypt;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import br.edu.fiap.cartoes.dto.*;
import br.edu.fiap.cartoes.entity.StatusSolicitacao;
import br.edu.fiap.cartoes.exception.EtapaNaoImplementadaException;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Camada de aplicação a implementar: coordena uma operação, suas regras e transação.
 * O controller já chama estas assinaturas. Na migração para Clean, substituam
 * a dependência de DTO HTTP por entradas/saídas da aplicação e portas de saída.
 */
@ApplicationScoped
public class UsuarioUseCase {
    @Inject
    UsuarioRepository usuarioRepository;
    @Inject
    SenhaBCrypt senhaBCrypt;

    /** Normalizar e-mail, rejeitar duplicado, gerar hash BCrypt, persistir e mapear sem senha. */
    @Transactional
    public UsuarioResponse cadastrar(UsuarioRequest request) {
        String emailNormalizado = request.email().trim().toLowerCase();

        if (usuarioRepository.buscarPorEmail(emailNormalizado).isPresent()) {
            throw new RegraNegocioException("E-mail já cadastrado no sistema.");
        }

        String senhaHash = senhaBCrypt.criarHash(request.senha());
        Usuario usuario = new Usuario(request.nome().trim(), emailNormalizado, senhaHash);

        usuarioRepository.salvar(usuario);

        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getCriadoEm());
    }
}
