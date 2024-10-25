package org.database.services;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StudentsAggregationService {
    private static final String DATABASE_NAME = "school"; // Change to your database name
    private static final String COLLECTION_NAME = "students"; // Change to your collection name

    public static void main(String[] args) {
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

            // command 1 & 2
            countNumberOfPersonsPerState(collection);
            aggregatePeopleByState(collection);
            //command 3
            aggregateAndTransformName(collection);
            // command 4
            aggregateAndConvertDataTypesOfDOBAndPoints(collection);

            // command 6
            aggregateByBirthYearAndSortByDESC(collection);
            // Command 13
            averagePersonsAgePerBucket(collection);
            // Command 14
            fetchTenRecordsSortByBirthData(collection);
            // Command 15
            performTransformationSentToTransformedCollection(collection);


        }
    }

    private static void performTransformationSentToTransformedCollection(MongoCollection<Document> collection) {
        // Define the aggregation pipeline
        var pipeline = Arrays.asList(
                // First $project stage
                Aggregates.project(Projections.fields(
                        Projections.excludeId(),
                        Projections.include("name", "email", "dob", "gender", "location"),
                        Projections.computed("birthdate", new Document("$toDate", "$dob.date")),
                        Projections.computed("age", "$dob.age"),
                        Projections.computed("location", new Document("type", "Point").append("coordinates", Arrays.asList(
                                new Document("$convert", new Document("input", "$location.coordinates.longitude").append("to", "double").append("onError", 0.0).append("onNull", 0.0)),
                                new Document("$convert", new Document("input", "$location.coordinates.latitude").append("to", "double").append("onError", 0.0).append("onNull", 0.0))
                        )))
                )),
                // Second $project stage to create fullName
                Aggregates.project(Projections.fields(
                        Projections.include("gender", "email", "location", "birthdate", "age"),
                        Projections.computed("fullName", new Document("$concat", Arrays.asList(
                                new Document("$toUpper", new Document("$substrCP", Arrays.asList("$name.first", 0, 1))),
                                new Document("$substrCP", Arrays.asList("$name.first", 1, new Document("$subtract", Arrays.asList(new Document("$strLenCP", "$name.first"), 1)))),
                                " ",
                                new Document("$toUpper", new Document("$substrCP", Arrays.asList("$name.last", 0, 1))),
                                new Document("$substrCP", Arrays.asList("$name.last", 1, new Document("$subtract", Arrays.asList(new Document("$strLenCP", "$name.last"), 1))))
                        )))
                )),
                // Output to a new collection "transformedPersons"
                Aggregates.out("transformedPersons")
        );

        // Execute the aggregation
        collection.aggregate(pipeline).first(); // Execute without printing since we are outputting to a new collection

        System.out.println("Transformation complete, results stored in 'transformedPersons'.");
    }

    private static void fetchTenRecordsSortByBirthData(MongoCollection<Document> collection) {
        // Define the aggregation pipeline
        var pipeline = Arrays.asList(
                Aggregates.match(Filters.eq("gender", "male")),
                Aggregates.project(Projections.fields(
                        Projections.excludeId(),
                        Projections.include("gender"),
                        Projections.computed("name",
                                new Document("$concat", Arrays.asList(
                                        "$name.first", " ", "$name.last"))),
                        Projections.computed("birthdate",
                                new Document("$toDate", "$dob.date"))
                )),
                Aggregates.sort(Sorts.ascending("birthdate")),
                Aggregates.skip(10),
                Aggregates.limit(10)
        );

        // Execute the aggregation
        collection.aggregate(pipeline)
                .forEach(doc -> System.out.println(doc.toJson()));
    }

    private static void averagePersonsAgePerBucket(MongoCollection<Document> collection) {
        // Define the aggregation pipeline for $bucket
        List<Document> bucketPipeline = Arrays.asList(
                new Document("$bucket", new Document("groupBy", "$dob.age")
                        .append("boundaries", Arrays.asList(18, 30, 40, 50, 60, 120))
                        .append("output", new Document("numPersons", new Document("$sum", 1))
                                .append("averageAge", new Document("$avg", "$dob.age"))))
        );

        // Execute the $bucket aggregation and print each document
        System.out.println("Results for $bucket:");
        collection.aggregate(bucketPipeline).forEach(doc -> System.out.println(doc.toJson()));

        // Define the aggregation pipeline for $bucketAuto
        List<Document> bucketAutoPipeline = Arrays.asList(
                new Document("$bucketAuto", new Document("groupBy", "$dob.age")
                        .append("buckets", 5)
                        .append("output", new Document("numPersons", new Document("$sum", 1))
                                .append("averageAge", new Document("$avg", "$dob.age"))))
        );

        // Execute the $bucketAuto aggregation and print each document
        System.out.println("Results for $bucketAuto:");
        collection.aggregate(bucketAutoPipeline).forEach(doc -> System.out.println(doc.toJson()));
    }

    private static void aggregateByBirthYearAndSortByDESC(MongoCollection<Document> collection) {
        List<Document> pipeline = Arrays.asList(
                // Stage 1: Project relevant fields and convert dob.date to Date
                new Document("$project", new Document("_id", 0)
                        .append("name", 1)
                        .append("email", 1)
                        .append("birthdate", new Document("$toDate", "$dob.date"))
                        .append("age", "$dob.age")
                        .append("location", new Document("type", "Point")
                                .append("coordinates", Arrays.asList(
                                        new Document("$convert",
                                                new Document("input", "$location.coordinates.longitude")
                                                        .append("to", "double")
                                                        .append("onError", 0.0)
                                                        .append("onNull", 0.0)),
                                        new Document("$convert",
                                                new Document("input", "$location.coordinates.latitude")
                                                        .append("to", "double")
                                                        .append("onError", 0.0)
                                                        .append("onNull", 0.0))
                                ))
                        )
                ),
                // Stage 2: Format full name and include selected fields
                new Document("$project", new Document("gender", 1)
                        .append("email", 1)
                        .append("location", 1)
                        .append("birthdate", 1)
                        .append("age", 1)
                        .append("fullName", new Document("$concat", Arrays.asList(
                                new Document("$toUpper", new Document("$substrCP", Arrays.asList("$name.first", 0, 1))),
                                new Document("$substrCP", Arrays.asList("$name.first", 1,
                                        new Document("$subtract", Arrays.asList(new Document("$strLenCP", "$name.first"), 1)))),
                                " ",
                                new Document("$toUpper", new Document("$substrCP", Arrays.asList("$name.last", 0, 1))),
                                new Document("$substrCP", Arrays.asList("$name.last", 1,
                                        new Document("$subtract", Arrays.asList(new Document("$strLenCP", "$name.last"), 1))))
                        )))
                ),
                // Stage 3: Group by birth year and count the number of persons
                new Document("$group", new Document("_id", new Document("birthYear", new Document("$isoWeekYear", "$birthdate")))
                        .append("numPersons", new Document("$sum", 1))
                ),
                // Stage 4: Sort by numPersons in descending order
                new Document("$sort", new Document("numPersons", -1))
        );

        collection.aggregate(pipeline).forEach(doc -> System.out.println(doc.toJson()));
    }

    private static void aggregateAndConvertDataTypesOfDOBAndPoints(MongoCollection<Document> collection) {
        // Define the aggregation pipeline
        List<Document> pipeline = Arrays.asList(
                // First project stage: convert dob.date and coordinates to specific types, include required fields
                new Document("$project", new Document("_id", 0)
                        .append("name", 1)
                        .append("email", 1)
                        .append("birthdate", new Document("$convert",
                                new Document("input", "$dob.date").append("to", "date")))
                        .append("age", "$dob.age")
                        .append("location", new Document("type", "Point")
                                .append("coordinates", Arrays.asList(
                                        new Document("$convert",
                                                new Document("input", "$location.coordinates.longitude")
                                                        .append("to", "double")
                                                        .append("onError", 0.0)
                                                        .append("onNull", 0.0)),
                                        new Document("$convert",
                                                new Document("input", "$location.coordinates.latitude")
                                                        .append("to", "double")
                                                        .append("onError", 0.0)
                                                        .append("onNull", 0.0))
                                ))
                        )
                ),
                // Second project stage: format fullName from name fields and include selected fields
                new Document("$project", new Document("gender", 1)
                        .append("email", 1)
                        .append("location", 1)
                        .append("birthdate", 1)
                        .append("age", 1)
                        .append("fullName", new Document("$concat", Arrays.asList(
                                new Document("$toUpper", new Document("$substrCP", Arrays.asList("$name.first", 0, 1))),
                                new Document("$substrCP", Arrays.asList("$name.first", 1,
                                        new Document("$subtract", Arrays.asList(new Document("$strLenCP", "$name.first"), 1)))),
                                " ",
                                new Document("$toUpper", new Document("$substrCP", Arrays.asList("$name.last", 0, 1))),
                                new Document("$substrCP", Arrays.asList("$name.last", 1,
                                        new Document("$subtract", Arrays.asList(new Document("$strLenCP", "$name.last"), 1))))
                        )))
                )
        );

        // Execute the pipeline and print results
        collection.aggregate(pipeline).forEach(doc -> System.out.println(doc.toJson()));
    }

    private static void aggregateAndTransformName(MongoCollection<Document> collection) {
        // Build the aggregation pipeline
        collection.aggregate(Arrays.asList(
                Aggregates.project(new Document("_id", 0)
                        .append("gender", 1)
                        .append("fullName", new Document("$concat", Arrays.asList(
                                new Document("$toUpper", new Document("$substrCP", Arrays.asList("$name.first", 0, 1))),
                                new Document("$substrCP", Arrays.asList("$name.first", 1,
                                        new Document("$subtract", Arrays.asList(new Document("$strLenCP", "$name.first"), 1)))),
                                " ",
                                new Document("$toUpper", new Document("$substrCP", Arrays.asList("$name.last", 0, 1))),
                                new Document("$substrCP", Arrays.asList("$name.last", 1,
                                        new Document("$subtract", Arrays.asList(new Document("$strLenCP", "$name.last"), 1))))
                        )))
                )
        )).forEach(doc -> System.out.println(doc.toJson()));  // Print each result in JSON format
    }

    private static void countNumberOfPersonsPerState(MongoCollection<Document> collection) {
        // Aggregation Pipeline
        collection.aggregate(Arrays.asList(
                Aggregates.match(Filters.eq("gender", "female")), // Match female persons
                Aggregates.group("$location.state", Accumulators.sum("totalPersons", 1)),
                Aggregates.sort(new Document("totalPersons", -1)) // Group by state and count
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
