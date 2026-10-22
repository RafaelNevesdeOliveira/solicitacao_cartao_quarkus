
package br.edu.fiap.cartoes.application.usecase;

import br.edu.fiap.cartoes.application.port.CartaoRepository;
import br.edu.fiap.cartoes.domain.model.Cartao;
import br.edu.fiap.cartoes.domain.model.SolicitacaoCartao;
import br.edu.fiap.cartoes.domain.model.TipoCartao;
import br.edu.fiap.cartoes.presentation.dto.CartaoResponse;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class CartaoUseCase {

    private final CartaoRepository repository;

    @Inject
    public CartaoUseCase(CartaoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public List<CartaoResponse> listar(Long usuarioId) {
        return repository.listarPorUsuario(usuarioId)
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    private CartaoResponse converterParaResponse(Cartao cartao) {

        SolicitacaoCartao solicitacao = cartao.getSolicitacao();
        TipoCartao tipo = solicitacao.getTipoCartao();

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
