const Promise=  require('es6-promise').Promise
const MongoClient = require('mongodb').MongoClient
const db = require("../data/dbconfig.json")
const ParkingLotsData = require("./ParkingLotsData")

var user;
module.exports = {

    // connect to mongo client and pulls once
    Connect() {
        return MongoClient.connect(db.url).then((db) => {
            user= db.db("ParkMe")
            this.Update();
        })
    },
    // pulls updates from database
    Update() {
        if (!user)
            return;
        const collection = user.collection("ParkingLots")
        collection.find({}).toArray((err,docs) => {
            if (err) {
                return console.log("Error in finding collection:" + err)
            } else {
                ParkingLotsData.Update(docs)
            }
        })
    }
}
