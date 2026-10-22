
package br.edu.fiap.cartoes.infrastructure.persistence;

import br.edu.fiap.cartoes.application.port.SolicitacaoCartaoRepository;
import br.edu.fiap.cartoes.domain.model.SolicitacaoCartao;
import br.edu.fiap.cartoes.domain.model.StatusSolicitacao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

/**
 * Implementacao JPA do contrato
 * SolicitacaoCartaoRepository.
 *
 * Responsavel por salvar e consultar
 * solicitacoes de cartao.
 *
 * Utiliza EntityManager e consultas JPQL
 * com parametros.
 *
 * Oferece busca com bloqueio pessimista
 * para operacoes de cancelamento e emissao.
 *
 * As transacoes de gravacao sao controladas
 * pelos casos de uso.
 */
@ApplicationScoped
public class SolicitacaoCartaoRepositoryJpa
        implements SolicitacaoCartaoRepository {

    @Inject
    EntityManager entityManager;

    /**
     * Salva uma nova solicitacao ou atualiza
     * uma solicitacao existente.
     */
    @Override
    public SolicitacaoCartao salvar(
            SolicitacaoCartao solicitacao) {

        if (solicitacao.getId() == null) {
            entityManager.persist(solicitacao);
            return solicitacao;
        }

        return entityManager.merge(solicitacao);
    }

    /**
     * Busca uma solicitacao pelo identificador.
     *
     * Retorna Optional.empty() caso nao exista.
     */
    @Override
    public Optional<SolicitacaoCartao> buscarPorId(Long id) {

        return Optional.ofNullable(
                entityManager.find(SolicitacaoCartao.class, id)
        );
    }

    /**
     * Busca uma solicitacao com bloqueio pessimista
     * de escrita.
     *
     * Deve ser chamada dentro de uma transacao ativa.
     * O bloqueio protege a solicitacao contra
     * atualizacoes concorrentes.
     *
     * Retorna Optional.empty() caso nao exista.
     */
    @Override
    public Optional<SolicitacaoCartao> buscarPorIdComBloqueio(Long id) {

        return Optional.ofNullable(
                entityManager.find(
                        SolicitacaoCartao.class,
                        id,
                        LockModeType.PESSIMISTIC_WRITE
                )
        );
    }

    /**
     * Lista todas as solicitacoes de um usuario.
     */
    @Override
    public List<SolicitacaoCartao> listarPorUsuario(
            Long usuarioId) {

        return entityManager.createQuery(
                        "SELECT s FROM SolicitacaoCartao s " +
                                "WHERE s.usuario.id = :usuarioId " +
                                "ORDER BY s.solicitadaEm DESC",
                        SolicitacaoCartao.class
                )
                .setParameter("usuarioId", usuarioId)
                .getResultList();
    }

    /**
     * Lista as solicitacoes de um usuario
     * filtradas por status.
     */
    @Override
    public List<SolicitacaoCartao> listarPorUsuarioEStatus(
            Long usuarioId,
            StatusSolicitacao status) {

        return entityManager.createQuery(
                        "SELECT s FROM SolicitacaoCartao s " +
                                "WHERE s.usuario.id = :usuarioId " +
                                "AND s.status = :status " +
                                "ORDER BY s.solicitadaEm DESC",
                        SolicitacaoCartao.class
                )
                .setParameter("usuarioId", usuarioId)
                .setParameter("status", status)
                .getResultList();
    }
}
