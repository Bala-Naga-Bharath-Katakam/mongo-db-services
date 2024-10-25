package org.database.services;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Accumulators;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MongoDBAggregationService {
    private static final String DATABASE_NAME = "school"; // Change to your database name
    private static final String COLLECTION_NAME = "students"; // Change to your collection name

    public static void main(String[] args) {
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

            countNumberOfPersonsPerState(collection);
            aggregatePeopleByState(collection);
        }
    }

    private static void countNumberOfPersonsPerState(MongoCollection<Document> collection) {
        // Aggregation Pipeline
        collection.aggregate(Arrays.asList(
                Aggregates.match(Filters.eq("gender", "female")), // Match female persons
                Aggregates.group("$location.state", Accumulators.sum("totalPersons", 1)) // Group by state and count
        )).forEach(doc -> System.out.println(doc.toJson()));
    }

    private static void aggregatePeopleByState(MongoCollection<Document> collection) {
        // Perform the aggregation
        List<Document> results = collection.aggregate(Arrays.asList(
                Aggregates.match(Filters.eq("gender", "female")), // Match female persons
                Aggregates.group("$location.state",
                        Accumulators.push("people", "$$ROOT")) // Group by state and push documents into a list
        )).into(new ArrayList<>());

        // Functional programming approach to collect into a Map<String, List<Document>>
        Map<String, List<Document>> stateToPeopleMap = results.stream()
                .collect(Collectors.toMap(
                        doc -> doc.getString("_id"), // Use state as key
                        doc -> (List<Document>) doc.get("people") // Use list of persons as value
                ));

        // Print the results using functional style
        stateToPeopleMap.forEach((state, people) -> {
            System.out.println("State: " + state);
            people.forEach(person ->
                    System.out.println("  Person: " + person.toJson())
            );
        });
    }
}
