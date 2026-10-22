package br.edu.fiap.cartoes.application.port;

import br.edu.fiap.cartoes.domain.model.Usuario;

import java.util.Optional;

/**
 * Contrato de persistencia de usuarios.
 *
 * Define as operacoes necessarias para acessar
 * e armazenar usuarios no banco de dados.
 *
 * A implementacao e realizada por
 * UsuarioRepositoryJpa.
 */
public interface UsuarioRepository {

    /**
     * Salva um usuario no banco de dados.
     *
     * @param usuario usuario a ser salvo
     * @return usuario persistido
     */
    Usuario salvar(Usuario usuario);

    /**
     * Busca um usuario pelo endereco de e-mail.
     *
     * @param email e-mail do usuario
     * @return Optional contendo o usuario,
     *         caso seja encontrado
     */
    Optional<Usuario> buscarPorEmail(String email);

    /**
     * Busca um usuario pelo identificador.
     *
     * @param id identificador do usuario
     * @return Optional contendo o usuario,
     *         caso seja encontrado
     */
    Optional<Usuario> buscarPorId(Long id);

    /**
     * Busca um usuario aplicando bloqueio pessimista
     * de escrita no registro correspondente.
     *
     * Deve ser utilizado dentro de uma transacao ativa
     * para coordenar operacoes concorrentes.
     *
     * @param id identificador do usuario
     * @return Optional contendo o usuario bloqueado,
     *         caso seja encontrado
     */
    Optional<Usuario> buscarPorIdComBloqueio(Long id);
}
