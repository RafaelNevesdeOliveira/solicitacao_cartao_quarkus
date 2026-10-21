package br.edu.fiap.cartoes.security;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import br.edu.fiap.cartoes.dto.TokenResponse;
import br.edu.fiap.cartoes.entity.Usuario;
import br.edu.fiap.cartoes.exception.EtapaNaoImplementadaException;

import java.time.Instant;

/**
 * Adaptador de emissão a implementar com io.smallrye.jwt.build.Jwt e HS256.
 * Verificação Bearer já é feita pelo Quarkus. Assinar com o mesmo segredo configurado.
 * Claims obrigatórios: iss, sub (email), iat, exp e usuarioId. Nunca incluir senha/hash.
 * Na migração Clean, criar porta EmissorToken para que o use case não conheça SmallRye.
 */
@ApplicationScoped
public class JwtService {
    @ConfigProperty(name="security.jwt.secret") String segredo;
    @ConfigProperty(name="security.jwt.issuer") String emissor;
    @ConfigProperty(name="security.jwt.expiration-seconds") long duracao;

    /** TODO emitir TokenResponse com Bearer, duração e expiração coerentes com o JWT. */
    public TokenResponse emitir(Usuario usuario) {
        long agoraEpoch = Instant.now().getEpochSecond();
        long expiracaoEpoch = agoraEpoch + duracao;

        String accessToken = Jwt.issuer(emissor)
                .subject(usuario.getEmail())
                .upn(usuario.getEmail())
                .issuedAt(agoraEpoch)
                .expiresAt(expiracaoEpoch)
                .claim("usuarioId", usuario.getId())
                .sign();

        return new TokenResponse(accessToken, "Bearer", duracao, expiracaoEpoch);
    }
}
