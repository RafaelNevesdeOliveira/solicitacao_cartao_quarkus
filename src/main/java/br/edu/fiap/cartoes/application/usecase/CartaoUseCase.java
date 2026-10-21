package br.edu.fiap.cartoes.application.usecase;
import br.edu.fiap.cartoes.presentation.dto.CartaoResponse;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

import br.edu.fiap.cartoes.domain.exception.EtapaNaoImplementadaException;
/**
 * Camada de aplicação a implementar: coordena uma operação, suas regras e transação.
 * O controller já chama estas assinaturas. Na migração para Clean, substituam
 * a dependência de DTO HTTP por entradas/saídas da aplicação e portas de saída.
 */
@ApplicationScoped
public class CartaoUseCase {
    /** JPQL com relacionamento cartão -> solicitação -> usuário e tipo; somente cartões próprios. */
    public List<CartaoResponse> listar(Long usuarioId) {
        // TODO: implementar sem colocar SQL ou regras no controller.
        throw new EtapaNaoImplementadaException("CartaoUseCase.listar");
    }
}
