package br.com.projeto.service;

import br.com.projeto.model.TokenRecuperacaoSenha;
import br.com.projeto.model.Usuario;
import br.com.projeto.repository.TokenRecuperacaoRepository;
import br.com.projeto.repository.UsuarioRepository;
import br.com.projeto.util.AppException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Recuperação de senha por token temporário. A senha antiga nunca é exibida.
 * O banco guarda só o hash do token, que expira em 15 minutos e só vale uma vez.
 */
public class RecuperacaoSenhaService {

    private static final Duration VALIDADE = Duration.ofMinutes(15);

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();
    private final TokenRecuperacaoRepository tokenRepository = new TokenRecuperacaoRepository();
    private final SecureRandom aleatorio = new SecureRandom();

    /**
     * Gera um código de recuperação se o e-mail existir; senão devolve vazio.
     * A tela deve exibir SEMPRE a mesma mensagem neutra ("se o e-mail existir, o código foi gerado"),
     * para não revelar quais e-mails estão cadastrados.
     */
    public Optional<String> solicitar(String email) {
        if (email == null || email.isBlank()) {
            throw new AppException("Informe o e-mail.");
        }

        Optional<Usuario> usuario = usuarioRepository.buscarPorEmail(email.trim().toLowerCase());
        if (usuario.isEmpty()) {
            return Optional.empty();
        }

        // um novo pedido cancela os códigos anteriores ainda pendentes
        tokenRepository.invalidarPendentes(usuario.get().getId());

        byte[] bytes = new byte[32];
        aleatorio.nextBytes(bytes);
        String codigo = HexFormat.of().formatHex(bytes);

        TokenRecuperacaoSenha token = new TokenRecuperacaoSenha();
        token.setUserId(usuario.get().getId());
        token.setTokenHash(calcularHash(codigo));
        token.setExpiresAt(Instant.now().plus(VALIDADE));
        token.setUsed(false);
        tokenRepository.inserir(token);

        return Optional.of(codigo);
    }

    public void redefinirSenha(String codigo, String novaSenha, String confirmacao) {
        if (codigo == null || codigo.isBlank()) {
            throw new AppException("Informe o código de recuperação.");
        }
        AuthenticationService.validarSenha(novaSenha);
        if (!novaSenha.equals(confirmacao)) {
            throw new AppException("A confirmação da senha não confere.");
        }

        TokenRecuperacaoSenha token = tokenRepository.buscarValidoPorHash(calcularHash(codigo.trim()))
                .orElseThrow(() -> new AppException("Código inválido ou expirado."));

        usuarioRepository.atualizarSenhaHash(token.getUserId(), BCrypt.hashpw(novaSenha, BCrypt.gensalt(12)));
        tokenRepository.marcarComoUsado(token.getId());
        tokenRepository.invalidarPendentes(token.getUserId());
    }

    private static String calcularHash(String texto) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(sha256.digest(texto.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}