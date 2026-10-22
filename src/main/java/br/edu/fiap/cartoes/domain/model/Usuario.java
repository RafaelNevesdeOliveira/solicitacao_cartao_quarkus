
package br.edu.fiap.cartoes.domain.model;

import br.edu.fiap.cartoes.domain.exception.RegraNegocioException;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * Representa o usuário da aplicação.
 * Contém seus dados e comportamentos.
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected Usuario() {
    }

    public Usuario(String nome, String email, String senhaHash) {
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.criadoEm = Instant.now();
    }

    public Usuario(Long id, String nome, String email, String senhaHash, Instant criadoEm) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.criadoEm = criadoEm;
    }

    public static Usuario de(Usuario usuario) {
        Usuario entity = new Usuario();
        entity.nome = usuario.nome;
        entity.email = usuario.email;
        entity.senhaHash = usuario.senhaHash;
        entity.criadoEm = usuario.criadoEm;
        return entity;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

}
