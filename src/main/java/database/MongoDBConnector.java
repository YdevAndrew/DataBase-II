package database;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoDBConnector {

    private MongoDatabase database;
    private MongoClient client;

    public MongoDBConnector() {
    }

    // Conectar ao MongoDB
    public void connect() {
        try {
            client = MongoClients.create("mongodb+srv://andre:rrX245hhh@pixelartdb.hzmie.mongodb.net/?retryWrites=true&w=majority&appName=pixelartdb");
            database = client.getDatabase("PixeRPG");
            System.out.println("Conectado ao MongoDB com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro ao conectar ao MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Retornar uma coleção específica
    public MongoCollection<Document> getCollection(String collectionName) {
        return database.getCollection(collectionName);
    }

    // Fechar a conexão
    public void close() {
        if (client != null) {
            client.close();
        }
    }
}
