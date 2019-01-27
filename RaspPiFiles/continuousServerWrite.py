import time
import serial
import csv
import string
import datetime
from pymongo import MongoClient
from random import randint
from pprint import pprint
client = MongoClient('mongodb+srv://admin:0urtnknM5IpkGQcV@4g06parkme-4gwxq.mongodb.net/test?retryWrites=true')
db=client.spots

ser = serial.Serial('COM3',9600,timeout=None)
file = open ("dataFile.txt", "w")
while True:
    
    timestamp = datetime.datetime.now()
    
    data = ser.readline()
    
    data = data.decode().replace("b'","")
    data = data.replace("\r\n'","")
    #file.write((timestamp.strftime("%Y%m%d%H%M%S") + ","+data))
    data = timestamp.strftime("%Y%m%d%H%M%S") + ","+data;
    #file.flush()
    print ("RAW: " + data)
    event = data.split(",")
    event[4].replace("\r\n","")
    if (event[0] != ""):
        print(event)
        singleSpot = db.spots.find_one({'spot': int(event[1])})
        print('Spot to be changed')
        print(singleSpot)
        #if (int(event[0]) > int(singleSpot['lastUpdated'])):
        result = db.spots.update_one({'spot': int(event[1])}, {'$set': {'spotStatus' : int(event[4])}})
        print("spot status update: " + str(result.modified_count))
        result = db.spots.update_one({'spot': int(event[1])}, {'$set': {'lastUpdated' : int(event[0])}})
        print("last updated update: " + str(result.modified_count))
        print('Number of documents modified : ' + str(result.modified_count))

        UpdatedDocument = db.spots.find_one({'spot': int(event[1])})
        print('The updated document:')
        pprint(UpdatedDocument)
    


