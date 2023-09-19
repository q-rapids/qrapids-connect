package connect.mongodb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoDBApi {
    private static final int PORT = 27017;
    private static final String DATABASE = "mongo";

    public static List<Document> getTaskReference(String collectionName, int reference) {
        MongoClient mongoClient = MongoClients.create("mongodb://mongodb :" + PORT);
        MongoDatabase database = mongoClient.getDatabase(DATABASE);
        MongoCollection<Document> collection = database.getCollection(collectionName);
        Document query = new Document("reference", new Document("$eq", reference));

        FindIterable<Document> response = collection.find(query);
        List<Document> results = new ArrayList<>();
        for (Document document : response) results.add(document);
        mongoClient.close();
        return results;
    }

    public static void main(String[] args) throws IOException {
        List<Document> results = getTaskReference("taiga_asw_asw11a.tasks", 3);
        System.out.println("Size: " + results.size());
        for (Document result : results) {
            System.out.println(result.get("id").toString());
            System.out.println(result.get("reference").toString());
        }
    }
}
