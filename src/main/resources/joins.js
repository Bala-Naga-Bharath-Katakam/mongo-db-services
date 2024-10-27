// Default left join
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


// Inner join
db.authors.aggregate([
    {
        $lookup: {
            from: "books",
            localField: "books",
            foreignField: "_id",
            as: "bookDetails"
        }
    },
    {
        $match: { bookDetails: { $ne: [] } }
    }
]).pretty();
