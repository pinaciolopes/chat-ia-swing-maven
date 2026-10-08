package br.com.projeto.service;

import br.com.projeto.model.Mensagem;
import br.com.projeto.repository.ConversaRepository;
import br.com.projeto.repository.MensagemRepository;
import br.com.projeto.util.AppException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;

/** Fluxo: pergunta -> RAGService -> GroqService -> resposta -> MongoDB. */
public class ChatService {

    private static final int LIMITE_HISTORICO = 10;
    private static final int TAMANHO_MAXIMO_MENSAGEM = 4000;

    private final ConversaRepository conversaRepository = new ConversaRepository();
    private final MensagemRepository mensagemRepository = new MensagemRepository();
    private final RAGService ragService = new RAGService();
    private final GroqService groqService = new GroqService();

    /**
     * Salva a pergunta, consulta a IA e salva a resposta, que é devolvida.
     * Pode demorar: na interface, chame fora da thread principal do JavaFX.
     */
    public Mensagem enviar(ObjectId conversaId, String texto) {
        if (texto == null || texto.isBlank()) {
            throw new AppException("Digite uma pergunta.");
        }
        if (texto.length() > TAMANHO_MAXIMO_MENSAGEM) {
            throw new AppException("A mensagem é muito longa (máximo de " + TAMANHO_MAXIMO_MENSAGEM + " caracteres).");
        }

        ObjectId usuarioId = SessionManagerService.getInstance().exigirUsuario().getId();

        // a conversa precisa existir e ser do usuário logado
        conversaRepository.buscarPorIdEDono(conversaId, usuarioId)
                .orElseThrow(() -> new AppException("Conversa não encontrada."));

        // 1. salva a pergunta
        try {
            mensagemRepository.inserir(montar(usuarioId, conversaId, Mensagem.PAPEL_USUARIO, texto.trim()));
        } catch (RuntimeException e) {
            throw new AppException("Erro ao salvar a mensagem.", e);
        }

        // 2. RAG: contexto da base de conhecimento (vazio se nada foi encontrado)
        String contexto = ragService.recuperarContexto(texto);

        // 3. histórico recente (já inclui a pergunta que acabou de ser salva)
        List<Mensagem> historico = mensagemRepository.listarPorConversa(conversaId, usuarioId);
        List<Mensagem> recentes = historico.subList(Math.max(0, historico.size() - LIMITE_HISTORICO), historico.size());

        String instrucao = "Você é um assistente útil. Responda em português.";
        if (!contexto.isBlank()) {
            instrucao += "\n\nUse o contexto abaixo quando for relevante para a pergunta:\n" + contexto;
        }

        List<Map<String, String>> payload = new ArrayList<>();
        payload.add(Map.of("role", "system", "content", instrucao));
        for (Mensagem m : recentes) {
            payload.add(Map.of("role", m.getRole(), "content", m.getContent()));
        }

        // 4. IA
        String resposta = groqService.perguntar(payload);

        // 5. salva a resposta e atualiza a data da conversa
        Mensagem mensagemIA = montar(usuarioId, conversaId, Mensagem.PAPEL_ASSISTENTE, resposta);
        try {
            mensagemRepository.inserir(mensagemIA);
            conversaRepository.atualizarData(conversaId, usuarioId);
        } catch (RuntimeException e) {
            throw new AppException("Erro ao salvar a resposta.", e);
        }
        return mensagemIA;
    }

    private Mensagem montar(ObjectId usuarioId, ObjectId conversaId, String papel, String conteudo) {
        Mensagem mensagem = new Mensagem();
        mensagem.setUserId(usuarioId);
        mensagem.setConversationId(conversaId);
        mensagem.setRole(papel);
        mensagem.setContent(conteudo);
        mensagem.setCreatedAt(Instant.now());
        return mensagem;
    }
}