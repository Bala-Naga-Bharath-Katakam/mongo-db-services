package org.database.services;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Arrays;

public class MongoDBCRUDService {
    private static final String DATABASE_NAME = "school";
    private static final String COLLECTION_NAME = "students";

    public static void main(String[] args) {
        MongoClient mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .applyToClusterSettings(builder ->
                                builder.hosts(Arrays.asList(new ServerAddress("localhost", 27017))))
                        .build());

        MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

        // Create a document with embedded documents and insert it
        createStudent(collection);

        // Read and display documents
        readStudents(collection);

        // Update a document
        updateStudent(collection);

        // Delete a document
        deleteStudent(collection);

        // Close the MongoDB connection
        mongoClient.close();
    }



    // CREATE operation
    private static void createStudent(MongoCollection<Document> collection) {
        Document student = new Document("name", "John Doe")
                .append("age", 20)
                .append("subjects", Arrays.asList(
                        new Document("name", "Math").append("grade", "A"),
                        new Document("name", "Science").append("grade", "B")
                ));

        collection.insertOne(student);
        System.out.println("Student inserted successfully.");
    }

    // READ operation
    private static void readStudents(MongoCollection<Document> collection) {
        for (Document student : collection.find()) {
            System.out.println("Student: " + student.toJson());
        }
    }

    // UPDATE operation
    private static void updateStudent(MongoCollection<Document> collection) {
        Bson filter = Filters.eq("name", "John Doe");
        Bson update = Updates.set("subjects.0.grade", "A+");  // Update Math grade to A+
        collection.updateOne(filter, update);
        System.out.println("Student updated successfully.");
    }

    // DELETE operation
    private static void deleteStudent(MongoCollection<Document> collection) {
        Bson filter = Filters.eq("name", "John Doe");
        collection.deleteOne(filter);
        System.out.println("Student deleted successfully.");
    }
}

