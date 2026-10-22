package br.edu.fiap.cartoes.application.port;

import br.edu.fiap.cartoes.domain.model.TipoCartao;

import java.util.List;

/**
 * Contrato de consulta dos tipos de cartao disponiveis.
 *
 * Define a operacao de leitura utilizada
 * pela implementacao JDBC.
 *
 * Nao expoe detalhes de infraestrutura,
 * como Connection ou ResultSet.
 */
public interface ConsultaTiposDisponiveis {

    /**
     * Consulta todos os tipos de cartao disponiveis.
     *
     * @return lista de tipos de cartao disponiveis.
     *         Retorna uma lista vazia caso nao existam.
     */
    List<TipoCartao> listarDisponiveis();
}
