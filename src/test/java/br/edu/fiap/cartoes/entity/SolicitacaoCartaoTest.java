package br.edu.fiap.cartoes.entity;

import br.edu.fiap.cartoes.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SolicitacaoCartaoTest {
    private SolicitacaoCartao novaSolicitacao() {
        return new SolicitacaoCartao(
                new Usuario("Ana", "ana@example.com", "hash"),
                new TipoCartao("ESSENCIAL", "Cartao essencial", true));
    }

    @Test
    void nascePendenteComDataDeSolicitacao() {
        var solicitacao = novaSolicitacao();
        assertEquals(StatusSolicitacao.PENDENTE, solicitacao.getStatus());
        assertNotNull(solicitacao.getSolicitadaEm());
    }

    @Test
    void cancelaSomentePedidoPendente() {
        var solicitacao = novaSolicitacao();
        solicitacao.cancelar();
        assertEquals(StatusSolicitacao.CANCELADA, solicitacao.getStatus());
        assertThrows(RegraNegocioException.class, solicitacao::cancelar);
        assertThrows(RegraNegocioException.class, solicitacao::marcarEmitida);
        assertEquals(StatusSolicitacao.CANCELADA, solicitacao.getStatus());
    }

    @Test
    void emiteSomentePedidoPendente() {
        var solicitacao = novaSolicitacao();
        solicitacao.marcarEmitida();
        assertEquals(StatusSolicitacao.EMITIDA, solicitacao.getStatus());
        assertThrows(RegraNegocioException.class, solicitacao::marcarEmitida);
        assertThrows(RegraNegocioException.class, solicitacao::cancelar);
        assertEquals(StatusSolicitacao.EMITIDA, solicitacao.getStatus());
    }
}
