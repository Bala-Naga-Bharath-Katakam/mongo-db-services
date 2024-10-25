package org.database.services;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.Arrays;
import java.util.List;

public class FriendsAggregationService {
    public static void main(String[] args) {
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("school");  // Replace with your database name
            MongoCollection<Document> collection = database.getCollection("friends");

            // Command 7
            getHobbiesByAge(collection);
        }
    }

    private static void getHobbiesByAge(MongoCollection<Document> collection) {
        List<Document> pipeline = Arrays.asList(
                // Stage 1: Unwind the hobbies array
                new Document("$unwind", "$hobbies"),
                // Stage 2: Group by age and collect all hobbies into a list
                new Document("$group", new Document("_id", new Document("age", "$age"))
                        .append("allHobbies", new Document("$push", "$hobbies"))
                )
        );

        collection.aggregate(pipeline).forEach(doc -> System.out.println(doc.toJson()));
    }
}

