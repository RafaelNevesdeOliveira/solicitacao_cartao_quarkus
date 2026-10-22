
package br.edu.fiap.cartoes.application.port;

import br.edu.fiap.cartoes.domain.model.SolicitacaoCartao;
import br.edu.fiap.cartoes.domain.model.StatusSolicitacao;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia das solicitacoes de cartao.
 *
 * Define as operacoes utilizadas pelos casos de uso
 * para salvar e consultar solicitacoes.
 *
 * A implementacao e realizada pela classe
 * SolicitacaoCartaoRepositoryJpa.
 *
 * Nao depende de EntityManager,
 * Connection ou ResultSet.
 */
public interface SolicitacaoCartaoRepository {

    /**
     * Salva uma solicitacao de cartao.
     *
     * @param solicitacao solicitacao a persistir
     * @return solicitacao persistida
     */
    SolicitacaoCartao salvar(SolicitacaoCartao solicitacao);

    /**
     * Busca uma solicitacao pelo ID.
     *
     * @param id identificador da solicitacao
     * @return solicitacao encontrada ou Optional vazio
     */
    Optional<SolicitacaoCartao> buscarPorId(Long id);

    /**
     * Lista as solicitacoes de determinado usuario.
     *
     * @param usuarioId identificador do titular
     * @return lista de solicitacoes do usuario
     */
    List<SolicitacaoCartao> listarPorUsuario(Long usuarioId);

    /**
     * Lista as solicitacoes de determinado usuario
     * filtradas pelo status.
     *
     * @param usuarioId identificador do titular
     * @param status situacao da solicitacao
     * @return lista de solicitacoes correspondentes
     */
    List<SolicitacaoCartao> listarPorUsuarioEStatus(
            Long usuarioId,
            StatusSolicitacao status
    );

    /**
     * Busca uma solicitacao com bloqueio pessimista
     * de escrita para evitar alteracoes concorrentes.
     *
     * Deve ser utilizado dentro de uma transacao.
     *
     * @param id identificador da solicitacao
     * @return solicitacao encontrada ou Optional vazio
     */
    Optional<SolicitacaoCartao> buscarPorIdComBloqueio(Long id);
}
