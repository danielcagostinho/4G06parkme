import time
import serial
import csv
import string
import datetime
from pymongo import MongoClient
from random import randint
from pprint import pprint

from bson.objectid import ObjectId
client = MongoClient('mongodb+srv://admin:0urtnknM5IpkGQcV@4g06parkme-4gwxq.mongodb.net/test?retryWrites=true')
db=client.ParkMe
rpi-serial-port = '/dev/ttyACM0';
serial-port-pc = 'COM3';
collection = db.get_collection('ParkingLots')
id1 = '5c49470d78dea5feb9d02a2c'
ser = serial.Serial(rpi-serial-port,9600,timeout=None)
file = open ("dataFile.txt", "w")
while True:
    
    timestamp = datetime.datetime.now()
    
    data = ser.readline()
    
    data = data.decode().replace("b'","")
    data = data.replace("\r\n'","")
    #file.write((timestamp.strftime("%Y%m%d%H%M%S") + ","+data))
    #data = timestamp.strftime("%Y%m%d%H%M%S") + ","+data;
    #file.flush()
    print ("RAW: " + data)
    event = data.split(",")
    print(event)
    event[1].replace("\r\n","")
    if (event[0] != ""):
        print(event, flush=True)
        #singleSpot = db.spots.find_one({'spot': int(event[0])})
        #print('Spot to be changed')
        #print(singleSpot)
        #if (int(event[0]) > int(singleSpot['lastUpdated'])):
        print( "exact string: " + event[1], flush=True)
        if ("1" in str(event[1])):
            spotState = True;
        else:
            spotState = False;
        print(spotState, flush=True)
        #result = collection.update_one({"_id":ObjectId(id1),"parking_spaces.id": ObjectId(event[0])}, {"$set": {"parking_spaces.$.occupancy": spotState}})
        result = collection.update_one({"_id":ObjectId(id1),"parking_spaces.id": ObjectId(event[0])}, {"$set": {"parking_spaces.$.occupancy": spotState}})

        print("spot status update: " + str(result.modified_count), flush=True)
        #result = db.spots.update_one({'spot': int(event[0])}, {'$set': {'lastUpdated' : int(event[1])}})
        #print("last updated update: " + str(result.modified_count))
        #print('Number of documents modified : ' + str(result.modified_count))

        #UpdatedDocument = db.spots.find_one({'spot': int(event[1])})
        #print('The updated document:')
        #pprint(UpdatedDocument)
    





