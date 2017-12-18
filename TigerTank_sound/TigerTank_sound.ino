#include "Arduino.h"
#include "SoftwareSerial.h"
#include "DFRobotDFPlayerMini.h"

SoftwareSerial mySoftwareSerial(4, 3); // RX, TX

DFRobotDFPlayerMini myDFPlayer;

/***********************Sound states****************/
const int startup = 1;
const int idle = 2;
const int highspeed = 3;
const int motorstop = 4;

const int startupPin = A0;
const int idlePin = A1;
const int highspeedPin = A2;
const int motorstopPin = A3;

int sound;

void HaldleSound(int soundIn){
  if (soundIn != sound){
    switch(soundIn){
      case startup:
        myDFPlayer.play(0001);  //Play the first mp3
        sound = startup;
        Serial.println("Startup spund!");
        break;
      case idle:
        myDFPlayer.loop(0002);
        sound = idle;
        Serial.println("Idle sound");
        break;
      case highspeed:  
        myDFPlayer.loop(0003);
        sound = highspeed;
        Serial.println("Accelerate sound");
        break;        
      case motorstop:
        myDFPlayer.play(0004);
        sound = motorstop;
        Serial.println("Stopp sound ");
        break;      
    }
  }
}
void printDetail(uint8_t type, int value){
 switch (type) {
  case TimeOut:
    Serial.println(F("Time Out!"));
    break;
  case WrongStack:
    Serial.println(F("Stack Wrong!"));
    break;
  case DFPlayerCardInserted:
    Serial.println(F("Card Inserted!"));
    break;
  case DFPlayerCardRemoved:
    Serial.println(F("Card Removed!"));
    break;
  case DFPlayerCardOnline:
    Serial.println(F("Card Online!"));
    break;
  case DFPlayerPlayFinished:
    Serial.print(F("Number:"));
    Serial.print(value);
    Serial.println(F(" Play Finished!"));
    Serial.println(F("DFPlayerPlayFinished value:"));
    Serial.println(DFPlayerPlayFinished);
    break;
  case DFPlayerError:
    Serial.print(F("DFPlayerError:"));
    switch (value) {
      case Busy:
        Serial.println(F("Card not found"));
        break;
      case Sleeping:
        Serial.println(F("Sleeping"));
        break;
      case SerialWrongStack:
        Serial.println(F("Get Wrong Stack"));
        break;
      case CheckSumNotMatch:
        Serial.println(F("Check Sum Not Match"));
        break;
      case FileIndexOut:
        Serial.println(F("File Index Out of Bound"));
        break;
      case FileMismatch:
        Serial.println(F("Cannot Find File"));
        break;
      case Advertise:
        Serial.println(F("In Advertise"));
        break;
      default:
        break;
    }
    break;
  default:
    break;
  }
}

void HandleSoundInputPins(){
  bool startupRead, idleRead, highSpeedRead, motorStopRead;
  startupRead = digitalRead(startupPin);
  idleRead = digitalRead(idlePin);
  highSpeedRead = digitalRead(highspeedPin);
  motorStopRead = digitalRead(motorstopPin);
  
  /*Serial.print("startupRead: ");
  Serial.println(startupRead);
  Serial.print("idleRead: ");
  Serial.println(idleRead);
  Serial.print("highSpeedRead: ");
  Serial.println(highSpeedRead);
  Serial.print("motorStopRead: ");
  Serial.println(motorStopRead);*/
  
  if (startupRead){
    HaldleSound(startup);
  }
  if (idleRead){
    HaldleSound(idle);
  }
  if (highSpeedRead){
    HaldleSound(highspeed);
  }
  if (motorStopRead){
    HaldleSound(motorstop);
  }
}
void setup() {
  mySoftwareSerial.begin(9600);
  Serial.begin(9600);
  if (!myDFPlayer.begin(mySoftwareSerial)) {  //Use softwareSerial to communicate with mp3.
    Serial.println(F("Unable to begin:"));
    Serial.println(F("1.Please recheck the connection!"));
    Serial.println(F("2.Please insert the SD card!"));
  }
  Serial.println(F("DFPlayer Mini online."));
  myDFPlayer.volume(30);  //Set volume value. From 0 to 30
  HaldleSound(startup);
  // put your setup code here, to run once:
  pinMode(startupPin, INPUT);
  pinMode(idlePin, INPUT);
  pinMode(highspeedPin, INPUT);
  pinMode(motorstopPin, INPUT);
  pinMode(A4, INPUT_PULLUP);
  pinMode(A5, INPUT_PULLUP);
  pinMode(A6, INPUT_PULLUP);
  pinMode(A7, INPUT_PULLUP);
  pinMode(11, INPUT_PULLUP);
  pinMode(12, INPUT_PULLUP);
}

void loop() {
  if (myDFPlayer.available()) {
    Serial.println("-----------------------------------------------------------------");
    printDetail(myDFPlayer.readType(), myDFPlayer.read()); //Print the detail message from DFPlayer to handle different errors and states.
  }
  if ((sound == startup) && ((uint8_t)myDFPlayer.readType()== DFPlayerPlayFinished)){
      Serial.println(F("Idle sound paly"));
      HaldleSound(idle);
  }
  HandleSoundInputPins();
}
