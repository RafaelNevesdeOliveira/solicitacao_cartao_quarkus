package br.edu.fiap.cartoes.repository;

import br.edu.fiap.cartoes.entity.Cartao;
import java.util.List;

public interface CartaoRepository {

    void salvar(Cartao cartao);

    boolean existePorSolicitacao(Long solicitacaoId);

    List<Cartao> listarPorTitular(Long usuarioId);
}
