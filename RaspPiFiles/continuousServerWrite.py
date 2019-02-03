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
ser = serial.Serial('COM3',9600,timeout=None)
file = open ("dataFile.txt", "w")
spotState = False;
while True:
    eventTime = datetime.datetime.now().strftime("%Y%m%d%H%M%S")

    data = ser.readline()

    data = data.decode().replace("b'","")
    data = data.replace("\r\n'","")
    event = data.split(",")
    event[1].replace("\r\n","")
    if (event[0] != ""):
        print( "exact string: " + event[1], flush=True)
        if ("1" in str(event[1])):
            spotState = True;
        else:
            spotState = False;
        print(spotState, flush=True)
        #result = collection.update_one({"_id":ObjectId(id1),"parking_spaces.id": ObjectId(event[0])}, {"$set": {"parking_spaces.$.occupancy": spotState}})
        result = collection.update_one({"_id":ObjectId(id1),"parking_spaces.id": ObjectId(event[0])}, {"$set": {"parking_spaces.$.occupancy": spotState}}, {"$set": {"parking_spaces.$.logs.occupancy": spotState}}, {"$push": {"parking_spaces.$.logs.time": eventTime}})
        print("spot status update: " + str(result.modified_count), flush=True)
