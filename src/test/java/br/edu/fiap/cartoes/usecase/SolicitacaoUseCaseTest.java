package br.edu.fiap.cartoes.usecase;

import br.edu.fiap.cartoes.dto.SolicitacaoRequest;
import br.edu.fiap.cartoes.entity.*;
import br.edu.fiap.cartoes.exception.RecursoNaoEncontradoException;
import br.edu.fiap.cartoes.exception.RegraNegocioException;
import br.edu.fiap.cartoes.repository.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SolicitacaoUseCaseTest {
    private SolicitacaoUseCase useCase;
    private Usuario usuario;
    private TipoCartao tipo;
    private SolicitacaoCartao solicitacao;
    private final SolicitacaoRequest request = new SolicitacaoRequest(2L);

    @BeforeEach
    void preparar() {
        useCase = new SolicitacaoUseCase();
        useCase.solicitacoes = mock(SolicitacaoCartaoRepository.class);
        useCase.usuarios = mock(UsuarioRepository.class);
        useCase.tiposCartao = mock(TipoCartaoRepository.class);
        usuario = new Usuario("Ana", "ana@example.com", "hash");
        usuario.setId(1L);
        tipo = new TipoCartao("ESSENCIAL", "Cartao essencial", true);
        tipo.setId(2L);
        solicitacao = new SolicitacaoCartao(usuario, tipo);
        solicitacao.setId(3L);
    }

    private void prepararCriacao() {
        when(useCase.usuarios.buscarPorIdComBloqueio(1L)).thenReturn(Optional.of(usuario));
        when(useCase.tiposCartao.buscarPorId(2L)).thenReturn(Optional.of(tipo));
    }

    @Test
    void criaPedidoPendenteComTitularETipo() {
        prepararCriacao();
        doAnswer(invocacao -> {
            SolicitacaoCartao salvo = invocacao.getArgument(0);
            salvo.setId(3L);
            return null;
        }).when(useCase.solicitacoes).salvar(any(SolicitacaoCartao.class));

        var resposta = useCase.criar(1L, request);
        assertEquals(3L, resposta.id());
        assertEquals(1L, resposta.usuarioId());
        assertEquals(2L, resposta.tipoCartaoId());
        assertEquals("ESSENCIAL", resposta.tipoCartaoNome());
        assertEquals(StatusSolicitacao.PENDENTE, resposta.status());
        var captor = ArgumentCaptor.forClass(SolicitacaoCartao.class);
        var ordem = inOrder(useCase.usuarios, useCase.tiposCartao, useCase.solicitacoes);
        ordem.verify(useCase.usuarios).buscarPorIdComBloqueio(1L);
        ordem.verify(useCase.tiposCartao).buscarPorId(2L);
        ordem.verify(useCase.solicitacoes).existePendente(1L, 2L);
        ordem.verify(useCase.solicitacoes).salvar(captor.capture());
        assertSame(usuario, captor.getValue().getUsuario());
        assertSame(tipo, captor.getValue().getTipoCartao());
        assertEquals(captor.getValue().getSolicitadaEm(), resposta.solicitadaEm());
    }

    @Test
    void rejeitaTitularInexistente() {
        assertThrows(RecursoNaoEncontradoException.class, () -> useCase.criar(1L, request));
        verifyNoInteractions(useCase.tiposCartao, useCase.solicitacoes);
    }

    @Test
    void rejeitaTipoInexistente() {
        when(useCase.usuarios.buscarPorIdComBloqueio(1L)).thenReturn(Optional.of(usuario));
        assertThrows(RecursoNaoEncontradoException.class, () -> useCase.criar(1L, request));
        verifyNoInteractions(useCase.solicitacoes);
    }

    @Test
    void rejeitaTipoIndisponivel() {
        prepararCriacao();
        tipo.setDisponivel(false);
        assertThrows(RegraNegocioException.class, () -> useCase.criar(1L, request));
        verifyNoInteractions(useCase.solicitacoes);
    }

    @Test
    void rejeitaPedidoPendenteDuplicado() {
        prepararCriacao();
        when(useCase.solicitacoes.existePendente(1L, 2L)).thenReturn(true);
        assertThrows(RegraNegocioException.class, () -> useCase.criar(1L, request));
        verify(useCase.solicitacoes, never()).salvar(any());
    }

    @Test
    void listaComFiltroOpcionalESemResultados() {
        when(useCase.solicitacoes.listarPorTitularEStatus(1L, StatusSolicitacao.PENDENTE))
                .thenReturn(List.of(solicitacao));
        var respostas = useCase.listar(1L, StatusSolicitacao.PENDENTE);
        assertEquals(1, respostas.size());
        assertEquals(3L, respostas.getFirst().id());
        assertEquals("ESSENCIAL", respostas.getFirst().tipoCartaoNome());
        assertEquals(List.of(), useCase.listar(1L, null));
        verify(useCase.solicitacoes).listarPorTitularEStatus(1L, null);
    }

    @Test
    void buscaSomentePedidoDoTitular() {
        when(useCase.solicitacoes.buscarPorIdETitular(3L, 1L)).thenReturn(Optional.of(solicitacao));
        assertEquals(3L, useCase.buscar(1L, 3L).id());
        assertThrows(RecursoNaoEncontradoException.class, () -> useCase.buscar(4L, 3L));
        assertThrows(RecursoNaoEncontradoException.class, () -> useCase.buscar(1L, 99L));
    }

    @Test
    void cancelaComBloqueioESemContornarEstado() {
        when(useCase.solicitacoes.buscarPorIdETitularComBloqueio(3L, 1L))
                .thenReturn(Optional.of(solicitacao));
        assertEquals(StatusSolicitacao.CANCELADA, useCase.cancelar(1L, 3L).status());
        assertThrows(RegraNegocioException.class, () -> useCase.cancelar(1L, 3L));
        verify(useCase.solicitacoes, never()).buscarPorIdETitular(any(), any());
    }

    @Test
    void naoCancelaPedidoEmitido() {
        solicitacao.marcarEmitida();
        when(useCase.solicitacoes.buscarPorIdETitularComBloqueio(3L, 1L))
                .thenReturn(Optional.of(solicitacao));
        assertThrows(RegraNegocioException.class, () -> useCase.cancelar(1L, 3L));
        assertEquals(StatusSolicitacao.EMITIDA, solicitacao.getStatus());
    }

    @Test
    void naoCancelaPedidoInexistenteOuDeOutroTitular() {
        assertThrows(RecursoNaoEncontradoException.class, () -> useCase.cancelar(4L, 3L));
        assertThrows(RecursoNaoEncontradoException.class, () -> useCase.cancelar(1L, 99L));
    }
}
