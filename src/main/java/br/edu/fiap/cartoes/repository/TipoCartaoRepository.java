
package br.edu.fiap.cartoes.repository;

import br.edu.fiap.cartoes.entity.TipoCartao;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia dos tipos de cartao.
 *
 * Define as operacoes de consulta e gravacao
 * que serao implementadas pela classe
 * TipoCartaoRepositoryJpa.
 *
 * Nao depende de EntityManager,
 * Connection ou ResultSet.
 */
public interface TipoCartaoRepository {

    /**
     * Salva um tipo de cartao.
     *
     * @param tipoCartao tipo de cartao a persistir
     * @return tipo de cartao persistido
     */
    TipoCartao salvar(TipoCartao tipoCartao);

    /**
     * Busca um tipo de cartao pelo ID.
     *
     * @param id identificador do tipo
     * @return tipo encontrado ou Optional vazio
     */
    Optional<TipoCartao> buscarPorId(Long id);

    /**
     * Lista todos os tipos de cartao cadastrados,
     * independentemente da disponibilidade.
     *
     * @return lista de tipos de cartao
     */
    List<TipoCartao> listarTodos();
}
