package br.com.projeto.service;

import br.com.projeto.model.Usuario;
import br.com.projeto.repository.UsuarioRepository;
import br.com.projeto.util.AppException;
import com.mongodb.MongoWriteException;
import java.time.Instant;
import java.util.regex.Pattern;
import org.mindrot.jbcrypt.BCrypt;

public class AuthenticationService {

    public static final int TAMANHO_MINIMO_SENHA = 8;
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();

    public Usuario cadastrar(String nome, String email, String senha, String confirmacao) {
        if (vazio(nome) || vazio(email) || vazio(senha) || vazio(confirmacao)) {
            throw new AppException("Preencha todos os campos.");
        }

        String emailNormalizado = email.trim().toLowerCase();
        if (!EMAIL.matcher(emailNormalizado).matches()) {
            throw new AppException("E-mail inválido.");
        }

        validarSenha(senha);

        if (!senha.equals(confirmacao)) {
            throw new AppException("A confirmação da senha não confere.");
        }

        if (usuarioRepository.buscarPorEmail(emailNormalizado).isPresent()) {
            throw new AppException("Este e-mail já está cadastrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setName(nome.trim());
        usuario.setEmail(emailNormalizado);
        usuario.setPasswordHash(BCrypt.hashpw(senha, BCrypt.gensalt(12)));
        usuario.setCreatedAt(Instant.now());

        try {
            return usuarioRepository.inserir(usuario);
        } catch (MongoWriteException e) {
            throw new AppException("Este e-mail já está cadastrado.", e);
        }
    }

    public Usuario login(String email, String senha) {
        if (vazio(email) || senha == null || senha.isEmpty()) {
            throw new AppException("Informe e-mail e senha.");
        }

        Usuario usuario = usuarioRepository.buscarPorEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new AppException("E-mail ou senha inválidos."));

        if (!BCrypt.checkpw(senha, usuario.getPasswordHash())) {
            throw new AppException("E-mail ou senha inválidos.");
        }

        SessionManagerService.getInstance().iniciar(usuario);
        return usuario;
    }

    public void logout() {
        SessionManagerService.getInstance().encerrar();
    }

    public static void validarSenha(String senha) {
        if (senha == null || senha.length() < TAMANHO_MINIMO_SENHA) {
            throw new AppException("A senha deve ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres.");
        }
    }

    private static boolean vazio(String texto) {
        return texto == null || texto.isBlank();
    }
}