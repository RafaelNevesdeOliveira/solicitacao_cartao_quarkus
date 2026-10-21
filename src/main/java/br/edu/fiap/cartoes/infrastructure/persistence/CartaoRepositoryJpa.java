
package br.edu.fiap.cartoes.infrastructure.persistence;

import br.edu.fiap.cartoes.application.port.CartaoRepository;
import br.edu.fiap.cartoes.domain.model.Cartao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

/**
 * Implementacao JPA do contrato CartaoRepository.
 *
 * Responsavel por salvar e consultar cartoes.
 *
 * Utiliza EntityManager e consultas JPQL
 * com parametros.
 *
 * Nao executa regras de negocio nem gera tokens.
 *
 * As transacoes de gravacao sao controladas
 * pelos casos de uso.
 */
@ApplicationScoped
public class CartaoRepositoryJpa implements CartaoRepository {

    @Inject
    EntityManager entityManager;

    /**
     * Salva um novo cartao ou atualiza
     * um cartao existente.
     */
    @Override
    public Cartao salvar(Cartao cartao) {

        if (cartao.getId() == null) {
            entityManager.persist(cartao);
            return cartao;
        }

        return entityManager.merge(cartao);
    }

    /**
     * Busca um cartao pelo identificador.
     *
     * Retorna Optional.empty() caso nao exista.
     */
    @Override
    public Optional<Cartao> buscarPorId(Long id) {

        return Optional.ofNullable(
                entityManager.find(Cartao.class, id)
        );
    }

    /**
     * Busca o cartao associado a uma solicitacao.
     */
    @Override
    public Optional<Cartao> buscarPorSolicitacaoId(
            Long solicitacaoId) {

        return entityManager.createQuery(
                        "SELECT c FROM Cartao c " +
                                "WHERE c.solicitacao.id = :solicitacaoId",
                        Cartao.class
                )
                .setParameter("solicitacaoId", solicitacaoId)
                .getResultStream()
                .findFirst();
    }

    /**
     * Lista os cartoes de um determinado usuario.
     */
    @Override
    public List<Cartao> listarPorUsuario(Long usuarioId) {

        return entityManager.createQuery(
                        "SELECT c FROM Cartao c " +
                                "WHERE c.solicitacao.usuario.id = :usuarioId " +
                                "ORDER BY c.id",
                        Cartao.class
                )
                .setParameter("usuarioId", usuarioId)
                .getResultList();
    }
}
