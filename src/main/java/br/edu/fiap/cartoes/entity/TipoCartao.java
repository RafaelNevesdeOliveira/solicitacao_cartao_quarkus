
package br.edu.fiap.cartoes.entity;

import jakarta.persistence.*;

/**
 * Camada entity: representa o estado persistido de TipoCartao
 * e seus comportamentos simples.
 *
 * As anotacoes JPA ja estao prontas.
 * Na migracao para Clean Architecture, sera discutido
 * se o dominio continuara anotado ou se havera
 * um modelo de persistencia separado.
 */
@Entity
@Table(name = "tipos_cartao")
public class TipoCartao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String nome;

    @Column(nullable = false, length = 255)
    private String descricao;

    @Column(nullable = false)
    private boolean disponivel;

    /** Construtor sem argumentos exigido pelo JPA. */
    protected TipoCartao() {
    }

    /** Construtor utilizado no cadastro. */
    public TipoCartao(
            String nome,
            String descricao,
            boolean disponivel
    ) {
        this.nome = nome;
        this.descricao = descricao;
        this.disponivel = disponivel;
    }

    /**
     * Altera a disponibilidade do tipo de cartao.
     *
     * false: impede novas solicitacoes.
     * true: permite novas solicitacoes.
     *
     * Solicitacoes anteriores nao sao apagadas.
     */
    public void alterarDisponibilidade(boolean disponivel) {
        this.disponivel = disponivel;
    }

    // Getters e setters originais

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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;
    }
}
