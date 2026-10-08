package br.com.projeto.service;

import br.com.projeto.config.AppConfig;
import br.com.projeto.util.AppException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class GroqService {

    private static final URI ENDPOINT = URI.create("https://api.groq.com/openai/v1/chat/completions");

    /** Se a Groq descontinuar este modelo, troque por outro da lista em console.groq.com. */
    private static final String MODELO = "openai/gpt-oss-20b";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /** Cada mensagem tem as chaves "role" (system, user ou assistant) e "content". */
    public String perguntar(List<Map<String, String>> mensagens) {
        String apiKey = AppConfig.require("GROQ_API_KEY");

        JsonObject corpo = new JsonObject();
        corpo.addProperty("model", MODELO);
        corpo.add("messages", new Gson().toJsonTree(mensagens));

        HttpRequest requisicao = HttpRequest.newBuilder(ENDPOINT)
                .timeout(Duration.ofSeconds(60))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corpo.toString()))
                .build();

        try {
            HttpResponse<String> resposta = http.send(requisicao, HttpResponse.BodyHandlers.ofString());
            int status = resposta.statusCode();

            if (status == 401) {
                throw new AppException("API Key da Groq inválida.");
            }
            if (status == 429) {
                throw new AppException("Limite de requisições da Groq atingido. Tente novamente em instantes.");
            }
            if (status >= 500) {
                throw new AppException("O serviço da Groq está indisponível no momento.");
            }
            if (status != 200) {
                throw new AppException("Erro ao consultar a IA (código " + status + ").");
            }

            return JsonParser.parseString(resposta.body()).getAsJsonObject()
                    .getAsJsonArray("choices").get(0).getAsJsonObject()
                    .getAsJsonObject("message").get("content").getAsString();

        } catch (IOException e) {
            throw new AppException("Sem conexão com a internet ou com a API da Groq.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AppException("A requisição foi interrompida.", e);
        } catch (AppException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new AppException("Resposta inesperada da API da Groq.", e);
        }
    }
}