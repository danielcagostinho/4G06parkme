int photocellPin0 = 0;
int photocellPin1 = 1;
int photocellPin2 = 2;
int redPin2 = 11;
int greenPin2 = 10;
int redPin1 = 9;
int greenPin1 = 6;
int redPin0 = 5;
int greenPin0 = 3;
int ledCol0[] = {0,0};
int ledCol1[] = {0,0};
int ledCol2[] = {0,0};
int read0 = 0;
int read1 = 0;
int read2 = 0;
void setup()
{
  Serial.begin(9600);
  pinMode(redPin0, OUTPUT);
  pinMode(greenPin0, OUTPUT);
  pinMode(redPin1, OUTPUT);
  pinMode(greenPin1, OUTPUT);
  pinMode(redPin2, OUTPUT);
  pinMode(greenPin2, OUTPUT);
  
}
 
void loop()
{
  int reading0 = analogRead(photocellPin0);

  int reading1 = analogRead(photocellPin1);

  int reading2 = analogRead(photocellPin2);


  if (reading0 > 80 && read0 != 0) {
     read0 = 0;
    ledCol0[0] = 255; 
    ledCol0[1] = 0;// red
    Serial.println("0,0,1,1");
   
  } else if(reading0 < 80  && read0 != 1) {
    read0 = 1;
    ledCol0[0] = 0;
    ledCol0[1] = 255;// green
    Serial.println("0,0,1,0");
    
  }
  
  if (reading1 > 80 && read1 != 0) {
    read1 = 0;
    ledCol1[0] = 255; 
    ledCol1[1] = 0;// red
    Serial.println("1,0,2,1");
    
  } else if(reading1 < 80  && read1 != 1) {
    read1 = 1;
    ledCol1[0] = 0;
    ledCol1[1] = 255;// green
    Serial.println("1,0,2,0");
    
  }
  
  if (reading2 > 80 && read2 != 0) {
    read2 = 0;
    ledCol2[0] = 255;
    ledCol2[1] = 0;// green
    Serial.println("2,0,3,1");
    
  } else if(reading2 < 80  && read2 != 1) {
    read2 = 1;
    ledCol2[0] = 0;
    ledCol2[1] = 255;// green
    Serial.println("2,0,3,0");
    
  }

    setColor0(ledCol0[0],ledCol0[1]);
  setColor1(ledCol1[0],ledCol1[1]);
  setColor2(ledCol2[0],ledCol2[1]);
  
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
