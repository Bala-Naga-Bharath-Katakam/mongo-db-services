package org.database.services;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.UnwindOptions;
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
            // Command 8
            getHobbiesByAgeAndRemoveDuplicates(collection);
            // Command 9
            sliceTheExamScores(collection);
            // Command 10
            // Define the aggregation pipeline
            fetchSizeOfExamScores(collection);
            // Command 11
            // Define the aggregation pipeline
            fetchScoresGreaterThanSixty(collection);
            // Command 12
            getMaxScore(collection);



        }
    }

    private static void getMaxScore(MongoCollection<Document> collection) {
        // Define the aggregation pipeline
        List<Document> pipeline = Arrays.asList(
                new Document("$unwind", "$examScores"),
                new Document("$project", new Document("_id", 1)
                        .append("name", 1)
                        .append("age", 1)
                        .append("score", "$examScores.score")),
                new Document("$sort", new Document("score", -1)),
                new Document("$group", new Document("_id", "$_id")
                        .append("name", new Document("$first", "$name"))
                        .append("maxScore", new Document("$max", "$score"))),
                new Document("$sort", new Document("maxScore", -1))
        );

        // Execute the aggregation and print each document
        collection.aggregate(pipeline).forEach(doc -> System.out.println(doc.toJson()));
    }

    private static void fetchScoresGreaterThanSixty(MongoCollection<Document> collection) {
        List<Document> pipeline = Arrays.asList(
                new Document("$project", new Document("_id", 0)
                        .append("scores", new Document("$filter", new Document("input", "$examScores")
                                .append("as", "sc")
                                .append("cond", new Document("$gt", Arrays.asList("$$sc.score", 60)))
                        ))
                )
        );

        // Execute the aggregation and print each document
        collection.aggregate(pipeline).forEach(doc -> System.out.println(doc.toJson()));
    }

    private static void fetchSizeOfExamScores(MongoCollection<Document> collection) {
        List<Document> pipeline = Arrays.asList(
                new Document("$project", new Document("_id", 0)
                        .append("numScores", new Document("$size", "$examScores"))
                )
        );

        // Execute the aggregation and print each document
        collection.aggregate(pipeline).forEach(doc -> System.out.println(doc.toJson()));
    }

    private static void sliceTheExamScores(MongoCollection<Document> collection) {
        // Define the aggregation pipeline
        List<Document> pipeline = Arrays.asList(
                new Document("$project", new Document("_id", 0)
                        .append("examScore", new Document("$slice", Arrays.asList("$examScores", 2, 1)))
                )
        );

        // Execute the aggregation and print each document
        collection.aggregate(pipeline).forEach(doc -> System.out.println(doc.toJson()));
    }

    private static void getHobbiesByAgeAndRemoveDuplicates(MongoCollection<Document> collection) {
        List<Document> pipeline = Arrays.asList(
                // Stage 1: Unwind the hobbies array
                new Document("$unwind", "$hobbies"),
                // Stage 2: Group by age, adding unique hobbies to a set
                new Document("$group", new Document("_id", new Document("age", "$age"))
                        .append("allHobbies", new Document("$addToSet", "$hobbies"))
                )
        );

        collection.aggregate(pipeline).forEach(doc -> System.out.println(doc.toJson()));
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

