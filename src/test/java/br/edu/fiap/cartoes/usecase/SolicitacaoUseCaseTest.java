package br.edu.fiap.cartoes.usecase;
import br.edu.fiap.cartoes.dto.SolicitacaoRequest;
import br.edu.fiap.cartoes.entity.StatusSolicitacao;
import br.edu.fiap.cartoes.exception.RegraNegocioException;
import br.edu.fiap.cartoes.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Cenários do caso de uso para cobertura de regras de negócio e titularidade.
 * Ativar quando os repositórios e a implementação do use case forem concluídos.
 */
@Disabled("Aguardando implementação de SolicitacaoUseCase e repositórios (Devs 3 e 4)")
class SolicitacaoUseCaseTest {

    private final SolicitacaoUseCase useCase = new SolicitacaoUseCase();

    @Test
    void buscar_quandoPedidoNaoForDoTitular_deveRetornar404() {
        assertThrows(RecursoNaoEncontradoException.class, () -> useCase.buscar(1L, 99L));
    }

    @Test
    void cancelar_quandoPedidoNaoEstiverPendente_deveRetornar409() {
        assertThrows(RegraNegocioException.class, () -> useCase.cancelar(1L, 99L));
    }

    @Test
    void emitir_quandoPedidoJaEmitido_deveRetornar409SemCriarSegundoCartao() {
        assertThrows(RegraNegocioException.class, () -> useCase.emitir(1L, 99L));
    }

    @Test
    void listar_comStatusOpcional_deveFiltrarSomenteSolicitacoesDoTitular() {
        // Cenário-alvo: somente registros do usuarioId informado e filtro opcional por status.
        useCase.listar(1L, StatusSolicitacao.PENDENTE);
    }

    @Test
    void criar_quandoJaExistirPendenteMesmoTipo_deveRetornar409() {
        SolicitacaoRequest request = new SolicitacaoRequest(1L);
        assertThrows(RegraNegocioException.class, () -> useCase.criar(1L, request));
    }
}
