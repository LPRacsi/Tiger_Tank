#include <Servo.h>
#include "Arduino.h"


Servo gunServo;
Servo towerServo;



/* Pin configuration */
const int rightChainPin = 3; //PWM pin for right chain
const int rightChainDirectionPin = 5; //PWM pin for right chain
const int rightChainDirectionPin2 = 6;  //PWM pin for left chain
const int leftChainPin = 10;  //PWM pin for left chain
const int leftChainDirectionPin = 8; //Pin for enable motor driver
const int leftChainDirectionPin2 = 7;  //Digital out pin 2 for left chain direction
const int gunServoPin = 4; //Servo uotput pin for gun up and down
const int towerServoPin = 2;  //Servo output pin for tower right/left turn
const int startupPin = A0;
const int idlePin = A1;
const int highspeedPin = A2;
const int motorstopPin = A3;

const int gunAngleMin = 1;
const int gunAngleMax = 20;
int gunAngle = gunAngleMin;
/********************Commands**********************************/
const char commandLeftChain = 'L';      // UART-command for left chain
const char commandRightChain = 'R';      // UART-command for right chain
const char commandTower = 'T';      // UART-command for tower
const char commandGun = 'G';      // UART-command for gun
const char aliveMSG = 'A';      // UART-command for alive check
const char tuneTower = 'C';     // UART-command for controlling tower turn default value
const char commandGoForward = 'F';     // UART-command for go forward/backward
const char commandTurnToSide = 'S';     // UART-command for turn right/left

const int autoOFF = 1500;
/***********************Sound states****************/
const int startup = 1;
const int idle = 2;
const int highspeed = 3;
const int motorstop = 4;

int sound;

int towerDefaultAngle = 111;

bool serialWasAvailable;

