#ifndef _GPMOTORS_H_
  #define _GPMOTORS_H_

  #ifdef ARDUINO
      #if ARDUINO < 100
          #include "WProgram.h"
      #else
          #include "Arduino.h"
      #endif
  #else
      #include "ArduinoWrapper.h"
  #endif
  
  /*ONLY FOR TEST PURPOSES*/
  /*It is recommended to use 1rpm*/
  #define MOTORS_SPEED 1
  
  #define POLAR_ANGLE_LIMIT 90
  #define DELAY_AFTER_MOVING_PHI 100
  #define DELAY_AFTER_MOVING_THETA 250
  #define THETACOORD 0
  #define PHICOORD 1
  #define CLKWISE 1
  #define CCLKWISE -1

  #include "goniophotometer.h"
  #include <Stepper.h>

  class GPMOTORS {
      public:
        GPMOTORS();
        boolean search_reference(int coordinate, int dir);
        void thetaInit(float dps, int res);
        void phiInit(float dps, int res);
        void moveMotors();
        int getThetaSteps();
        int getPhiSteps();
        boolean getEOM();
        
        
      private:
        /*Stepper pointers*/
        Stepper *stepper_theta;
        Stepper *stepper_phi;
        /*Resolution in Steps Per Sample*/
        int res_theta;
        int res_phi;
        /*Steps Per Revolution*/
        int spr_theta;
        int spr_phi;
        /*Degrees Per Step*/
        float dps_theta;
        float dps_phi;
        /*Step count*/
        int thetaSteps;
        int phiSteps;
        /*Turn directions*/
        int theta_dir;
        int phi_dir;
        /*End Of Measure*/
        boolean eom;
        /*Private functions*/
        void movePhi();
        void moveTheta();
        void stepSlow(Stepper *stepper, int steps,int msPerStep);
  };

#endif
