
package br.edu.fiap.cartoes.repository;

import br.edu.fiap.cartoes.entity.TipoCartao;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador JDBC para consulta de tipos de cartao.
 *
 * Implementa o contrato ConsultaTiposDisponiveis.
 *
 * Utiliza AgroalDataSource, PreparedStatement
 * e try-with-resources.
 *
 * Nao executa regras de negocio,
 * nao chama controllers e nao gera tokens.
 */
@ApplicationScoped
public class TipoCartaoRepositoryJdbc
        implements ConsultaTiposDisponiveis {

    @Inject
    AgroalDataSource dataSource;

    /**
     * Consulta todos os tipos de cartao
     * que estao disponiveis para solicitacao.
     */
    @Override
    public List<TipoCartao> listarDisponiveis() {

        String sql = """
                SELECT id, nome, descricao, disponivel
                FROM tipos_cartao
                WHERE disponivel = ?
                ORDER BY nome
                """;

        List<TipoCartao> tiposDisponiveis =
                new ArrayList<>();

        try (
                Connection connection =
                        dataSource.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setBoolean(1, true);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    TipoCartao tipoCartao =
                            new TipoCartao(
                                    resultSet.getString("nome"),
                                    resultSet.getString("descricao"),
                                    resultSet.getBoolean("disponivel")
                            );

                    tipoCartao.setId(
                            resultSet.getLong("id")
                    );

                    tiposDisponiveis.add(tipoCartao);
                }
            }

            return tiposDisponiveis;

        } catch (SQLException exception) {

            throw new IllegalStateException(
                    "Erro ao consultar tipos de cartao disponiveis.",
                    exception
            );
        }
    }
}
