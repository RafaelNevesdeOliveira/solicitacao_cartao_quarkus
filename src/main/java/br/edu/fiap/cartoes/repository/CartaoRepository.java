
package br.edu.fiap.cartoes.repository;

import br.edu.fiap.cartoes.entity.Cartao;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia dos cartoes emitidos.
 *
 * Define as operacoes utilizadas pelos casos de uso
 * para salvar e consultar cartoes.
 *
 * A implementacao sera realizada pela classe
 * CartaoRepositoryJpa.
 *
 * Nao depende de EntityManager,
 * Connection ou ResultSet.
 */
public interface CartaoRepository {

    /**
     * Salva um cartao no banco de dados.
     *
     * @param cartao cartao a persistir
     * @return cartao persistido
     */
    Cartao salvar(Cartao cartao);

    /**
     * Busca um cartao pelo identificador.
     *
     * @param id identificador do cartao
     * @return cartao encontrado ou Optional vazio
     */
    Optional<Cartao> buscarPorId(Long id);

    /**
     * Busca o cartao associado a uma solicitacao.
     *
     * @param solicitacaoId identificador da solicitacao
     * @return cartao encontrado ou Optional vazio
     */
    Optional<Cartao> buscarPorSolicitacaoId(Long solicitacaoId);

    /**
     * Lista os cartoes pertencentes a um usuario.
     *
     * @param usuarioId identificador do titular
     * @return lista de cartoes do usuario
     */
    List<Cartao> listarPorUsuario(Long usuarioId);
}
