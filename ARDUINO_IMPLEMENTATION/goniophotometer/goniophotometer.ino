/*
   @file     goniophotometer.ino
   @author   Abdon Alejandro Vivas Imparato.

   Description:
   Arduino implementation of a control device of a goniophotometer compatible with
   KNDL architecture. This is the main file. It enters in different states depen-
   ding on the command received through serial communication and sends the corres-
   ponding responses. It uses the functions defined in gpMotors.h and gpSensors.h
   to move the motors and take measures.

   For more information about KNDL go to https://github.com/abdonvivas/KNDL
*/
#include "goniophotometer.h"
#include "gpSensor.h"
#include "gpMotors.h"

String command;
String state = S_IDLE;
/*The value of the theta coordinate itself*/
float theta = 0;
/*The value of the phi coordinate itself*/
float phi = 0;
/*Indicates the number of measures taken during the calibration process*/
int cal_measures = 0;
/*SENSOR-----------------------------------------------*/
GPSENSOR sensor;
static double meas_value;
/*MOTORS--------------------------------------------*/
GPMOTORS motors;
/*Degrees Per Step*/
float dps_theta;
float dps_phi;
/*Indicates if the first search for the reference is already done*/
boolean firstRefSearch = false;

void setup() {
  Wire.begin();
  Serial.begin(9600);
  pinMode(P_SLED, OUTPUT);
}

/*
   Main loop. Depending on the state, it takes measures, measures the offset
   of the goniophotometer, or waits.
*/
void loop() {
  if (state.equalsIgnoreCase(S_MEASURING)) {
    if (!firstRefSearch) {
      firstRefSearch = motors.search_reference(THETACOORD, CLKWISE);
      if (!firstRefSearch) {
        Serial.print("The reference angle could not be found for the motor corresponding to the polar angle." + SEPARATOR);
        Serial.print(R_END + SEPARATOR);
        state = S_IDLE;
      } else {
        firstRefSearch = motors.search_reference(PHICOORD, CLKWISE);
        if (!firstRefSearch) {
          Serial.print("The reference angle could not be found for the motor corresponding to the azimuth angle." + SEPARATOR);
          Serial.print(R_END + SEPARATOR);
          state = S_IDLE;
        } else {
          delay(1000);
        }
      }
    } else {
      take_measure();
    }
  } else if (state.equalsIgnoreCase(S_CALIBRATING)) {
    calibrate();
  } else if (state.equalsIgnoreCase(S_IDLE)) {
    /*Blink State LED*/
    digitalWrite(P_SLED, HIGH);
    delay(50);
    digitalWrite(P_SLED, LOW);
    delay(50);
  }
}

/*
   Measures the offset of the ggoniophotometer.
*/
void calibrate() {
  Serial.print("Calibrating...");
  Serial.print(SEPARATOR);

  /*Get illuminance value*/
  digitalWrite(P_SLED, HIGH);
  meas_value += sensor.getValue();
  digitalWrite(P_SLED, LOW);
  Serial.print(meas_value, 5);
  Serial.print(SEPARATOR);
  cal_measures++;

  /*Sends calibration result*/
  if (cal_measures == 5) {
    meas_value /= 5;
    Serial.print(R_DATA + SEPARATOR);
    Serial.print(meas_value, 5);
    Serial.print(SEPARATOR);
    Serial.print(R_END + SEPARATOR);
    cal_measures = 0;
    state = S_IDLE;
  }
}

/*
   Takes a measure, calculate the coordinates based on the number of steps
   taken by the motors, sends the results back to KNDL and move the motors
   to the next position.
*/
void take_measure() {
  /*Get voltage*/
  digitalWrite(P_SLED, HIGH);
  meas_value = sensor.getValue();
  digitalWrite(P_SLED, LOW);

  /*Compute coordinates*/
  theta = motors.getThetaSteps() * dps_theta;
  phi = motors.getPhiSteps() * dps_phi;

  /*Send data to serial port*/
  Serial.print(R_DATA + SEPARATOR);
  Serial.print(theta + SEPARATOR);
  Serial.print(phi + SEPARATOR);
  Serial.print(meas_value, 5);
  Serial.print(SEPARATOR);

  /*Move motors to next position*/
  motors.moveMotors();

  if (motors.getEOM()) {
    firstRefSearch = false;
    Serial.print(R_END + SEPARATOR);
    state = S_IDLE;
  }
}

/*
   SerialEvent occurs whenever a new data comes in the
   hardware serial RX.  This routine is run between each
   time loop() runs, so using delay inside loop can delay
   response.  Multiple bytes of data may be available.

   Changes the state of the device depending on the command
   received.
*/
void serialEvent() {
  command = Serial.readString();
  if  (command.equalsIgnoreCase(C_START)) {
    dps_theta = Serial.readString().toFloat();
    dps_phi = Serial.readString().toFloat();
    motors.thetaInit(dps_theta, Serial.readString().toInt());
    motors.phiInit(dps_phi, Serial.readString().toInt());
    Serial.print(R_ACK);
    Serial.print(SEPARATOR);
    delay(1000);
    sensor.sensorInit();
    if (sensor.sensorTest()) {
      state = S_MEASURING;
    } else {
      state = S_IDLE;
      Serial.print("The measure did not start because the communication with the sensing subsystem failed." + SEPARATOR);
    }
  } else if (command.equalsIgnoreCase(C_STOP)) {
    Serial.print(R_ACK);
    Serial.print(SEPARATOR);
    state = S_IDLE;
  } else if (command.equalsIgnoreCase(C_CALIBRATE)) {
    Serial.print(R_ACK);
    Serial.print(SEPARATOR);
    delay(1000);
    sensor.sensorInit();
    if (sensor.sensorTest()) {
      meas_value = 0;
      state = S_CALIBRATING;
    } else {
      state = S_IDLE;
      Serial.print("The calibration did not started because the communication with the sensing subsystem failed." + SEPARATOR);
    }
  } else {
    state = S_IDLE;
  }
}

