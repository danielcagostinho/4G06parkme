#include <Ultrasonic.h>
const unsigned int TxPin1 = 12;
const unsigned int RxPin1 = 13;

Ultrasonic midSonar(TxPin1, RxPin1);
int redPin0 = 5;
int greenPin0 = 3;
int ledCol0[] = {0,0};
bool read0 = false;
int read1 = 0;
int read2 = 0;
int threshold = 25;
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
  int distance = midSonar.read(CM);
  //int reading0 = analogRead(photocellPin0);
  //Serial.print("Distance to car: ");
  //Serial.println(distance);


  if (distance <= threshold && read0 == false) {
     read0 = true;
    ledCol0[0] = 255; 
    ledCol0[1] = 0;// red
    Serial.println("5c4946c9ac8f790000f001cf,True");
    //Serial.println("Sensed Car!");
  } else if(distance > threshold && read0 == true) {
    read0 = false;
    ledCol0[0] = 0;
    ledCol0[1] = 255;// green
    Serial.println("5c4946c9ac8f790000f001cf,False");
    
  }
  
  

    setColor0(ledCol0[0],ledCol0[1]);
    delay(500);
}

void setColor0(int red, int green)
{
  #ifdef COMMON_ANODE
    red = 255 - red;
    green = 255 - green;
  #endif
 
  
  digitalWrite(redPin0, red);
  digitalWrite(greenPin0, green);
}