char incomingByte;    // incoming data
char L_Data[4];       // array data for left motor
byte L_index = 0;     // index of array L
char R_Data[4];       // array data for right motor
byte R_index = 0;     // index of array R
char T_Data[3];       // array data for tower
byte T_index = 0;     // index of array tower
char G_Data[3];       // array data for gun
byte G_index = 0;     // index of array gun
char C_Data[3];       // array data for tower rurn default value
byte C_index = 0;     // index of array tower rurn default value
char F_Data[3];       // array data for foward/backward
byte F_index = 0;     // index of array foward/backward
char S_Data[3];       // array data for turn right/left
byte S_index = 0;     // index of array turn right/left
char command;         // command
char lastCommand;
unsigned long lastTimeCommand;
int looper;
/*******************Function definitions***************************************/
void ControlChains(int chainLeft, int chainRight){

  bool directionL, directionR;      // direction of motor rotation L298N
  int chainLeft2, chainRight2;
  //digitalWrite(motorControlEnable,HIGH);
  if ((abs(chainRight) > 0 || abs(chainLeft) > 0) && sound != highspeed){
    HandleSoundPins(highspeed);
  }else if ((chainRight == 0 && chainLeft == 0) && sound != idle && sound != motorstop){
    HandleSoundPins(idle);
  }
  
  if(chainLeft > 0){
    //chainLeft = 0;  
    //chainLeft2 = abs(chainLeft);  
    directionL = LOW;
  }
  else if(chainLeft < 0){
    chainLeft = abs(chainLeft);
    //chainLeft2 = 0;
    directionL = HIGH;
  }
  else {
    chainLeft = 0;
  }
 
  if(chainRight > 0){
    //chainRight = 0;
    //chainRight2 = abs(chainRight);
    directionR = LOW;
  }
  else if(chainRight < 0){
    chainRight = abs(chainRight);
    //chainRight2 = 0;
    directionR = HIGH;
  }
  else {
    chainRight = 0;
    //chainRight2 = 0;
  }
   
  analogWrite(rightChainPin, chainRight);            // set speed for left motor
  analogWrite(leftChainPin, chainLeft);            // set speed for right motor
  digitalWrite(rightChainDirectionPin, directionR);       // set direction of left motor rotation
  digitalWrite(rightChainDirectionPin2, !directionR);       // set direction2 of left motor rotation
  digitalWrite(leftChainDirectionPin, directionL);       // set direction of right motor rotation
  digitalWrite(leftChainDirectionPin2, !directionL);       // set direction2 of right motor rotation    
}
/**************************************************************/
void ControlChainsArrow(char moveCommand, int moveDirection){

  int directionL, directionR;      // direction of motor rotation L298N
  int chainLeft,chainRight;
  chainLeft = 255;
  chainRight = 255;
  //digitalWrite(motorControlEnable,HIGH);
  if (moveCommand == 'F'){// Go forward/backward
    if(moveDirection > 0){
      directionL = LOW;
      directionR = LOW;
      Serial.println("Elore");
      /*analogWrite(rightChainPin, directionR);            // set speed for left motor
      analogWrite(rightChainPin2, !directionR);            // set speed for right motor
      analogWrite(leftChainPin, directionL);       // set direction of left motor rotation
      analogWrite(leftChainPin2, !directionL);       // set direction2 of left motor rotation
      digitalWrite(leftChainDirectionPin, directionL);       // set direction of right motor rotation
      digitalWrite(leftChainDirectionPin2, !directionL);*/       // set direction2 of right motor rotation   
    }
    else if(moveDirection < 0){
      directionL = HIGH;
      directionR = HIGH;
      /*Serial.println("Hatra");
      Serial.print("directionR: ");
      Serial.println(directionR);
      Serial.print("directionL: ");
      Serial.println(directionL);*/    
      /*analogWrite(rightChainPin, (chainRight));            // set speed for left motor
      analogWrite(leftChainPin, (chainLeft));            // set speed for right motor
      digitalWrite(rightChainDirectionPin, directionR);       // set direction of left motor rotation
      digitalWrite(rightChainDirectionPin2, !directionR);       // set direction2 of left motor rotation
      digitalWrite(leftChainDirectionPin, directionL);       // set direction of right motor rotation
      digitalWrite(leftChainDirectionPin2, !directionL);*/       // set direction2 of right motor rotation        
    }
    else {
      chainLeft = 0;
      chainRight = 0;
      /*analogWrite(rightChainPin, (chainRight));            // set speed for left motor
      analogWrite(leftChainPin, (chainLeft));            // set speed for right motor
      digitalWrite(rightChainDirectionPin, 0);       // set direction of left motor rotation
      digitalWrite(rightChainDirectionPin2, 0);       // set direction2 of left motor rotation
      digitalWrite(leftChainDirectionPin, 0);       // set direction of right motor rotation
      digitalWrite(leftChainDirectionPin2, 0);*/       // set direction2 of right motor rotation        
    }
  }
  if (moveCommand == 'S'){// Turn right/left
    if(moveDirection > 0){
      directionL = HIGH;
      directionR = LOW;
      /*analogWrite(rightChainPin, (chainRight));            // set speed for left motor
      analogWrite(leftChainPin, (chainLeft));            // set speed for right motor
      digitalWrite(rightChainDirectionPin, directionR);       // set direction of left motor rotation
      digitalWrite(rightChainDirectionPin2, !directionR);       // set direction2 of left motor rotation
      digitalWrite(leftChainDirectionPin, directionL);       // set direction of right motor rotation
      digitalWrite(leftChainDirectionPin2, !directionL);*/       // set direction2 of right motor rotation   
    }
    else if(moveDirection < 0){
      directionL = LOW;
      directionR = HIGH;
      /*analogWrite(rightChainPin, (chainRight));            // set speed for left motor
      analogWrite(leftChainPin, (chainLeft));            // set speed for right motor
      digitalWrite(rightChainDirectionPin, directionR);       // set direction of left motor rotation
      digitalWrite(rightChainDirectionPin2, !directionR);       // set direction2 of left motor rotation
      digitalWrite(leftChainDirectionPin, directionL);       // set direction of right motor rotation
      digitalWrite(leftChainDirectionPin2, !directionL);*/       // set direction2 of right motor rotation        
    }
    else {
      chainLeft = 0;
      chainRight = 0;
      /*analogWrite(rightChainPin, (chainRight));            // set speed for left motor
      analogWrite(leftChainPin, (chainLeft));            // set speed for right motor
      digitalWrite(rightChainDirectionPin, 0);       // set direction of left motor rotation
      digitalWrite(rightChainDirectionPin2, 0);       // set direction2 of left motor rotation
      digitalWrite(leftChainDirectionPin, 0);       // set direction of right motor rotation
      digitalWrite(leftChainDirectionPin2, 0);*/       // set direction2 of right motor rotation        
    }
  }
  
  if (chainLeft == 0 && chainRight == 0){
    analogWrite(rightChainPin, LOW);            // set speed for left motor
    analogWrite(leftChainPin, LOW);       // set direction of left motor rotation
    digitalWrite(rightChainDirectionPin, directionR);       // set direction of left motor rotation
    digitalWrite(rightChainDirectionPin2, !directionR);       // set direction2 of left motor rotation
    digitalWrite(leftChainDirectionPin, directionL);       // set direction of right motor rotation
    digitalWrite(leftChainDirectionPin2, !directionL);       // set direction2 of right motor rotation      
    Serial.print("nulla");
  }else{
    Serial.print("directionR: ");
    Serial.println(directionR);
    Serial.print("directionR2: ");
    Serial.println(abs(255 - directionR));    
    Serial.print("directionL: ");
    Serial.println(directionL);   
    analogWrite(rightChainPin, chainRight);            // set speed for left motor
    analogWrite(leftChainPin, chainLeft);       // set direction of left motor rotation
    digitalWrite(rightChainDirectionPin, directionR);       // set direction of left motor rotation
    digitalWrite(rightChainDirectionPin2, !directionR);       // set direction2 of left motor rotation
    digitalWrite(leftChainDirectionPin, directionL);       // set direction of right motor rotation
    digitalWrite(leftChainDirectionPin2, !directionL);       // set direction2 of right motor rotation      
  }

  
  if ((chainRight > 0 || chainLeft > 0) && sound != highspeed){
    HandleSoundPins(highspeed);
  }else if ((chainRight == 0 && chainLeft == 0) && sound != idle && sound != motorstop){
    HandleSoundPins(idle);
  }    
}
/**************************************************************/
void ControlTower(int tower){
  if (tower > 0){
    towerServo.write(towerDefaultAngle - 2);
  }else if(tower < 0){
    towerServo.write(towerDefaultAngle + 2);
  } else if (tower == 0){ // Button released
     towerServo.write(towerDefaultAngle);
  }
}
/**************************************************************/
void ControlGun(int gun){  
  if (gun > 0){
    gunAngle = gunAngle + 1;
    if (gunAngle > gunAngleMax){
        gunAngle = gunAngleMax;
    }
  }
  if (gun < 0){
    gunAngle = gunAngle - 1;
    if (gunAngle < gunAngleMin){
        gunAngle = gunAngleMin;
    }          
  }
  gunServo.write(gunAngle);
}
/**************************************************************/
void ModifyTowerDefault(int towerDefault){
  if (towerDefault > 0){
    towerDefaultAngle = towerDefaultAngle + 1;
  }
  if (towerDefault < 0){
    towerDefaultAngle = towerDefaultAngle - 1;          
  }
  towerServo.write(towerDefaultAngle);
}
/**************************************************************/
void HandleSoundPins(int innerSound){
  sound = innerSound;
  switch(innerSound){
    case startup: 
      digitalWrite(startupPin,HIGH);
      digitalWrite(idlePin,LOW);
      digitalWrite(highspeedPin,LOW);
      digitalWrite(motorstopPin,LOW);
      break;
    case idle: 
      digitalWrite(startupPin,LOW);
      digitalWrite(idlePin,HIGH);
      digitalWrite(highspeedPin,LOW);
      digitalWrite(motorstopPin,LOW);
      break;
    case highspeed: 
      digitalWrite(startupPin,LOW);
      digitalWrite(idlePin,LOW);
      digitalWrite(highspeedPin,HIGH);
      digitalWrite(motorstopPin,LOW);
      break;
    case motorstop: 
      digitalWrite(startupPin,LOW);
      digitalWrite(idlePin,LOW);
      digitalWrite(highspeedPin,LOW);
      digitalWrite(motorstopPin,HIGH);
      break;
   default: 
      digitalWrite(startupPin,LOW);
      digitalWrite(idlePin,LOW);
      digitalWrite(highspeedPin,LOW);
      digitalWrite(motorstopPin,LOW);
      break;                            
  }
}

