db.authors.aggregate([
    {
        $lookup: {
            from: "books",
            localField: "books",
            foreignField: "_id",
            as: "bookDetails"
        }
    }
]).pretty();
