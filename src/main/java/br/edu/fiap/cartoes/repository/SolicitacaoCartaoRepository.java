package br.edu.fiap.cartoes.repository;

import java.util.List;
import java.util.Optional;
import br.edu.fiap.cartoes.entity.SolicitacaoCartao;
import br.edu.fiap.cartoes.entity.StatusSolicitacao;
/**
 * Interface de persistência a completar pelo grupo. Não há Spring Data: declarar o
 * nome de um método não cria sua implementação. Escrevam os contratos usados pelos
 * use cases e implementem a classe concreta. Ao migrar, movam o contrato para uma
 * porta da aplicação, sem tipos de EntityManager, ResultSet ou DTO HTTP.
 */
public interface SolicitacaoCartaoRepository {
    void salvar(SolicitacaoCartao solicitacao);
    Optional<SolicitacaoCartao> buscarPorIdETitular(Long id, Long usuarioId);
    Optional<SolicitacaoCartao> buscarPorIdETitularComBloqueio(Long id, Long usuarioId);
    List<SolicitacaoCartao> listarPorTitularEStatus(Long usuarioId, StatusSolicitacao status);
    boolean existePendente(Long usuarioId, Long tipoCartaoId);
}
