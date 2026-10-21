package br.edu.fiap.cartoes.usecase;

import br.edu.fiap.cartoes.dto.*;
import br.edu.fiap.cartoes.entity.TipoCartao;
import br.edu.fiap.cartoes.repository.TipoCartaoRepositoryJdbc;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

/**
 * Camada de aplicação a implementar: coordena uma operação, suas regras e transação.
 * O controller já chama estas assinaturas. Na migração para Clean, substituam
 * a dependência de DTO HTTP por entradas/saídas da aplicação e portas de saída.
 */
@ApplicationScoped
public class TipoCartaoUseCase {
    /** Delegar à única consulta JDBC e mapear a resposta. */

    @Inject TipoCartaoRepositoryJdbc tipoCartaoRepositoryJdbc;

    @Transactional
    public List<TipoCartaoResponse> listarDisponiveis() {
        // TODO: implementar sem colocar SQL ou regras no controller.
        return tipoCartaoRepositoryJdbc.listarDisponiveisPorNome().stream()
                .map(this::resposta).toList();
    }

    private TipoCartaoResponse resposta(TipoCartao cartao) {
        return new TipoCartaoResponse(
                cartao.getId(),
                cartao.getNome(),
                cartao.getDescricao(),
                cartao.isDisponivel()
        );
    }
}
