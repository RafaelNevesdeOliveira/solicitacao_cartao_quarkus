package br.edu.fiap.cartoes.domain.model;

import br.edu.fiap.cartoes.domain.exception.RegraNegocioException;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Representa uma solicitacao de cartao bancario.
 *
 * Contem os dados persistidos e as regras
 * de mudanca de estado da solicitacao.
 */
@Entity
@Table(name = "solicitacoes_cartao")
public class SolicitacaoCartao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_cartao_id", nullable = false)
    private TipoCartao tipoCartao;

    @Column(name = "solicitada_em", nullable = false)
    private Instant solicitadaEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusSolicitacao status;

    /**
     * Construtor exigido pelo JPA.
     */
    protected SolicitacaoCartao() {
    }

    /**
     * Cria uma solicitacao com estado inicial PENDENTE.
     */
    public SolicitacaoCartao(Usuario usuario, TipoCartao tipoCartao) {
        this.usuario = usuario;
        this.tipoCartao = tipoCartao;
        this.solicitadaEm = Instant.now();
        this.status = StatusSolicitacao.PENDENTE;
    }

    /**
     * Permite cancelar somente solicitacoes pendentes.
     */
    public void cancelar() {

        if (status != StatusSolicitacao.PENDENTE) {
            throw new RegraNegocioException(
                    "Somente solicitacoes pendentes podem ser canceladas."
            );
        }

        this.status = StatusSolicitacao.CANCELADA;
    }

    /**
     * Marca a solicitacao como emitida.
     *
     * Somente solicitacoes pendentes podem ser emitidas.
     * Solicitacoes canceladas ou ja emitidas sao rejeitadas.
     */
    public void marcarEmitida() {

        if (status != StatusSolicitacao.PENDENTE) {
            throw new RegraNegocioException(
                    "Somente solicitacoes pendentes podem ser emitidas."
            );
        }

        this.status = StatusSolicitacao.EMITIDA;
    }

    // Getters e setters originais

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public TipoCartao getTipoCartao() {
        return tipoCartao;
    }

    public void setTipoCartao(TipoCartao tipoCartao) {
        this.tipoCartao = tipoCartao;
    }

    public Instant getSolicitadaEm() {
        return solicitadaEm;
    }

    public void setSolicitadaEm(Instant solicitadaEm) {
        this.solicitadaEm = solicitadaEm;
    }

    public StatusSolicitacao getStatus() {
        return status;
    }

    public void setStatus(StatusSolicitacao status) {
        this.status = status;
    }
}
