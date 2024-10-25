package org.database.services;
import org.bson.Document;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.mongodb.reactivestreams.client.MongoCollection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;

public class MongoDBReactiveService {

    private static final String DATABASE_NAME = "school";
    private static final String COLLECTION_NAME = "students";

    public static void main(String[] args) {
        // Step 1: Connect to MongoDB
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

        // Step 2: Perform CRUD operations
        createStudent(collection)
                .thenMany(readStudents(collection))
                .thenMany(updateStudent(collection))
                .thenMany(readStudents(collection))
                .thenMany(deleteStudent(collection))
                .thenMany(readStudents(collection))
                .doOnTerminate(mongoClient::close)
                .subscribe(result -> System.out.println("Operation result: " + result),
                        error -> System.err.println("Error: " + error),
                        () -> System.out.println("CRUD operations complete."));
    }

    // CREATE operation
    private static Mono<Void> createStudent(MongoCollection<Document> collection) {
        Document student = new Document("name", "John Doe")
                .append("age", 20)
                .append("subjects", Arrays.asList(
                        new Document("name", "Math").append("grade", "A"),
                        new Document("name", "Science").append("grade", "B")
                ));

        return Mono.from(collection.insertOne(student))
                .doOnSuccess(result -> System.out.println("Inserted student: " + student.toJson()))
                .doOnError(error -> System.err.println("Insertion error: " + error))
                .then();
    }


    // READ operation
    private static Flux<Document> readStudents(MongoCollection<Document> collection) {
        return Flux.from(collection.find())
                .doOnNext(student -> System.out.println("Read student: " + student.toJson()));
    }

    // UPDATE operation
    private static Mono<Void> updateStudent(MongoCollection<Document> collection) {
        Document update = new Document("$set", new Document("subjects.0.grade", "A+"));
        return Mono.from(collection.updateOne(new Document("name", "John Doe"), update))
                .doOnNext(result -> System.out.println("Updated count: " + result.getModifiedCount()))
                .then();
    }

    // DELETE operation
    private static Mono<Void> deleteStudent(MongoCollection<Document> collection) {
        return Mono.from(collection.deleteOne(new Document("name", "John Doe")))
                .doOnNext(result -> System.out.println("Deleted count: " + result.getDeletedCount()))
                .then();
    }
}

