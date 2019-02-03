var parkingLots= require("../data/parkinglots")
var AnalyticsController = require("../modules/AnalyticsController")

var currentHash = ""

// Converts from degrees to radians.
Math.radians = function(degrees) {
  return degrees * Math.PI / 180;
};

// returns of the distance between user and parking lot is within a radius
var radiusCheck = function(lat, long, parkinglot, radius) {


    var lat2 = parkinglot.location.lat
    var long2 = parkinglot.location.long

    var R = 6371e3; // metres
    var φ1 = Math.radians(lat)
    var φ2 = Math.radians(lat2)
    var Δφ = Math.radians(lat2-lat)
    var Δλ = Math.radians(long2-long)

    // math stuff
    var a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
            Math.cos(φ1) * Math.cos(φ2) *
            Math.sin(Δλ/2) * Math.sin(Δλ/2)
    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))

    var d =  R * c/1000
    return d <= radius
}

// returns the closest parking space
var getBestParkingSpace = function(parkinglot, preference, accessible) {

    var currentBestSpot = {}
    var currentClosestDistance = Infinity
    var entrance = parkinglot.entrance
    for(space of parkinglot.parking_spaces) {
        
        // ignore occupant spots
        if (space.occupancy)
            continue

        // if they are not looking for accessible parking spaces skip
        if (space.accessible && !accessible)
            continue 
        // use pythagorean lmao
        var diffX = Math.abs(entrance.x - space.position.x)
        var diffY = Math.abs(entrance.y - space.position.y)
        var distance = Math.sqrt(Math.pow(diffX,2) + Math.pow(diffY,2))
        // set new best
        if (distance < currentClosestDistance) {
            currentBestSpot = space
            currentClosestDistance = distance
        }
    }
    return currentBestSpot.id
} 
module.exports=  {
    
    Update(data) {

        // compare hashes so that we dont compute changes for nothing
        var dataHash = JSON.stringify(data)
        if (dataHash !== currentHash) {
            parkingLots = data;
            currentHash = dataHash
            console.log("Updating Parking Lots Data!")
        }
    },

    // returns all parking lot ids
    GetAllParkingLotsID() {
        return parkingLots.map(parkinglot => parkinglot._id)
    },

    // Gets all parking lots within radius (in km)
    GetAllParkingLotsWithinRadius(lat, long, radius) {
        return parkingLots.filter(parkinglot => {
            return radiusCheck(lat, long, parkinglot, radius)
        }).map(lot => {
            var clone = Object.assign({}, lot)
            var total = lot.parking_spaces.length
            var available = lot.parking_spaces.reduce((prev, curr) => {
                if (!curr.occupancy) {
                    prev++
                }
                return prev
            }, 0)
            clone["parking_spaces"] = {
                total,
                available
            }
            var analytics = AnalyticsController.GetParkingLotAnalyticsNoID(lot)
            clone = Object.assign(clone, analytics)
            return clone
        })
    },
    

    // returns a parking lot with the specified id
    GetParkingLot(id) {
        return parkingLots.find(parkinglot => {
            return parkinglot._id == id
        })
    },

    // returns all parking spaces in a parking lot
    GetAllParkingSpaces(parkinglotid) {
        return this.GetParkingLot(parkinglotid).parking_spaces.map(space => space.id)
    },

    // returns all parking spaces in a parking lot
    GetParkingSpace(parkinglotid, parkingspaceid) {
        return this.GetParkingLot(parkinglotid).parking_spaces.find(space => { return space.id == parkingspaceid})
    },

    // returns the best parking space based on user settings
    GetBestParkingSpace(parkinglotid, preference, accessible) {
        return getBestParkingSpace(this.GetParkingLot(parkinglotid), preference, accessible)
    }
}