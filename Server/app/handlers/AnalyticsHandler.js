let AnalyticsController = require("../modules/AnalyticsController")

module.exports = (app) => {

    // gets analytics for a entire parking lot
    app.get('/api/parkinglot/:id/analytics', (req, res) => {
        res.send(AnalyticsController.GetParkingLotAnalytics(req.params.id))
    })

    // gets analytics for a entire parking space
    app.get('/api/parkinglot/:parkinglotid/parkingspace/:parkingspaceid/analytics', (req, res) => {
        res.send(AnalyticsController.GetParkingSpaceAnalytics(req.params.parkinglotid, req.params.parkingspaceid));
    })
}