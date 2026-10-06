package br.com.projeto.service;

import br.com.projeto.model.Conversa;
import br.com.projeto.model.Mensagem;
import br.com.projeto.repository.ConversaRepository;
import br.com.projeto.repository.MensagemRepository;
import br.com.projeto.util.AppException;
import java.time.Instant;
import java.util.List;
import org.bson.types.ObjectId;

public class ConversaService {

    private static final int TAMANHO_MAXIMO_TITULO = 80;

    private final ConversaRepository conversaRepository = new ConversaRepository();
    private final MensagemRepository mensagemRepository = new MensagemRepository();

    /** O dono é SEMPRE o usuário da sessão, nunca um id vindo da interface. */
    private ObjectId idDoUsuarioLogado() {
        return SessionManagerService.getInstance().exigirUsuario().getId();
    }

    public Conversa criar() {
        Conversa conversa = new Conversa();
        conversa.setOwnerId(idDoUsuarioLogado());
        conversa.setTitle("Nova conversa");
        conversa.setCreatedAt(Instant.now());
        conversa.setUpdatedAt(Instant.now());
        return conversaRepository.inserir(conversa);
    }

    public List<Conversa> listarMinhas() {
        return conversaRepository.listarPorDono(idDoUsuarioLogado());
    }

    public void renomear(ObjectId conversaId, String novoTitulo) {
        if (novoTitulo == null || novoTitulo.isBlank()) {
            throw new AppException("O título não pode ser vazio.");
        }
        String titulo = novoTitulo.trim();
        if (titulo.length() > TAMANHO_MAXIMO_TITULO) {
            throw new AppException("O título deve ter no máximo " + TAMANHO_MAXIMO_TITULO + " caracteres.");
        }
        if (!conversaRepository.renomear(conversaId, idDoUsuarioLogado(), titulo)) {
            throw new AppException("Conversa não encontrada.");
        }
    }

    public void excluir(ObjectId conversaId) {
        ObjectId usuarioId = idDoUsuarioLogado();
        // primeiro valida a propriedade e apaga a conversa; só então apaga as mensagens
        if (!conversaRepository.excluir(conversaId, usuarioId)) {
            throw new AppException("Conversa não encontrada.");
        }
        mensagemRepository.excluirPorConversa(conversaId, usuarioId);
    }

    public List<Mensagem> carregarMensagens(ObjectId conversaId) {
        ObjectId usuarioId = idDoUsuarioLogado();
        conversaRepository.buscarPorIdEDono(conversaId, usuarioId)
                .orElseThrow(() -> new AppException("Conversa não encontrada."));
        return mensagemRepository.listarPorConversa(conversaId, usuarioId);
    }
}