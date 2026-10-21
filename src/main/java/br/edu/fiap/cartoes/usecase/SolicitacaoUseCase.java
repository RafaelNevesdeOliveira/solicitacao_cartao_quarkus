package br.edu.fiap.cartoes.usecase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import br.edu.fiap.cartoes.dto.*;
import br.edu.fiap.cartoes.entity.StatusSolicitacao;
import br.edu.fiap.cartoes.entity.SolicitacaoCartao;
import br.edu.fiap.cartoes.exception.EtapaNaoImplementadaException;
import br.edu.fiap.cartoes.exception.RecursoNaoEncontradoException;
import br.edu.fiap.cartoes.exception.RegraNegocioException;
import br.edu.fiap.cartoes.repository.SolicitacaoCartaoRepository;
import br.edu.fiap.cartoes.repository.TipoCartaoRepository;
import br.edu.fiap.cartoes.repository.UsuarioRepository;
/**
 * Camada de aplicação a implementar: coordena uma operação, suas regras e transação.
 * O controller já chama estas assinaturas. Na migração para Clean, substituam
 * a dependência de DTO HTTP por entradas/saídas da aplicação e portas de saída.
 */
@ApplicationScoped
public class SolicitacaoUseCase {
    @Inject SolicitacaoCartaoRepository solicitacoes;
    @Inject UsuarioRepository usuarios;
    @Inject TipoCartaoRepository tiposCartao;

    /** Buscar titular e tipo por JPA; tipo indisponível ou pedido pendente duplicado gera 409. */
    @Transactional
    public SolicitacaoResponse criar(Long usuarioId, SolicitacaoRequest request) {
        // O bloqueio do titular serializa a verificacao e a insercao de pedidos concorrentes.
        var usuario = usuarios.buscarPorIdComBloqueio(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado"));
        var tipo = tiposCartao.buscarPorId(request.tipoCartaoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tipo de cartao nao encontrado"));
        if (!tipo.isDisponivel()) {
            throw new RegraNegocioException("Tipo de cartao indisponivel");
        }
        if (solicitacoes.existePendente(usuarioId, tipo.getId())) {
            throw new RegraNegocioException("Ja existe uma solicitacao pendente para este tipo de cartao");
        }
        var solicitacao = new SolicitacaoCartao(usuario, tipo);
        solicitacoes.salvar(solicitacao);
        return resposta(solicitacao);
    }

    /** JPQL própria com filtro de titular e status opcional, nunca listar todos os usuários. */
    @Transactional
    public List<SolicitacaoResponse> listar(Long usuarioId, StatusSolicitacao status) {
        return solicitacoes.listarPorTitularEStatus(usuarioId, status).stream()
                .map(this::resposta).toList();
    }

    /** Buscar por id E titular; retornar 404 para pedido de outro usuário. */
    @Transactional
    public SolicitacaoResponse buscar(Long usuarioId, Long id) {
        return resposta(solicitacoes.buscarPorIdETitular(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitacao nao encontrada")));
    }

    /** Em transação, buscar o próprio pedido e chamar cancelar; estado diferente de PENDENTE gera 409. */
    @Transactional
    public SolicitacaoResponse cancelar(Long usuarioId, Long id) {
        var solicitacao = solicitacoes.buscarPorIdETitularComBloqueio(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitacao nao encontrada"));
        solicitacao.cancelar();
        return resposta(solicitacao);
    }

    private SolicitacaoResponse resposta(SolicitacaoCartao solicitacao) {
        return new SolicitacaoResponse(solicitacao.getId(), solicitacao.getUsuario().getId(),
                solicitacao.getTipoCartao().getId(), solicitacao.getTipoCartao().getNome(),
                solicitacao.getSolicitadaEm(), solicitacao.getStatus());
    }

    /** Em uma transação, bloquear o pedido para escrita, conferir titular/estado, criar cartão e marcar EMITIDA. */
    public CartaoResponse emitir(Long usuarioId, Long id) {
        // TODO: implementar sem colocar SQL ou regras no controller.
        throw new EtapaNaoImplementadaException("SolicitacaoUseCase.emitir");
    }
}
