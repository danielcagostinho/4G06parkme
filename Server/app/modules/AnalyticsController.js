const ParkingLotsData = require("./ParkingLotsData")

// Gets all parking Space usage analytics
 var GetParkingSpaceUsage = (parkingSpace) => {
        var totalDuration =0
        var totalParks = 0
        
        var parking = false
        var startParkTime = true
        
        var lastParked = false  // the last time someone parking
        parkingSpace.logs.forEach(log => {
            // stop recording
            if (parking && !log.occupancy) {
                var totalTimeSpentParking = (log.time - startParkTime) / (1000*60*60)
                totalDuration += totalTimeSpentParking
                totalParks++
                parking = false
                startParkTime = 0
            } 

            // start recording
            if (log.occupancy) {
                parking = true
                startParkTime = log.time
                lastParked = log.time
            }
        })
        return {
            averageDuration: (totalDuration / totalParks),
            lastUsage: lastParked
        }
    }
var GetTotalParks = (parkingspace, map) => {
    var hourly = map.hourly
    var daily = map.daily

    const minimumHoursParked = 0.5;
    parkingspace.logs.forEach(log => {
        if (log.occupancy) {
            var date = new Date(parseInt(log.time))
            var hour = date.getHours()
            var day = date.getDay()

            lastParked = log.time

            // dont add if not in original map
            if (hourly[hour] != undefined)
                hourly[hour]++
            
            if (daily[day] != undefined) {
                daily[day]++
            }
            map.total++
        }
    })
} 

// TODO make parking lot define its own open hours
var BuildHourlyMap = () => {
    var map = {}
    for(var i = 0;i<24;i++)
        map[i] = 0
    return map
}

// TODO make parking lot define its own open days
var BuildDailyMap = () => {
    var map = {}
    for(var i = 0;i<7;i++)
        map[i] = 0
    return map
}

module.exports = {

    init() {
    },
    
    GetParkingLotAnalytics(parkinglotID) {
        var parkinglot = ParkingLotsData.GetParkingLot(parkinglotID)
        return this.GetParkingLotAnalyticsNoID(parkinglot)


    },

    GetParkingLotAnalyticsNoID(parkinglot) {
        var totalMap = {
            hourly: BuildHourlyMap(),
            daily: BuildDailyMap(),
            total: 0
        }
        parkinglot.parking_spaces.forEach(space => {
            GetTotalParks(space, totalMap)
        })
        return {
            analytics: totalMap
        }
    },

    GetParkingSpaceAnalytics(parkingLotID, parkingSpaceID) {
        var parkingSpace = ParkingLotsData.GetParkingSpace(parkingLotID, parkingSpaceID)
        var parkingSpaceUsage = GetParkingSpaceUsage(parkingSpace)
        
        return {
            analytics: parkingSpaceUsage
        }
    },

    
    
}