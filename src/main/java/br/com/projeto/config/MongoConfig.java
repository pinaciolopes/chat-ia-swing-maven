package br.com.projeto.config;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import br.com.projeto.util.AppException;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

public final class MongoConfig {

    private static final String DATABASE_NAME = "chat_ia";
    private static MongoClient client;

    private MongoConfig() {
    }

    public static synchronized MongoDatabase getDatabase() {
        if (client == null) {
            try {
                CodecRegistry registry = fromRegistries(
                        MongoClientSettings.getDefaultCodecRegistry(),
                        fromProviders(PojoCodecProvider.builder().automatic(true).build()));

                MongoClientSettings settings = MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString(AppConfig.require("MONGODB_URI")))
                        .codecRegistry(registry)
                        .build();

                client = MongoClients.create(settings);
            } catch (AppException e) {
                throw e;
            } catch (Exception e) {
                throw new AppException("Não foi possível conectar ao banco de dados.", e);
            }
        }
        return client.getDatabase(DATABASE_NAME);
    }

    public static synchronized void close() {
        if (client != null) {
            client.close();
            client = null;
        }
    }
}
