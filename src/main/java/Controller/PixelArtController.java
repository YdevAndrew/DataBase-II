package Controller;

import Main.PixelArt;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PixelArtController {

    private MongoCollection<Document> pixelArtCollection;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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

    // Método para obter todas as PixelArts do banco de dados, agora com data de início e fim
    public List<PixelArt> getAllPixelArts() {
        List<PixelArt> pixelArts = new ArrayList<>();
        for (Document doc : pixelArtCollection.find()) {
            String name = doc.getString("name");
            boolean isCompleted = doc.getBoolean("isCompleted", false);
            String status = doc.getString("status");

            // Recuperar datas
            String startDateStr = doc.getString("startDate");
            String endDateStr = doc.getString("endDate");
            LocalDate startDate = (startDateStr != null) ? LocalDate.parse(startDateStr, formatter) : null;
            LocalDate endDate = (endDateStr != null) ? LocalDate.parse(endDateStr, formatter) : null;

            pixelArts.add(new PixelArt(name, status, startDate, endDate));
        }
        return pixelArts;
    }

    // Método para adicionar uma nova PixelArt com data de início e fim
    public void addPixelArt(PixelArt pixelArt) {
        Document doc = new Document("name", pixelArt.getName())
                .append("isCompleted", pixelArt.isCompleted())
                .append("status", pixelArt.getStatus())
                .append("startDate", pixelArt.getStartDate() != null ? pixelArt.getStartDate().format(formatter) : null)
                .append("endDate", pixelArt.getEndDate() != null ? pixelArt.getEndDate().format(formatter) : null);
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
