package br.edu.fiap.cartoes.entity;

import br.edu.fiap.cartoes.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SolicitacaoCartaoTest {

    @Test
    void deveTransicionarDePendenteParaCancelada() {
        SolicitacaoCartao solicitacao = novaSolicitacao();

        solicitacao.cancelar();

        assertEquals(StatusSolicitacao.CANCELADA, solicitacao.getStatus());
    }

    @Test
    void naoDeveCancelarSolicitacaoQueJaFoiCancelada() {
        SolicitacaoCartao solicitacao = novaSolicitacao();
        solicitacao.setStatus(StatusSolicitacao.CANCELADA);

        assertThrows(RegraNegocioException.class, solicitacao::cancelar);
        assertEquals(StatusSolicitacao.CANCELADA, solicitacao.getStatus());
    }

    private SolicitacaoCartao novaSolicitacao() {
        return new SolicitacaoCartao(
                new Usuario("Cliente", "cliente@example.com", "hash"),
                new TipoCartao("ESSENCIAL", "Cartão essencial", true)
        );
    }
}
