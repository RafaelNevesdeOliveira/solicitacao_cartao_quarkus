Qpackage br.edu.fiap.cartoes.usecase;

import br.edu.fiap.cartoes.dto.CartaoResponse;
import br.edu.fiap.cartoes.entity.Cartao;
import br.edu.fiap.cartoes.repository.CartaoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class CartaoUseCase {

    @Inject
    CartaoRepository cartoes;

    /**
     * Lista somente os cartões pertencentes ao titular informado.
     */
    @Transactional
    public List<CartaoResponse> listar(Long usuarioId) {
        return cartoes.listarPorTitular(usuarioId).stream()
                .map(this::resposta)
                .toList();
    }

    private CartaoResponse resposta(Cartao cartao) {
        var solicitacao = cartao.getSolicitacao();
        var tipo = solicitacao.getTipoCartao();

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
