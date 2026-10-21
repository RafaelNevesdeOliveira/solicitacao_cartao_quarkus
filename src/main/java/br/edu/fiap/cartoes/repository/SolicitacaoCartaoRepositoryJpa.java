package br.edu.fiap.cartoes.repository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import br.edu.fiap.cartoes.entity.SolicitacaoCartao;
import br.edu.fiap.cartoes.entity.StatusSolicitacao;
/**
 * Adaptador JPA: injete EntityManager, escreva JPQL parametrizada e retorne entidades.
 * Não chamar controller nem gerar token nesta camada. Gravações participam da transação aberta pelo use case.
 */
@ApplicationScoped
public class SolicitacaoCartaoRepositoryJpa implements SolicitacaoCartaoRepository {
    @Inject EntityManager entityManager;

    @Override
    public void salvar(SolicitacaoCartao solicitacao) {
        entityManager.persist(solicitacao);
    }

    @Override
    public Optional<SolicitacaoCartao> buscarPorIdETitular(Long id, Long usuarioId) {
        return consultaPorIdETitular(id, usuarioId).getResultList().stream().findFirst();
    }

    @Override
    public Optional<SolicitacaoCartao> buscarPorIdETitularComBloqueio(Long id, Long usuarioId) {
        return consultaPorIdETitular(id, usuarioId)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList().stream().findFirst();
    }

    private TypedQuery<SolicitacaoCartao> consultaPorIdETitular(Long id, Long usuarioId) {
        return entityManager.createQuery("""
                SELECT s FROM SolicitacaoCartao s
                JOIN FETCH s.tipoCartao
                JOIN FETCH s.usuario
                WHERE s.id = :id AND s.usuario.id = :usuarioId
                """, SolicitacaoCartao.class)
                .setParameter("id", id)
                .setParameter("usuarioId", usuarioId);
    }

    @Override
    public List<SolicitacaoCartao> listarPorTitularEStatus(Long usuarioId, StatusSolicitacao status) {
        String jpql = """
                SELECT s FROM SolicitacaoCartao s
                JOIN FETCH s.tipoCartao
                JOIN FETCH s.usuario
                WHERE s.usuario.id = :usuarioId
                """;
        if (status != null) {
            jpql += " AND s.status = :status";
        }
        jpql += " ORDER BY s.solicitadaEm DESC, s.id DESC";
        var consulta = entityManager.createQuery(jpql, SolicitacaoCartao.class)
                .setParameter("usuarioId", usuarioId);
        if (status != null) {
            consulta.setParameter("status", status);
        }
        return consulta.getResultList();
    }

    @Override
    public boolean existePendente(Long usuarioId, Long tipoCartaoId) {
        return entityManager.createQuery("""
                SELECT COUNT(s) FROM SolicitacaoCartao s
                WHERE s.usuario.id = :usuarioId AND s.tipoCartao.id = :tipoCartaoId
                AND s.status = :status
                """, Long.class)
                .setParameter("usuarioId", usuarioId)
                .setParameter("tipoCartaoId", tipoCartaoId)
                .setParameter("status", StatusSolicitacao.PENDENTE)
                .getSingleResult() > 0;
    }
}
