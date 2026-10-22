package br.edu.fiap.cartoes.usecase;

import br.edu.fiap.cartoes.presentation.dto.SolicitacaoResponse;
import br.edu.fiap.cartoes.domain.model.SolicitacaoCartao;
import br.edu.fiap.cartoes.domain.model.StatusSolicitacao;
import br.edu.fiap.cartoes.domain.model.TipoCartao;
import br.edu.fiap.cartoes.domain.model.Usuario;
import br.edu.fiap.cartoes.domain.exception.RegraNegocioException;
import br.edu.fiap.cartoes.application.port.SolicitacaoCartaoRepository;
import br.edu.fiap.cartoes.application.port.TipoCartaoRepository;
import br.edu.fiap.cartoes.application.port.UsuarioRepository;
import br.edu.fiap.cartoes.application.usecase.SolicitacaoUseCase;
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
        Usuario usuario = new Usuario(1L, "Cliente", "cliente@example.com", "hash", java.time.Instant.now());

        TipoCartao tipoCartao = new TipoCartao("ESSENCIAL", "CartÃ£o essencial", true);
        tipoCartao.setId(2L);

        SolicitacaoCartao solicitacao = new SolicitacaoCartao(usuario, tipoCartao);
        solicitacao.setId(10L);
        solicitacao.setStatus(status);
        return solicitacao;
    }
}


