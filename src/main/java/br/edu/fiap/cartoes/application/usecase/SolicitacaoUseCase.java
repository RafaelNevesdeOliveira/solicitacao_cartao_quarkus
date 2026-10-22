package br.edu.fiap.cartoes.application.usecase;

import br.edu.fiap.cartoes.application.port.CartaoRepository;
import br.edu.fiap.cartoes.application.port.SolicitacaoCartaoRepository;
import br.edu.fiap.cartoes.application.port.TipoCartaoRepository;
import br.edu.fiap.cartoes.application.port.UsuarioRepository;

import br.edu.fiap.cartoes.domain.exception.RecursoNaoEncontradoException;
import br.edu.fiap.cartoes.domain.exception.RegraNegocioException;

import br.edu.fiap.cartoes.domain.model.Cartao;
import br.edu.fiap.cartoes.domain.model.SolicitacaoCartao;
import br.edu.fiap.cartoes.domain.model.StatusSolicitacao;
import br.edu.fiap.cartoes.domain.model.TipoCartao;
import br.edu.fiap.cartoes.domain.model.Usuario;

import br.edu.fiap.cartoes.presentation.dto.CartaoResponse;
import br.edu.fiap.cartoes.presentation.dto.SolicitacaoRequest;
import br.edu.fiap.cartoes.presentation.dto.SolicitacaoResponse;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

/**
 * Caso de uso responsavel pelas operacoes
 * relacionadas as solicitacoes de cartao.
 *
 * Regras de negocio:
 * - Criacao de solicitacoes sem duplicidade pendente.
 * - Consulta por usuario.
 * - Cancelamento de solicitacoes pendentes.
 * - Emissao de cartoes.
 *
 * Utiliza bloqueio pessimista para coordenar
 * operacoes concorrentes.
 */
@ApplicationScoped
public class SolicitacaoUseCase {

    private final SolicitacaoCartaoRepository solicitacaoRepository;
    private final TipoCartaoRepository tipoCartaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CartaoRepository cartaoRepository;

    @Inject
    public SolicitacaoUseCase(
            SolicitacaoCartaoRepository solicitacaoRepository,
            TipoCartaoRepository tipoCartaoRepository,
            UsuarioRepository usuarioRepository,
            CartaoRepository cartaoRepository) {

        this.solicitacaoRepository = solicitacaoRepository;
        this.tipoCartaoRepository = tipoCartaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.cartaoRepository = cartaoRepository;
    }

    /**
     * Cria uma nova solicitacao de cartao.
     *
     * Bloqueia o registro do usuario antes
     * de verificar a existencia de solicitacoes
     * pendentes para o mesmo tipo de cartao.
     *
     * O bloqueio permanece ativo ate o termino
     * da transacao.
     */
    @Transactional
    public SolicitacaoResponse criar(
            Long usuarioId,
            SolicitacaoRequest request) {

        Usuario usuario = usuarioRepository
                .buscarPorIdComBloqueio(usuarioId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Usuario nao encontrado"
                        )
                );

