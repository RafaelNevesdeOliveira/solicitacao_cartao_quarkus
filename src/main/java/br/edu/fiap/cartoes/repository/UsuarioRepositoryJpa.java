package br.edu.fiap.cartoes.repository;
import br.edu.fiap.cartoes.entity.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import java.util.Optional;

/**
 * Adaptador JPA: injete EntityManager, escreva JPQL parametrizada e retorne entidades.
 * Não chamar controller nem gerar token nesta camada. Gravações participam da transação aberta pelo use case.
 */
@ApplicationScoped
public class UsuarioRepositoryJpa implements UsuarioRepository {
    // TODO: implementar a interface depois de declarar suas assinaturas.
    @Inject
    EntityManager em;

    @Override
    public void salvar(Usuario usuario) {
        if (usuario.getId() == null) {
            em.persist(usuario);
        } else {
            em.merge(usuario);
        }
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return em.createQuery("SELECT u FROM Usuario u WHERE u.email = :email", Usuario.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return Optional.ofNullable(em.find(Usuario.class, id));
    }

    @Override
    public Optional<Usuario> buscarPorIdComBloqueio(Long id) {
        return Optional.ofNullable(em.find(Usuario.class, id, LockModeType.PESSIMISTIC_WRITE));
    }

}
