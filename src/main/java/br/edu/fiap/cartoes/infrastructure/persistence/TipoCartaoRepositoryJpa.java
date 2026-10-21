
package br.edu.fiap.cartoes.infrastructure.persistence;

import br.edu.fiap.cartoes.application.port.TipoCartaoRepository;
import br.edu.fiap.cartoes.domain.model.TipoCartao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

/**
 * Implementacao JPA do contrato TipoCartaoRepository.
 *
 * Responsavel por salvar e consultar tipos de cartao.
 *
 * Utiliza EntityManager para acessar o banco de dados.
 *
 * Nao utiliza o adaptador JDBC.
 *
 * As transacoes de gravacao sao controladas
 * pelos casos de uso.
 */
@ApplicationScoped
public class TipoCartaoRepositoryJpa implements TipoCartaoRepository {

    @Inject
    EntityManager entityManager;

    /**
     * Salva um novo tipo de cartao ou atualiza
     * um tipo de cartao existente.
     */
    @Override
    public TipoCartao salvar(TipoCartao tipoCartao) {

        if (tipoCartao.getId() == null) {
            entityManager.persist(tipoCartao);
            return tipoCartao;
        }

        return entityManager.merge(tipoCartao);
    }

    /**
     * Busca um tipo de cartao pelo identificador.
     *
     * Retorna Optional.empty() caso nao exista.
     */
    @Override
    public Optional<TipoCartao> buscarPorId(Long id) {

        return Optional.ofNullable(
                entityManager.find(TipoCartao.class, id)
        );
    }

    /**
     * Lista todos os tipos de cartao cadastrados,
     * independentemente da disponibilidade.
     */
    @Override
    public List<TipoCartao> listarTodos() {

        return entityManager.createQuery(
                        "SELECT t FROM TipoCartao t ORDER BY t.id",
                        TipoCartao.class
                )
                .getResultList();
    }
}
