
package br.edu.fiap.cartoes.repository;
import br.edu.fiap.cartoes.entity.Usuario;
import br.edu.fiap.cartoes.exception.EmailJaCadastradoException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Implementacao JPA do contrato UsuarioRepository.
 *
 * Responsavel por salvar e consultar usuarios.
 *
 * Utiliza EntityManager e consultas JPQL
 * com parametros para acessar o banco de dados.
 *
 * As transacoes de gravacao sao controladas
 * pelos casos de uso.
 */
@ApplicationScoped
public class UsuarioRepositoryJpa implements UsuarioRepository {

    @Inject
    EntityManager entityManager;

    /**
     * Salva um novo usuario ou atualiza um existente.
     */
    @Override
    public Usuario salvar(Usuario usuario) {

        if (usuario.getId() == null) {
            entityManager.persist(usuario);
            return usuario;
        }

        return entityManager.merge(usuario);
    }

    /**
     * Busca um usuario pelo e-mail.
     *
     * Retorna Optional.empty() caso nao exista.
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

    @Override
    @Transactional
    public Usuario cadastrar(Usuario usuario){
        try{
            entityManager.persist(usuario);
            entityManager.flush();
            return usuario;
        }catch(RuntimeException erro){
            if(violouChaveUnica(erro)){
                throw new EmailJaCadastradoException(usuario.getEmail());
            }
            throw new IllegalStateException("Falhou", erro);
        }
    }

    private boolean violouChaveUnica(Throwable erro){
        Throwable atual = erro;
        while(atual != null){
            if(atual instanceof SQLException sql && "23505".equals(sql.getSQLState())){
                return true;
            }
            atual = atual.getCause();
        }
        return false;
    }
}
