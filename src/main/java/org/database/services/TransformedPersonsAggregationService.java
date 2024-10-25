package org.database.services;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.GeoNearOptions;
import org.bson.Document;

import java.util.Arrays;
import java.util.List;

public class TransformedPersonsAggregationService {
    public static void main(String[] args) {
        // Connect to the MongoDB server
        try (var mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            // Access the database and collection
            MongoDatabase database = mongoClient.getDatabase("school");
            MongoCollection<Document> collection = database.getCollection("transformedPersons");

            // Create the coordinates as a Document
            Document coordinates = new Document("type", "Point")
                    .append("coordinates", Arrays.asList(-42.8, -18.4)); // Longitude first, then latitude

            // Create the $geoNear stage
            Document geoNearStage = new Document("$geoNear", new Document()
                    .append("near", coordinates)
                    .append("maxDistance", 1000000) // max distance in meters
                    .append("spherical", true) // use spherical calculations
                    .append("distanceField", "distance") // field to store distance
                    .append("query", new Document("age", new Document("$gt", 30))) // optional query
            );

            // Perform the aggregation with the $geoNear stage
            var results = collection.aggregate(Arrays.asList(geoNearStage));

            // Print the results
            results.forEach(doc -> System.out.println(doc.toJson()));
        } catch (Exception e) {
            System.err.println("Error during geoNear operation: " + e.getMessage());
        }
    }
}
