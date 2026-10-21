
package br.edu.fiap.cartoes.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Camada entity: representa o estado persistido de Cartao
 * e seus comportamentos simples.
 *
 * As anotacoes JPA ja estao prontas.
 * Na migracao para Clean Architecture, sera discutido
 * se o modelo de dominio continuara anotado ou se havera
 * um modelo de persistencia separado.
 *
 * Nao retornar esta entidade diretamente no controller:
 * utilizar os DTOs.
 */
@Entity
@Table(name = "cartoes")
public class Cartao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitacao_id", nullable = false, unique = true)
    private SolicitacaoCartao solicitacao;

    @Column(name = "emitido_em", nullable = false)
    private Instant emitidoEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCartao status;

    /** Construtor sem argumentos exigido pelo JPA. */
    protected Cartao() {
    }

    /** Construtor com estado inicial ATIVO. */
    public Cartao(SolicitacaoCartao solicitacao) {
        this.solicitacao = solicitacao;
        this.emitidoEm = Instant.now();
        this.status = StatusCartao.ATIVO;
    }

    /**
     * Informa se o cartao esta ativo.
     *
     * true: cartao ativo.
     * false: cartao bloqueado.
     */
    public boolean estaAtivo() {
        return this.status == StatusCartao.ATIVO;
    }

    // Getters e setters originais

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SolicitacaoCartao getSolicitacao() {
        return solicitacao;
    }

    public void setSolicitacao(SolicitacaoCartao solicitacao) {
        this.solicitacao = solicitacao;
    }

    public Instant getEmitidoEm() {
        return emitidoEm;
    }

    public void setEmitidoEm(Instant emitidoEm) {
        this.emitidoEm = emitidoEm;
    }

    public StatusCartao getStatus() {
        return status;
    }

    public void setStatus(StatusCartao status) {
        this.status = status;
    }
}
