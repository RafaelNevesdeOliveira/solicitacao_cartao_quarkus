package br.edu.fiap.cartoes.usecase;

import br.edu.fiap.cartoes.application.port.CartaoRepository;
import br.edu.fiap.cartoes.application.port.SolicitacaoCartaoRepository;
import br.edu.fiap.cartoes.application.port.TipoCartaoRepository;
import br.edu.fiap.cartoes.application.port.UsuarioRepository;
import br.edu.fiap.cartoes.application.usecase.SolicitacaoUseCase;

import br.edu.fiap.cartoes.domain.exception.RecursoNaoEncontradoException;
import br.edu.fiap.cartoes.domain.exception.RegraNegocioException;
import br.edu.fiap.cartoes.domain.model.Cartao;
import br.edu.fiap.cartoes.domain.model.SolicitacaoCartao;
import br.edu.fiap.cartoes.domain.model.StatusCartao;
import br.edu.fiap.cartoes.domain.model.StatusSolicitacao;
import br.edu.fiap.cartoes.domain.model.TipoCartao;
import br.edu.fiap.cartoes.domain.model.Usuario;

import br.edu.fiap.cartoes.presentation.dto.CartaoResponse;
import br.edu.fiap.cartoes.presentation.dto.SolicitacaoResponse;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SolicitacaoUseCaseTest {

    private final SolicitacaoCartaoRepository solicitacaoRepository =
            mock(SolicitacaoCartaoRepository.class);

    private final TipoCartaoRepository tipoCartaoRepository =
            mock(TipoCartaoRepository.class);

    private final UsuarioRepository usuarioRepository =
            mock(UsuarioRepository.class);

    private final CartaoRepository cartaoRepository =
            mock(CartaoRepository.class);

    private final SolicitacaoUseCase useCase =
            new SolicitacaoUseCase(
                    solicitacaoRepository,
                    tipoCartaoRepository,
                    usuarioRepository,
                    cartaoRepository
            );

    /**
     * Testa o cancelamento de uma solicitacao pendente.
     */
    @Test
    void deveCancelarSolicitacaoPendenteEAtualizarRepositorio() {

        SolicitacaoCartao solicitacao =
                novaSolicitacao(StatusSolicitacao.PENDENTE);

        when(solicitacaoRepository.buscarPorIdComBloqueio(10L))
                .thenReturn(Optional.of(solicitacao));

        SolicitacaoResponse resposta =
                useCase.cancelar(1L, 10L);

        assertEquals(
                StatusSolicitacao.CANCELADA,
                resposta.status()
        );

        verify(solicitacaoRepository)
                .buscarPorIdComBloqueio(10L);

        verify(solicitacaoRepository)
                .salvar(solicitacao);

        verifyNoInteractions(cartaoRepository);
    }

    /**
     * Impede cancelar uma solicitacao ja emitida.
     */
    @Test
    void naoDevePersistirCancelamentoDeSolicitacaoEmitida() {

        SolicitacaoCartao solicitacao =
                novaSolicitacao(StatusSolicitacao.EMITIDA);

        when(solicitacaoRepository.buscarPorIdComBloqueio(10L))
                .thenReturn(Optional.of(solicitacao));

        assertThrows(
                RegraNegocioException.class,
                () -> useCase.cancelar(1L, 10L)
        );

        verify(solicitacaoRepository, never())
                .salvar(solicitacao);

        verifyNoInteractions(
                tipoCartaoRepository,
                usuarioRepository,
                cartaoRepository
        );
    }

    /**
     * Emite um cartao para uma solicitacao pendente.
     */
    @Test
    void deveEmitirCartaoParaSolicitacaoPendente() {

        SolicitacaoCartao solicitacao =
                novaSolicitacao(StatusSolicitacao.PENDENTE);

        when(solicitacaoRepository.buscarPorIdComBloqueio(10L))
                .thenReturn(Optional.of(solicitacao));

        when(cartaoRepository.buscarPorSolicitacaoId(10L))
                .thenReturn(Optional.empty());

        CartaoResponse resposta =
                useCase.emitir(1L, 10L);

        assertEquals(
                StatusSolicitacao.EMITIDA,
                solicitacao.getStatus()
        );

        assertEquals(
                StatusCartao.ATIVO,
                resposta.status()
        );

        assertEquals(10L, resposta.solicitacaoId());
        assertEquals(2L, resposta.tipoCartaoId());

        verify(solicitacaoRepository)
                .salvar(solicitacao);

        verify(cartaoRepository)
                .salvar(any(Cartao.class));
    }

    /**
     * Impede emitir novamente uma solicitacao
     * que ja foi emitida.
     */
    @Test
    void naoDeveEmitirSolicitacaoJaEmitida() {

        SolicitacaoCartao solicitacao =
                novaSolicitacao(StatusSolicitacao.EMITIDA);

        when(solicitacaoRepository.buscarPorIdComBloqueio(10L))
                .thenReturn(Optional.of(solicitacao));

        assertThrows(
                RegraNegocioException.class,
                () -> useCase.emitir(1L, 10L)
        );

        verify(cartaoRepository, never())
                .salvar(any(Cartao.class));

        verify(solicitacaoRepository, never())
                .salvar(any(SolicitacaoCartao.class));
    }

    /**
     * Impede emitir cartao para uma solicitacao
     * cancelada.
     */
    @Test
    void naoDeveEmitirSolicitacaoCancelada() {

        SolicitacaoCartao solicitacao =
                novaSolicitacao(StatusSolicitacao.CANCELADA);

        when(solicitacaoRepository.buscarPorIdComBloqueio(10L))
                .thenReturn(Optional.of(solicitacao));

        assertThrows(
                RegraNegocioException.class,
                () -> useCase.emitir(1L, 10L)
        );

        verify(cartaoRepository, never())
                .salvar(any(Cartao.class));
    }

    /**
     * Impede emissao quando ja existe um cartao
     * vinculado a solicitacao.
     */
    @Test
    void naoDeveEmitirCartaoDuplicado() {

        SolicitacaoCartao solicitacao =
                novaSolicitacao(StatusSolicitacao.PENDENTE);

        Cartao cartaoExistente =
                new Cartao(solicitacao);

        when(solicitacaoRepository.buscarPorIdComBloqueio(10L))
                .thenReturn(Optional.of(solicitacao));

        when(cartaoRepository.buscarPorSolicitacaoId(10L))
                .thenReturn(Optional.of(cartaoExistente));

        assertThrows(
                RegraNegocioException.class,
                () -> useCase.emitir(1L, 10L)
        );

        assertEquals(
                StatusSolicitacao.PENDENTE,
                solicitacao.getStatus()
        );

        verify(cartaoRepository, never())
                .salvar(any(Cartao.class));
    }

    /**
     * Impede acessar solicitacao de outro usuario.
     */
    @Test
    void naoDeveEmitirSolicitacaoDeOutroUsuario() {

        SolicitacaoCartao solicitacao =
                novaSolicitacao(StatusSolicitacao.PENDENTE);

        when(solicitacaoRepository.buscarPorIdComBloqueio(10L))
                .thenReturn(Optional.of(solicitacao));

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> useCase.emitir(99L, 10L)
        );

        verifyNoInteractions(cartaoRepository);
    }

    /**
     * Cria uma solicitacao para os testes.
     */
    private SolicitacaoCartao novaSolicitacao(
            StatusSolicitacao status) {

        Usuario usuario = new Usuario(
                1L,
                "Cliente",
                "cliente@example.com",
                "hash",
                Instant.now()
        );

        TipoCartao tipoCartao = new TipoCartao(
                "ESSENCIAL",
                "Cartao essencial",
                true
        );

        tipoCartao.setId(2L);

        SolicitacaoCartao solicitacao =
                new SolicitacaoCartao(usuario, tipoCartao);

        solicitacao.setId(10L);
        solicitacao.setStatus(status);

        return solicitacao;
    }
}