        TipoCartao tipo = tipoCartaoRepository
                .buscarPorId(request.tipoCartaoId())
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Tipo de cartao nao encontrado"
                        )
                );

        if (!tipo.isDisponivel()) {
            throw new RegraNegocioException(
                    "Tipo de cartao indisponivel"
            );
        }

        boolean jaTemPendente = solicitacaoRepository
                .listarPorUsuarioEStatus(
                        usuarioId,
                        StatusSolicitacao.PENDENTE
                )
                .stream()
                .anyMatch(s ->
                        s.getTipoCartao().getId().equals(tipo.getId())
                );

        if (jaTemPendente) {
            throw new RegraNegocioException(
                    "Ja existe uma solicitacao pendente para este tipo"
            );
        }

        SolicitacaoCartao nova =
                new SolicitacaoCartao(usuario, tipo);

        solicitacaoRepository.salvar(nova);

        return paraResponse(nova);
    }

    /**
     * Lista as solicitacoes de um usuario,
     * com filtro opcional por status.
     */
    @Transactional
    public List<SolicitacaoResponse> listar(
            Long usuarioId,
            StatusSolicitacao status) {

        List<SolicitacaoCartao> solicitacoes =
                status == null
                        ? solicitacaoRepository.listarPorUsuario(usuarioId)
                        : solicitacaoRepository.listarPorUsuarioEStatus(
                        usuarioId,
                        status
                );

        return solicitacoes.stream()
                .map(this::paraResponse)
                .toList();
    }

    /**
     * Consulta uma solicitacao pelo identificador.
     *
     * Somente o titular pode acessar os dados.
     */
    @Transactional
    public SolicitacaoResponse buscar(
            Long usuarioId,
            Long id) {

        SolicitacaoCartao solicitacao =
                buscarDoTitular(usuarioId, id);

        return paraResponse(solicitacao);
    }

    /**
     * Cancela uma solicitacao pendente.
     *
     * Utiliza bloqueio pessimista para evitar
     * alteracoes concorrentes.
     */
    @Transactional
    public SolicitacaoResponse cancelar(
            Long usuarioId,
            Long id) {

        SolicitacaoCartao solicitacao =
                buscarDoTitularComBloqueio(usuarioId, id);

        solicitacao.cancelar();

        solicitacaoRepository.salvar(solicitacao);

        return paraResponse(solicitacao);
    }

    /**
     * Emite um cartao para uma solicitacao pendente.
     *
     * A operacao e atomica:
     * - Bloqueia a solicitacao.
     * - Confirma a titularidade.
     * - Valida o status.
     * - Impede emissao duplicada.
     * - Marca a solicitacao como EMITIDA.
     * - Persiste o cartao com status ATIVO.
     */
    @Transactional
    public CartaoResponse emitir(
            Long usuarioId,
            Long id) {

        SolicitacaoCartao solicitacao =
                buscarDoTitularComBloqueio(usuarioId, id);

        if (solicitacao.getStatus() != StatusSolicitacao.PENDENTE) {
            throw new RegraNegocioException(
                    "Somente solicitacoes pendentes podem ser emitidas."
            );
        }

        boolean cartaoJaExiste = cartaoRepository
                .buscarPorSolicitacaoId(id)
                .isPresent();

        if (cartaoJaExiste) {
            throw new RegraNegocioException(
                    "Ja existe um cartao emitido para esta solicitacao."
            );
        }

        Cartao cartao = new Cartao(solicitacao);

        solicitacao.marcarEmitida();

        solicitacaoRepository.salvar(solicitacao);
        cartaoRepository.salvar(cartao);

        return paraCartaoResponse(cartao);
    }

    /**
     * Busca uma solicitacao e valida se
     * ela pertence ao usuario informado.
     */
    private SolicitacaoCartao buscarDoTitular(
            Long usuarioId,
            Long id) {

        return solicitacaoRepository.buscarPorId(id)
                .filter(s ->
                        s.getUsuario().getId().equals(usuarioId)
                )
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Solicitacao nao encontrada"
                        )
                );
    }

    /**
     * Busca uma solicitacao com bloqueio pessimista
     * e valida a titularidade.
     *
     * Deve ser chamado dentro de uma transacao ativa.
     */
    private SolicitacaoCartao buscarDoTitularComBloqueio(
            Long usuarioId,
            Long id) {

        return solicitacaoRepository
                .buscarPorIdComBloqueio(id)
                .filter(s ->
                        s.getUsuario().getId().equals(usuarioId)
                )
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Solicitacao nao encontrada"
                        )
                );
    }

    /**
     * Converte a entidade para o DTO de solicitacao.
     */
    private SolicitacaoResponse paraResponse(
            SolicitacaoCartao solicitacao) {

        return new SolicitacaoResponse(
                solicitacao.getId(),
                solicitacao.getUsuario().getId(),
                solicitacao.getTipoCartao().getId(),
                solicitacao.getTipoCartao().getNome(),
                solicitacao.getSolicitadaEm(),
                solicitacao.getStatus()
        );
    }

    /**
     * Converte a entidade Cartao para o DTO HTTP.
     */
    private CartaoResponse paraCartaoResponse(
            Cartao cartao) {

        SolicitacaoCartao solicitacao =
                cartao.getSolicitacao();

        TipoCartao tipo =
                solicitacao.getTipoCartao();

        return new CartaoResponse(
                cartao.getId(),
                solicitacao.getId(),
                tipo.getId(),
                tipo.getNome(),
                cartao.getEmitidoEm(),
                cartao.getStatus()
        );
    }
}
