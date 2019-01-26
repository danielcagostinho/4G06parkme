// necessary libraries
const express = require('express')
const app = express()

// necessary components
const DBRequestor = require("./app/modules/DBRequestor")
const AnalyticsController = require("./app/modules/AnalyticsController")
const Poller = require("./app/modules/Poller")

// port of webserver
const port = 8000
DBRequestor.Connect().then(() => { 
    
    // start poller
    Poller.init()
    AnalyticsController.init()
    
    initHandlers()

    app.listen(port, () => {
        console.log("We are live")
    });
}).catch((err) => {
    return console.log("Error: " + err)
} );


// init handler routes
let initHandlers = function () {
    require("./app/handlers/AnalyticsHandler")(app)
    require("./app/handlers/ParkingLotHandler")(app)
    require("./app/handlers/ParkingLotsHandler")(app)
}
