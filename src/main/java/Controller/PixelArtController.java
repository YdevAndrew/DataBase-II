package Controller;

import Main.PixelArt;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class PixelArtController {

    private MongoCollection<Document> pixelArtCollection;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final byte[] encryptionKey = "MySuperSecretKey".getBytes(); // Exemplo de chave de criptografia

    public void connectToMongoDB() {
        MongoClient mongoClient = MongoClients.create("mongodb+srv://andre:rrX245hhh@pixelartdb.hzmie.mongodb.net/?retryWrites=true&w=majority&appName=pixelartdb");
        MongoDatabase database = mongoClient.getDatabase("pixelartdb");
        pixelArtCollection = database.getCollection("pixelarts");
    }

    // Método para criptografar dados
    private String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        SecretKey secretKey = new SecretKeySpec(encryptionKey, ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // Método para descriptografar dados
    private String decrypt(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        SecretKey secretKey = new SecretKeySpec(encryptionKey, ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = Base64.getDecoder().decode(encryptedData);
        return new String(cipher.doFinal(decryptedBytes));
    }

    // Método para atualizar o status da PixelArt no banco de dados
    public void updatePixelArtStatus(String name, String status) {
        try {
            Document query = new Document("name", encrypt(name)); // Criptografar nome na consulta
            Document update = new Document("$set", new Document("status", encrypt(status))); // Criptografar status
            pixelArtCollection.updateOne(query, update);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para obter todas as PixelArts do banco de dados, agora com data de início e fim
    public List<PixelArt> getAllPixelArts() {
        List<PixelArt> pixelArts = new ArrayList<>();
        for (Document doc : pixelArtCollection.find()) {
            try {
                String name = decrypt(doc.getString("name"));    // Descriptografando o nome
                String status = decrypt(doc.getString("status"));  // Descriptografando o status

                // Recuperar datas
                String startDateStr = doc.getString("startDate");
                String endDateStr = doc.getString("endDate");
                LocalDate startDate = (startDateStr != null) ? LocalDate.parse(startDateStr, formatter) : null;
                LocalDate endDate = (endDateStr != null) ? LocalDate.parse(endDateStr, formatter) : null;

                pixelArts.add(new PixelArt(name, status, startDate, endDate));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return pixelArts;
    }

    // Método para adicionar uma nova PixelArt com data de início e fim
    public void addPixelArt(PixelArt pixelArt) {
        try {
            Document doc = new Document("name", encrypt(pixelArt.getName()))  // Criptografando o nome
                    .append("isCompleted", pixelArt.isCompleted())
                    .append("status", encrypt(pixelArt.getStatus()))          // Criptografando o status
                    .append("startDate", pixelArt.getStartDate() != null ? pixelArt.getStartDate().format(formatter) : null)
                    .append("endDate", pixelArt.getEndDate() != null ? pixelArt.getEndDate().format(formatter) : null);
            pixelArtCollection.insertOne(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para remover uma PixelArt
    public void removePixelArt(String name) {
        try {
            Document query = new Document("name", encrypt(name)); // Criptografar nome na consulta
            pixelArtCollection.deleteOne(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para editar o nome de uma PixelArt
    public void editPixelArt(String oldName, String newName) {
        try {
            Document query = new Document("name", encrypt(oldName)); // Criptografar nome original
            Document update = new Document("$set", new Document("name", encrypt(newName))); // Criptografar novo nome
            pixelArtCollection.updateOne(query, update);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
