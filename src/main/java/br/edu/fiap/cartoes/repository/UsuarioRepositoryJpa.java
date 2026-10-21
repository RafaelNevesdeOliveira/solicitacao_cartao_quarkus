package br.edu.fiap.cartoes.repository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import br.edu.fiap.cartoes.entity.Usuario;
/**
 * Adaptador JPA: injete EntityManager, escreva JPQL parametrizada e retorne entidades.
 * Não chamar controller nem gerar token nesta camada. Gravações participam da transação aberta pelo use case.
 */
@ApplicationScoped
public class UsuarioRepositoryJpa implements UsuarioRepository {
    @Inject EntityManager entityManager;

    @Override
    public Optional<Usuario> buscarPorIdComBloqueio(Long id) {
        return Optional.ofNullable(entityManager.find(Usuario.class, id, LockModeType.PESSIMISTIC_WRITE));
    }
}
