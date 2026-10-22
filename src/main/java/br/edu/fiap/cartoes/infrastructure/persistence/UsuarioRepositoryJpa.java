package br.edu.fiap.cartoes.infrastructure.persistence;

import br.edu.fiap.cartoes.application.port.UsuarioRepository;
import br.edu.fiap.cartoes.domain.model.Usuario;
import br.edu.fiap.cartoes.domain.exception.EmailJaCadastradoException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Implementacao JPA do contrato UsuarioRepository.
 *
 * Responsavel pela persistencia e consulta de usuarios.
 *
 * Utiliza EntityManager e JPQL para acessar
 * os dados sem expor detalhes de persistencia
 * aos casos de uso.
 */
@ApplicationScoped
public class UsuarioRepositoryJpa implements UsuarioRepository {

    private final EntityManager entityManager;

    @Inject
    public UsuarioRepositoryJpa(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Salva um novo usuario ou atualiza um existente.
     *
     * A transacao deve ser coordenada
     * pelo caso de uso chamador.
     */
    @Override
    @Transactional
    public Usuario salvar(Usuario usuario) {

        try {
            entityManager.persist(usuario);
            entityManager.flush();

            return usuario;

        } catch (RuntimeException erro) {

            if (violouChaveUnica(erro)) {
                throw new EmailJaCadastradoException(
                        usuario.getEmail()
                );
            }

            throw new IllegalStateException(
                    "Falha ao cadastrar usuario",
                    erro
            );
        }
    }

    /**
     * Busca um usuario pelo e-mail.
     *
     * Retorna Optional.empty() quando nao encontrado.
     */
    @Override
    public Optional<Usuario> buscarPorEmail(String email) {

        return entityManager.createQuery(
                        "SELECT u FROM Usuario u WHERE u.email = :email",
                        Usuario.class
                )
                .setParameter("email", email)
                .getResultStream()
                .findFirst();
    }

    /**
     * Busca um usuario pelo identificador.
     */
    @Override
    public Optional<Usuario> buscarPorId(Long id) {

        return Optional.ofNullable(
                entityManager.find(Usuario.class, id)
        );
    }

    /**
     * Busca um usuario aplicando bloqueio pessimista
     * de escrita no registro.
     *
     * A transacao deve permanecer ativa durante
     * a verificacao de duplicidade e a criacao
     * da solicitacao.
     */
    @Override
    public Optional<Usuario> buscarPorIdComBloqueio(Long id) {

        return Optional.ofNullable(
                entityManager.find(
                        Usuario.class,
                        id,
                        LockModeType.PESSIMISTIC_WRITE
                )
        );
    }

    /**
     * Verifica se a causa de uma falha
     * corresponde a violacao de chave unica.
     *
     * SQLSTATE 23505: unique violation.
     */
    private boolean violouChaveUnica(Throwable erro) {

        Throwable atual = erro;

        while (atual != null) {

            if (atual instanceof SQLException sql
                    && "23505".equals(sql.getSQLState())) {
                return true;
            }

            atual = atual.getCause();
        }

        return false;
    }
}
