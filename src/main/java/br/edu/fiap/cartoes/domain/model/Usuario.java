
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

    public static Usuario novo(String nome, String email, String hash) {
        return new Usuario(nome,email,hash);
    }

    public void alterarNome(String nome) {
        if (nome == null || nome.isBlank()
                || nome.trim().length() > 100) {
            throw new RegraNegocioException(
                    "O nome deve conter entre 1 e 100 caracteres."
            );
        }

        this.nome = nome.trim();
    }

    public void atualizarSenhaHash(String senhaHash) {
        if (senhaHash == null || senhaHash.isBlank()) {
            throw new RegraNegocioException(
                    "O hash da senha não pode ser vazio."
            );
        }

        this.senhaHash = senhaHash;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }
}
