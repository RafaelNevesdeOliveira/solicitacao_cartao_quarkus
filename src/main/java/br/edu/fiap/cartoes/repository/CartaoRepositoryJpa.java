
package br.edu.fiap.cartoes.repository;

import br.edu.fiap.cartoes.entity.Cartao;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@ApplicationScoped
public class CartaoRepositoryJpa implements CartaoRepository {

    @PersistenceContext
    EntityManager em;

    @Override
    public void salvar(Cartao cartao) {
        em.persist(cartao);
        em.flush();
    }

    @Override
    public boolean existePorSolicitacao(Long solicitacaoId) {
        Long quantidade = em.createQuery(
                        "select count(c) from Cartao c " +
                                "where c.solicitacao.id = :solicitacaoId",
                        Long.class)
                .setParameter("solicitacaoId", solicitacaoId)
                .getSingleResult();

        return quantidade > 0;
    }

    @Override
    public List<Cartao> listarPorTitular(Long usuarioId) {
        return em.createQuery(
                        "select c from Cartao c " +
                                "join fetch c.solicitacao s " +
                                "join fetch s.usuario u " +
                                "join fetch s.tipoCartao t " +
                                "where u.id = :usuarioId " +
                                "order by c.emitidoEm desc, c.id desc",
                        Cartao.class)
                .setParameter("usuarioId", usuarioId)
                .getResultList();
    }
}
