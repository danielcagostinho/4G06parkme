#include <Ultrasonic.h>



// Declaration of Ultrasonic Pins
const unsigned int TxPin1 = 11; // Sensor 1 Trigger
const unsigned int RxPin1 = 10; // Sensor 1 Echo
const unsigned int TxPin2= 13; // Sensor 2 Trigger
const unsigned int RxPin2 = 12; // Sensor 2 Echo
const unsigned int TxPin3 = 7; // Sensor 3 Trigger
const unsigned int RxPin3 = 6; // Sensor 3 Echo
const unsigned int TxPin4 = 9; // Sensor 4 Trigger
const unsigned int RxPin4 = 8; // Sensor 4 Echo

// Sensor IDs
const char sensorID1[24] = "5c4946c9ac8f790000f001cf";
const char sensorID2[24] = "5c4be566bc01840000b851d3";
const char sensorID3[24] = "5c534fd3a2b1de00000ed7a3";
const char sensorID4[24] = "5c53504ea2b1de00000ed7a4";

// Other Pins
//int redPin1 = 5;
//int greenPin1 = 3;
//int ledCol1[] = {0,0};

// State Variables
bool read1 = false;
bool read2 = false;
bool read3 = false;
bool read4 = false;

// Threshold Distance
int threshold = 5; // Will sense at a range of 120cm

Ultrasonic sensor1(TxPin1, RxPin1);
Ultrasonic sensor2(TxPin2, RxPin2);
Ultrasonic sensor3(TxPin3, RxPin3);
Ultrasonic sensor4(TxPin4, RxPin4);
void setup()
{
  Serial.begin(9600);
//  pinMode(redPin0, OUTPUT);
//  pinMode(greenPin0, OUTPUT);
  pinMode(TxPin1, OUTPUT);
  pinMode(RxPin1, INPUT);
  pinMode(TxPin2, OUTPUT);
  pinMode(RxPin2, INPUT);
  pinMode(TxPin3, OUTPUT);
  pinMode(RxPin3, INPUT);
  pinMode(TxPin4, OUTPUT);
  pinMode(RxPin4, INPUT);
}
 
void loop()
{
  int distance1 = sensor1.read(CM);
  int distance2 = sensor2.read(CM);
  int distance3 = sensor3.read(CM);
  int distance4 = sensor4.read(CM);

  //Check first sensor
  if (distance1 <= threshold && read1 == false) { // if it is within range and the previous state was unoccupied
    read1 = true; // set state to occupied 
    Serial.println("5c4946c9ac8f790000f001cf, 1");
  } else if(distance1 > threshold && read1 == true) { // if it is not within range and the previous state was occupied
    read1 = false;
    Serial.println("5c4946c9ac8f790000f001cf, 0");
  }
  //Check second sensor
  if (distance2 <= threshold && read2 == false) { // if it is within range and the previous state was unoccupied
    read2 = true; // set state to occupied 
    Serial.println("5c4be566bc01840000b851d3, 1");
  } else if(distance2 > threshold && read2 == true) { // if it is not within range and the previous state was occupied
    read2 = false;
    Serial.println("5c4be566bc01840000b851d3, 0");
    }
  //Check first sensor
  if (distance3 <= threshold && read3 == false) { // if it is within range and the previous state was unoccupied
    read3 = true; // set state to occupied 
    Serial.println("5c534fd3a2b1de00000ed7a3, 1");
  } else if(distance3 > threshold && read3 == true) { // if it is not within range and the previous state was occupied
    read3 = false;
    Serial.println("5c534fd3a2b1de00000ed7a3, 0");
  }
  //Check first sensor
  if (distance4 <= threshold && read4 == false) { // if it is within range and the previous state was unoccupied
    read4 = true; // set state to occupied 
    Serial.println("5c53504ea2b1de00000ed7a4, 1");
  } else if(distance4 > threshold && read4 == true) { // if it is not within range and the previous state was occupied
    read4 = false;
    Serial.println("5c53504ea2b1de00000ed7a4, 0");
  }
    delay(500);
}
