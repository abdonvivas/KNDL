#ifndef _GONIOPHOTOMETER_H_
  #define _GONIOPHOTOMETER_H_

  #ifdef ARDUINO
      #if ARDUINO < 100
          #include "WProgram.h"
      #else
          #include "Arduino.h"
      #endif
  #else
      #include "ArduinoWrapper.h"
  #endif

  /*PIN MAPPING*/
  /*State LED*/
  #define P_SLED 2
  /*Optocouplers' pins*/
  #define P_OPTOTHETA 8
  #define P_OPTOPHI 3
  /*Motor drivers' pins*/
  #define P1_THETA 12
  #define P2_THETA 11
  #define P3_THETA 10
  #define P4_THETA 9
  #define P1_PHI 7
  #define P2_PHI 6
  #define P3_PHI 5
  #define P4_PHI 4
  
  /*States*/
  #define S_MEASURING "MEAS"
  #define S_IDLE "IDL"
  #define S_CALIBRATING "CAL"
  /*Commands*/
  #define C_START "STR"
  #define C_STOP "STP"
  #define C_CALIBRATE "CAL"
  /*Responses*/
  #define R_DATA "DAT"
  #define R_END "END"
  #define R_ACK "ACK"
  
  /*Serial communication separator*/
  const String SEPARATOR = ";";

  const long timeOut = 15000;

#endif

