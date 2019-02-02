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

collection = db.get_collection('ParkingLots')
id1 = '5c49470d78dea5feb9d02a2c'
ser = serial.Serial('COM6',9600,timeout=None)
file = open ("dataFile.txt", "w")
spotState = False;
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
        print(event)
        #singleSpot = db.spots.find_one({'spot': int(event[0])})
        #print('Spot to be changed')
        #print(singleSpot)
        #if (int(event[0]) > int(singleSpot['lastUpdated'])):

        if (event[1] == "1"):
            spotState = True;
        else:
            spotState = False;
        result = collection.update_one({"_id":ObjectId(id1),"parking_spaces.id": ObjectId(event[0])}, {"$set": {"parking_spaces.$.occupancy": spotState}})
        print("spot status update: " + str(result.modified_count))
        #result = db.spots.update_one({'spot': int(event[0])}, {'$set': {'lastUpdated' : int(event[1])}})
        #print("last updated update: " + str(result.modified_count))
        #print('Number of documents modified : ' + str(result.modified_count))

        #UpdatedDocument = db.spots.find_one({'spot': int(event[1])})
        #print('The updated document:')
        #pprint(UpdatedDocument)
