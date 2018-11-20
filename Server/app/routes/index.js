// const parkingData = require("../data/parkinglot.json")
module.exports = (app, db) => {
    app.get('/parking', (req, res) => {
        const collection = db.collection("spots");
		console.log(collection);
        collection.find({}).toArray((err,docs) => {
            if (err) {
                return console.log("Error in finding collection:" + err);
            } else {
                res.send({parkingSpaces:docs});
            }
        })

    })
}