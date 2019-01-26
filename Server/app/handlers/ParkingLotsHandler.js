let ParkingLotData = require("../modules/ParkingLotsData")

module.exports = (app) => {
    app.get('/api/parkinglots/all', (req, res) => {
        res.send({parking_lots: ParkingLotData.GetAllParkingLotsID()});
    })

    app.get('/api/parkinglots/radius', (req,res) => {
        res.send({parking_lot: ParkingLotData.GetAllParkingLotsWithinRadius(JSON.parse(Buffer.from(req.query.center,"base64")), req.query.radius)})
    })

    app.get('/api/parkinglots/:id', (req,res) => {
        res.send({parking_lot: ParkingLotData.GetParkingLot(req.params.id)})
    })


}