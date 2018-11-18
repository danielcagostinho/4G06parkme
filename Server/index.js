const express = require('express');
const MongoClient = require('mongodb').MongoClient;
const db = require("./app/data/dbconfig.json");
const poller = require("./app/modules/dbpoller");
const app = express();

const port = 8000;

MongoClient.connect(db.url, (err, db) =>{
    if (err) {
        return console.log("Error: " + err);
    } else {
        db = db.db("user");
        
        require('./app/routes')(app, db);
        app.listen(port, () => {
            console.log("We are live")
        });
    }
});
