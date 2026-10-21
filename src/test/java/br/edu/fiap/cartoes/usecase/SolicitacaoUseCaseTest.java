package br.edu.fiap.cartoes.usecase;

import br.edu.fiap.cartoes.dto.SolicitacaoResponse;
import br.edu.fiap.cartoes.entity.SolicitacaoCartao;
import br.edu.fiap.cartoes.entity.StatusSolicitacao;
import br.edu.fiap.cartoes.entity.TipoCartao;
import br.edu.fiap.cartoes.entity.Usuario;
import br.edu.fiap.cartoes.exception.RegraNegocioException;
import br.edu.fiap.cartoes.repository.SolicitacaoCartaoRepository;
import br.edu.fiap.cartoes.repository.TipoCartaoRepository;
import br.edu.fiap.cartoes.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SolicitacaoUseCaseTest {

    @Test
    void deveCancelarSolicitacaoPendenteEAtualizarRepositorio() {
        SolicitacaoCartaoRepository solicitacaoRepository = mock(SolicitacaoCartaoRepository.class);
        TipoCartaoRepository tipoCartaoRepository = mock(TipoCartaoRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        SolicitacaoCartao solicitacao = novaSolicitacao(StatusSolicitacao.PENDENTE);
        when(solicitacaoRepository.buscarPorId(10L)).thenReturn(Optional.of(solicitacao));

        SolicitacaoUseCase useCase = new SolicitacaoUseCase(
                solicitacaoRepository, tipoCartaoRepository, usuarioRepository
        );

        SolicitacaoResponse resposta = useCase.cancelar(1L, 10L);

        assertEquals(StatusSolicitacao.CANCELADA, resposta.status());
        verify(solicitacaoRepository).salvar(solicitacao);
    }

    @Test
    void naoDevePersistirCancelamentoDeSolicitacaoEmitida() {
        SolicitacaoCartaoRepository solicitacaoRepository = mock(SolicitacaoCartaoRepository.class);
        TipoCartaoRepository tipoCartaoRepository = mock(TipoCartaoRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        SolicitacaoCartao solicitacao = novaSolicitacao(StatusSolicitacao.EMITIDA);
        when(solicitacaoRepository.buscarPorId(10L)).thenReturn(Optional.of(solicitacao));

        SolicitacaoUseCase useCase = new SolicitacaoUseCase(
                solicitacaoRepository, tipoCartaoRepository, usuarioRepository
        );

        assertThrows(RegraNegocioException.class, () -> useCase.cancelar(1L, 10L));

        verifyNoInteractions(tipoCartaoRepository, usuarioRepository);
        verify(solicitacaoRepository, never()).salvar(solicitacao);
    }

    private SolicitacaoCartao novaSolicitacao(StatusSolicitacao status) {
        Usuario usuario = new Usuario("Cliente", "cliente@example.com", "hash");
        usuario.setId(1L);
        TipoCartao tipoCartao = new TipoCartao("ESSENCIAL", "Cartão essencial", true);
        tipoCartao.setId(2L);

        SolicitacaoCartao solicitacao = new SolicitacaoCartao(usuario, tipoCartao);
        solicitacao.setId(10L);
        solicitacao.setStatus(status);
        return solicitacao;
    }
}
