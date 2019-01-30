#include <Ultrasonic.h>



// Declaration of Ultrasonic Pins
const unsigned int TxPin1 = 12; // Sensor 1 Trigger
const unsigned int RxPin1 = 13; // Sensor 1 Echo
const unsigned int TxPin1 = 3; // Sensor 2 Trigger
const unsigned int RxPin1 = 4; // Sensor 2 Echo
const unsigned int TxPin1 = 5; // Sensor 3 Trigger
const unsigned int RxPin1 = 6; // Sensor 3 Echo
const unsigned int TxPin1 = 7; // Sensor 4 Trigger
const unsigned int RxPin1 = 8; // Sensor 4 Echo

// Sensor IDs
const char sensorID1[24] = "5c4946c9ac8f790000f001cf";
const char sensorID2[24] = "aaaaaaaaaaaaaaaaaaaaaaaa";
const char sensorID3[24] = "bbbbbbbbbbbbbbbbbbbbbbbb";
const char sensorID4[24] = "cccccccccccccccccccccccc";

// Other Pins
int redPin1 = 5;
int greenPin1 = 3;
int ledCol1[] = {0,0};

// State Variables
bool read1 = false;
bool read2 = false;
bool read3 = false;
bool read4 = false;

// Threshold Distance
int threshold = 120; // Will sense at a range of 120cm

Ultrasonic sensor1(TxPin1, RxPin1);
void setup()
{
  Serial.begin(9600);
  pinMode(redPin0, OUTPUT);
  pinMode(greenPin0, OUTPUT);
  pinMode(TxPin1, OUTPUT);
  pinMode(RxPin1, INPUT);
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
    char serialString1[30] = sensorID1 + ",True"; // print id plus occupancy to serial port for RPi
    Serial.println(serialString1);
    ledCol1[0] = 255; 
    ledCol1[1] = 0; // Make LED red for occupied
  } else if(distance1 > threshold && read1 == true) { // if it is not within range and the previous state was occupied
    read1 = false;
    char serialString1[30] = sensorID1 + ",False"; // print id plus occupancy to serial port for RPi
    ledCol1[0] = 0;
    ledCol1[1] = 255;// Make LED green for vacant
    Serial.println(serialString1);
  }
  //Check second sensor
  if (distance2 <= threshold && read2 == false) { // if it is within range and the previous state was unoccupied
    read2 = true; // set state to occupied 
    char serialString2[30] = sensorID2 + ",True"; // print id plus occupancy to serial port for RPi
    Serial.println(serialString2);
    ledCol2[0] = 255; 
    ledCol2[1] = 0; // Make LED red for occupied
  } else if(distance2 > threshold && read2 == true) { // if it is not within range and the previous state was occupied
    read2 = false;
    char serialString2[30] = sensorID2 + ",False"; // print id plus occupancy to serial port for RPi
    ledCol2[0] = 0;
    ledCol2[1] = 255;// Make LED green for vacant
    Serial.println(serialString2);
  }
  //Check first sensor
  if (distance3 <= threshold && read3 == false) { // if it is within range and the previous state was unoccupied
    read3 = true; // set state to occupied 
    char serialString3[30] = sensorID3 + ",True"; // print id plus occupancy to serial port for RPi
    Serial.println(serialString3);
    ledCol3[0] = 255; 
    ledCol3[1] = 0; // Make LED red for occupied
  } else if(distance3 > threshold && read3 == true) { // if it is not within range and the previous state was occupied
    read3 = false;
    char serialString3[30] = sensorID3 + ",False"; // print id plus occupancy to serial port for RPi
    ledCol3[0] = 0;
    ledCol3[1] = 255;// Make LED green for vacant
    Serial.println(serialString3);
  }
  //Check first sensor
  if (distance4 <= threshold && read4 == false) { // if it is within range and the previous state was unoccupied
    read4 = true; // set state to occupied 
    char serialString4[30] = sensorID4 + ",True"; // print id plus occupancy to serial port for RPi
    Serial.println(serialString4);
    ledCol4[0] = 255; 
    ledCol4[1] = 0; // Make LED red for occupied
  } else if(distance4 > threshold && read4 == true) { // if it is not within range and the previous state was occupied
    read4 = false;
    char serialString4[30] = sensorID4 + ",False"; // print id plus occupancy to serial port for RPi
    ledCol4[0] = 0;
    ledCol4[1] = 255;// Make LED green for vacant
    Serial.println(serialString4);
  }
    setColor1(ledCol1[0],ledCol1[1]);
    setColor2(ledCol2[0],ledCol2[1]);
    setColor3(ledCol3[0],ledCol3[1]);
    setColor4(ledCol4[0],ledCol4[1]);
    delay(500);
}

void setColor1(int red, int green)
{
  #ifdef COMMON_ANODE
    red = 255 - red;
    green = 255 - green;
  #endif
  digitalWrite(redPin1, red);
  digitalWrite(greenPin1, green);
}
void setColor2(int red, int green)
{
  #ifdef COMMON_ANODE
    red = 255 - red;
    green = 255 - green;
  #endif
  digitalWrite(redPin2, red);
  digitalWrite(greenPin2, green);
}
void setColor3(int red, int green)
{
  #ifdef COMMON_ANODE
    red = 255 - red;
    green = 255 - green;
  #endif
  digitalWrite(redPin3, red);
  digitalWrite(greenPin3, green);
}
void setColor4(int red, int green)
{
  #ifdef COMMON_ANODE
    red = 255 - red;
    green = 255 - green;
  #endif
  digitalWrite(redPin4, red);
  digitalWrite(greenPin4, green);
}
