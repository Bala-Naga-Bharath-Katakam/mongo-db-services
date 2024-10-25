package org.database.services;
import org.bson.Document;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.mongodb.reactivestreams.client.MongoCollection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class MongoDBReactiveService {
    private static final String DATABASE_NAME = "school";
    private static final String COLLECTION_NAME = "students";

    public static void main(String[] args) {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

        // Perform CRUD operations
        createStudent(collection)
                .thenMany(readStudents(collection))
                .then(updateStudent(collection))
                .thenMany(readStudents(collection))
                .then(deleteStudent(collection))
                .thenMany(readStudents(collection))
                .doOnTerminate(mongoClient::close)
                .subscribe(
                        result -> System.out.println("Operation result: " + result),
                        error -> System.err.println("Error: " + error),
                        () -> System.out.println("CRUD operations complete.")
                );
    }

    // Reactive CREATE operation
    private static Mono<Void> createStudent(MongoCollection<Document> collection) {
        Document student = new Document("name", "John Doe")
                .append("age", 20)
                .append("subjects", Arrays.asList(
                        new Document("name", "Math").append("grade", "A"),
                        new Document("name", "Science").append("grade", "B")
                ));

        return Mono.from(collection.insertOne(student))
                .doOnSuccess(result -> System.out.println("Student inserted successfully: " + student.toJson()))
                .then();
    }

    // Reactive READ operation
    private static Flux<Document> readStudents(MongoCollection<Document> collection) {
        return Flux.from(collection.find())
                .doOnNext(student -> System.out.println("Found student: " + student.toJson()));
    }

    // Reactive UPDATE operation
    private static Mono<Void> updateStudent(MongoCollection<Document> collection) {
        Bson filter = Filters.eq("name", "John Doe");
        Bson update = Updates.set("subjects.0.grade", "A+");  // Update Math grade to A+
        return Mono.from(collection.updateOne(filter, update))
                .doOnSuccess(result -> System.out.println("Student updated successfully."))
                .then();
    }

    // Reactive DELETE operation
    private static Mono<Void> deleteStudent(MongoCollection<Document> collection) {
        Bson filter = Filters.eq("name", "John Doe");
        return Mono.from(collection.deleteOne(filter))
                .doOnSuccess(result -> System.out.println("Student deleted successfully."))
                .then();
    }
}
