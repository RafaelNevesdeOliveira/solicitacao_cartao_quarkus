
package br.edu.fiap.cartoes.domain.model;

import br.edu.fiap.cartoes.domain.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SolicitacaoCartaoTest {

    @Test
    void deveCriarSolicitacaoComStatusPendente() {
        SolicitacaoCartao solicitacao =
                new SolicitacaoCartao(null, null);

        assertEquals(
                StatusSolicitacao.PENDENTE,
                solicitacao.getStatus()
        );

        assertNotNull(solicitacao.getSolicitadaEm());
    }

    @Test
    void deveCancelarSolicitacaoPendente() {
        SolicitacaoCartao solicitacao =
                new SolicitacaoCartao(null, null);

        solicitacao.cancelar();

        assertEquals(
                StatusSolicitacao.CANCELADA,
                solicitacao.getStatus()
        );
    }

    @Test
    void naoDeveCancelarSolicitacaoJaCancelada() {
        SolicitacaoCartao solicitacao =
                new SolicitacaoCartao(null, null);

        solicitacao.cancelar();

        assertThrows(
                RegraNegocioException.class,
                solicitacao::cancelar
        );

        assertEquals(
                StatusSolicitacao.CANCELADA,
                solicitacao.getStatus()
        );
    }
}
