
package br.edu.fiap.cartoes.application.usecase;

import br.edu.fiap.cartoes.application.port.ConsultaTiposDisponiveis;
import br.edu.fiap.cartoes.presentation.dto.TipoCartaoResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class TipoCartaoUseCase {

    private final ConsultaTiposDisponiveis consultaTipos;

    @Inject
    public TipoCartaoUseCase(ConsultaTiposDisponiveis consultaTipos) {
        this.consultaTipos = consultaTipos;
    }

    public List<TipoCartaoResponse> listarDisponiveis() {
        return consultaTipos.listarDisponiveis()
                .stream()
                .map(tipo -> new TipoCartaoResponse(tipo.getId(), tipo.getNome(), tipo.getDescricao(), tipo.isDisponivel()))
                .toList();
    }
}
