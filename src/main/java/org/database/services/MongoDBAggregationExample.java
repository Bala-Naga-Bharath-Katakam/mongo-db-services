package org.database.services;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Accumulators;
import org.bson.Document;

import java.util.Arrays;

public class MongoDBAggregationExample {
    private static final String DATABASE_NAME = "school"; // Change to your database name
    private static final String COLLECTION_NAME = "students"; // Change to your collection name

    public static void main(String[] args) {
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

            // Aggregation Pipeline
            collection.aggregate(Arrays.asList(
                    Aggregates.match(Filters.eq("gender", "female")), // Match female persons
                    Aggregates.group("$location.state", Accumulators.sum("totalPersons", 1)) // Group by state and count
            )).forEach(doc -> System.out.println(doc.toJson()));
        }
    }
}
