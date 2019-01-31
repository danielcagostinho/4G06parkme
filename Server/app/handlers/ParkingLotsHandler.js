let ParkingLotData = require("../modules/ParkingLotsData")

module.exports = (app) => {
    app.get('/api/parkinglots/all', (req, res) => {
        res.send({parking_lots: ParkingLotData.GetAllParkingLotsID()});
    })

    app.get('/api/parkinglots/radius', (req,res) => {
        res.send({parking_lots: ParkingLotData.GetAllParkingLotsWithinRadius(req.query.latitude, req.query.longitude, req.query.radius)})
    })

    app.get('/api/parkinglots/:id', (req,res) => {
        res.send({parking_lot: ParkingLotData.GetParkingLot(req.params.id)})
    })


}