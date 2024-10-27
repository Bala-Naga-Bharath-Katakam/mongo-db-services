// Insert an author document
db.authors.insertOne({
    _id: ObjectId("64dbb57b8f1e0d9b3b45c4c1"),
    name: "J.K. Rowling",
    address: {
        street: "123 Fictional St",
        city: "London",
        country: "UK"
    },
    books: [
        ObjectId("64dbb57b8f1e0d9b3b45c4d1"),
        ObjectId("64dbb57b8f1e0d9b3b45c4d2")
    ]
});

// Insert book documents
db.books.insertMany([
    {
        _id: ObjectId("64dbb57b8f1e0d9b3b45c4d1"),
        title: "Harry Potter and the Sorcerer's Stone",
        genre: "Fantasy",
        publisher: {
            name: "Bloomsbury",
            address: {
                street: "50 Bedford Square",
                city: "London",
                country: "UK"
            }
        },
        publishedYear: 1997
    },
    {
        _id: ObjectId("64dbb57b8f1e0d9b3b45c4d2"),
        title: "Harry Potter and the Chamber of Secrets",
        genre: "Fantasy",
        publisher: {
            name: "Bloomsbury",
            address: {
                street: "50 Bedford Square",
                city: "London",
                country: "UK"
            }
        },
        publishedYear: 1998
    }
]);