/*************************************************Setup*****************************************************/
void setup()                    // run once, when the sketch starts
{
  Serial.begin(9600);
  looper = 0;
  pinMode(rightChainPin, OUTPUT);
  pinMode(leftChainPin, OUTPUT);
  pinMode(leftChainDirectionPin, OUTPUT);
  pinMode(leftChainDirectionPin2, OUTPUT);
  pinMode(rightChainDirectionPin, OUTPUT);
  pinMode(rightChainDirectionPin2, OUTPUT);
  pinMode(startupPin, OUTPUT);
  pinMode(idlePin, OUTPUT);
  pinMode(highspeedPin, OUTPUT);
  pinMode(motorstopPin, OUTPUT);
  pinMode(A4, OUTPUT);
  pinMode(A5, OUTPUT);
  pinMode(A6, OUTPUT);
  pinMode(A7, OUTPUT);
  pinMode(11, OUTPUT);
  pinMode(12, OUTPUT);
  analogWrite(rightChainPin, LOW);            // Initial speed for left motor
  analogWrite(leftChainPin, LOW);            // Initial speed for right motor
  HandleSoundPins(startup);
  delay(200);
  gunServo.attach(gunServoPin);
  gunServo.write(gunAngleMin);
  towerServo.attach(towerServoPin);
  towerServo.write(towerDefaultAngle);
  serialWasAvailable = false;
  HandleSoundPins(100); //Should be the default
}
/****************************************Loop**********************************************/
void loop()
{
  if (Serial.available() > 0) {          // if received UART data
    serialWasAvailable = true;
    if (sound == motorstop){HandleSoundPins(startup);}
    incomingByte = Serial.read();        // raed byte
    Serial.write(incomingByte);
    if(incomingByte == commandLeftChain) {           // if received data for left motor L
      command = commandLeftChain;                    // current command
      memset(L_Data,0,sizeof(L_Data));   // clear aray
      L_index = 0;                       // resetting array index
    }
    else if(incomingByte == commandRightChain) {      // if received data for left motor R
      command = commandRightChain;
      memset(R_Data,0,sizeof(R_Data));
      R_index = 0;
    }
    else if(incomingByte == commandTower) {      // if received data for towet turn
      command = commandTower;
      memset(T_Data,0,sizeof(T_Data));
      T_index = 0;
    }
    else if(incomingByte == commandGun) {      // if received data for gun move
      command = commandGun;
      memset(G_Data,0,sizeof(G_Data));
      G_index = 0;
    }
    else if(incomingByte == aliveMSG) {
      lastTimeCommand = millis();
      lastCommand = aliveMSG;
    }
    else if(incomingByte == tuneTower){
      command = tuneTower;
      memset(C_Data,0,sizeof(C_Data));
      C_index = 0;      
     }
     else if(incomingByte == commandGoForward){
      command = commandGoForward;
      memset(F_Data,0,sizeof(F_Data));
      F_index = 0;      
     }
     else if(incomingByte == commandTurnToSide){
      command = commandTurnToSide;
      memset(S_Data,0,sizeof(S_Data));
      S_index = 0;      
     }   
    else if(incomingByte == '\r') command = 'e';   // end of line
 /**************************Fill the buffers with values***********************************************/   
    if(command == commandLeftChain && incomingByte != commandLeftChain){
      L_Data[L_index] = incomingByte;              // store each byte in the array
      L_index++;                                   // increment array index
      lastCommand = commandLeftChain;
    }
    else if(command == commandRightChain && incomingByte != commandRightChain){
      R_Data[R_index] = incomingByte;
      R_index++;
      lastCommand = commandRightChain;
    }     
    if(command == commandTower && incomingByte != commandTower){
      T_Data[T_index] = incomingByte;              // store each byte in the array
      T_index++;                                   // increment array index
      lastCommand = commandTower;
    }
    else if(command == commandGun && incomingByte != commandGun){      
      G_Data[G_index] = incomingByte;
      G_index++;
      lastCommand = commandGun;
    }
    else if(command == tuneTower && incomingByte != tuneTower){
      C_Data[C_index] = incomingByte;
      C_index++;
      lastCommand = tuneTower;
    }
    else if(command == commandGoForward && incomingByte != commandGoForward){
      F_Data[F_index] = incomingByte;
      F_index++;
      lastCommand = commandGoForward;
    }
    else if(command == commandTurnToSide && incomingByte != commandTurnToSide){
      S_Data[S_index] = incomingByte;
      S_index++;
      lastCommand = commandTurnToSide;
    }    
/****************************Execute commands*********************************************/
    else if(command == 'e'){                       // if we take the line end
      if (lastCommand == commandRightChain || lastCommand == commandLeftChain){
        ControlChains(atoi(L_Data),atoi(R_Data));      
        delay(10);
      }
      if (lastCommand == commandTower){        
        ControlTower(atoi(T_Data));
      }
      if (lastCommand == commandGun){
        ControlGun(atoi(G_Data));
      }
      if (lastCommand == tuneTower){
        ModifyTowerDefault(atoi(C_Data));
      }
      if (lastCommand == commandGoForward){
        ControlChainsArrow(commandGoForward,atoi(F_Data));
      }
      if (lastCommand == commandTurnToSide){
        ControlChainsArrow(commandTurnToSide,atoi(S_Data));
      }
    }  
  }
/*********************Connection lost*******************************************/      
  if ((millis() >= lastTimeCommand + autoOFF) && serialWasAvailable){ //Connection lost
    HandleSoundPins(motorstop);
    ControlChains(0,0);
    //digitalWrite(motorControlEnable,LOW);
    gunServo.write(gunAngleMin);
    gunAngle = gunAngleMin;
    towerServo.write(towerDefaultAngle);    
  }
}
