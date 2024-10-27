package org.database.services;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JoinsService {
    public static void main(String[] args) {
        performJoins();

    }

    private static void performJoins() {
        try (var mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("school");
            MongoCollection<Document> authorsCollection = database.getCollection("authors");

            Document lookupStage = new Document("$lookup",
                    new Document("from", "books")
                            .append("localField", "books")
                            .append("foreignField", "_id")
                            .append("as", "bookDetails")
            );

            // Use this match stage if we need inner join
            Document matchStage = new Document("$match", new Document("bookDetails", new Document("$ne", new ArrayList<>())));

            List<Document> pipeline = Arrays.asList(lookupStage, matchStage);
            List<Document> results = authorsCollection.aggregate(pipeline).into(new ArrayList<>());

            if (results.isEmpty()) {
                System.out.println("No results found in the join operation.");
            } else {
                System.out.println("Join results:");
                for (Document doc : results) {
                    System.out.println(doc.toJson());
                }
            }
        }
    }
}

