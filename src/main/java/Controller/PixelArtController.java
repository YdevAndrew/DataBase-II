package Controller;

import Main.PixelArt;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class PixelArtController {

    private MongoCollection<Document> pixelArtCollection;

    public void connectToMongoDB() {
        MongoClient mongoClient = MongoClients.create("mongodb+srv://andre:rrX245hhh@pixelartdb.hzmie.mongodb.net/?retryWrites=true&w=majority&appName=pixelartdb");
        MongoDatabase database = mongoClient.getDatabase("pixelartdb");
        pixelArtCollection = database.getCollection("pixelarts");
    }

    // Método para atualizar o status da PixelArt no banco de dados
    public void updatePixelArtStatus(String name, String status) {
        Document query = new Document("name", name);
        Document update = new Document("$set", new Document("status", status));
        pixelArtCollection.updateOne(query, update);
    }

    // Método para obter todas as PixelArts do banco de dados
    public List<PixelArt> getAllPixelArts() {
        List<PixelArt> pixelArts = new ArrayList<>();
        for (Document doc : pixelArtCollection.find()) {
            String name = doc.getString("name");
            boolean isCompleted = doc.getBoolean("isCompleted", false);
            String status = doc.getString("status");
            pixelArts.add(new PixelArt(name, isCompleted, status));
        }
        return pixelArts;
    }

    public void addPixelArt(PixelArt pixelArt) {
        Document doc = new Document("name", pixelArt.getName())
                .append("isCompleted", pixelArt.isCompleted())
                .append("status", pixelArt.getStatus());
        pixelArtCollection.insertOne(doc);
    }

    public void removePixelArt(String name) {
        Document query = new Document("name", name);
        pixelArtCollection.deleteOne(query);
    }

    public void editPixelArt(String oldName, String newName) {
        Document query = new Document("name", oldName);
        Document update = new Document("$set", new Document("name", newName));
        pixelArtCollection.updateOne(query, update);
    }
}
