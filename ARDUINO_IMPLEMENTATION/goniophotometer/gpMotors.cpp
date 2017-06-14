/*
   @file     gpMotors.cpp
   @author   Abdon Alejandro Vivas Imparato.

   Description:
   Handle the movement of the motors.

   For more information about KNDL go to https://github.com/abdonvivas/KNDL
*/
#include "gpMotors.h"

GPMOTORS::GPMOTORS() {
  pinMode(P_OPTOTHETA, INPUT);
  pinMode(P_OPTOPHI, INPUT);
  eom = false;
}

/*
   Makes the necessary initializations in order to start moving the motor
   corresponding to the polar angle.
*/
void GPMOTORS::thetaInit(float dps, int resolution) {
  res_theta = resolution;
  dps_theta = dps;
  spr_theta = (int) (360 / dps);
  stepper_theta = new Stepper(spr_theta, P1_THETA, P2_THETA, P3_THETA, P4_THETA);
  stepper_theta -> setSpeed(MOTORS_SPEED);
  thetaSteps = 0;
  theta_dir = CLKWISE;//THETA_FIRST_DIRECTION
}

/*
   Makes the necessary initializations in order to start moving the motor
   corresponding to the azimuth angle.
*/
void GPMOTORS::phiInit(float dps, int resolution) {
  res_phi = resolution;
  dps_phi = dps;
  spr_phi = (int) (360 / dps);
  stepper_phi = new Stepper(spr_phi, P1_PHI, P2_PHI, P3_PHI, P4_PHI);
  stepper_phi -> setSpeed(MOTORS_SPEED);
  phiSteps = 0;
  phi_dir = CLKWISE;//PHI_FIRST_DIRECTION
}

/*
   In goniophotometer.ino, this function is used to move the motors. If this file
   is to be changed to support other types of motor drivers or non-bipolar
   motors or non-stepper motors, goniophotometer.ino can still be used
   if this function is maintained to move the motors to the next position.
*/
void GPMOTORS::moveMotors() {
  movePhi();
}

/*
   Moves the motor corresponding to the azimuth angle. If its time to
   move the motor corresponding to the polar angle, it calls its res-
   pective function.
*/
void GPMOTORS::movePhi() {
  phiSteps += (phi_dir) * res_phi;
  if ( phiSteps == (int) spr_phi ) {
    phi_dir *= -1;
    phiSteps += (phi_dir) * res_phi;
    moveTheta();
  } else if (phiSteps < 0) {
    phi_dir *= -1;
    phiSteps += (phi_dir) * res_phi;
    moveTheta();
  } else {
    if (thetaSteps == 0 || thetaSteps == (int) (180 / dps_theta)) {
      moveTheta();
      //Undo the step counted.
      phiSteps -= (phi_dir) * res_phi;
    } else {
      stepper_phi -> step( phi_dir * res_phi );
      delay(DELAY_AFTER_MOVING_PHI);
    }
  }
}
/*
   Moves the motor corresponding to the polar angle.
*/
void GPMOTORS::moveTheta() {
  thetaSteps += theta_dir * res_theta;
  if ( thetaSteps > (int) (POLAR_ANGLE_LIMIT / dps_theta) ) {
    if ( phi_dir == CCLKWISE ) {
      stepper_phi -> step( phi_dir * spr_phi);
    }
    theta_dir *= -1;
    stepper_theta -> step(theta_dir * (int) (POLAR_ANGLE_LIMIT / dps_theta));
    theta_dir *= -1;
    eom = true;
  } else if ( thetaSteps == (int) (POLAR_ANGLE_LIMIT / dps_theta) ) {
    if ( phi_dir == CCLKWISE ) {
      stepper_phi -> step( phi_dir * (spr_phi - res_phi));
      phiSteps += phi_dir * (spr_phi - res_phi);
      phi_dir *= -1;
      stepSlow(stepper_theta, (theta_dir * res_theta), DELAY_AFTER_MOVING_THETA);
    }
  } else {
    stepSlow(stepper_theta, (theta_dir * res_theta), DELAY_AFTER_MOVING_THETA);
  }
}

/*
   Searches for the reference angle.
   Returns true if it found it and false it does not.
*/
boolean GPMOTORS::search_reference(int coordinate, int dir) {
  boolean ref_found = true;
  int n = 0;
  int n_max = spr_theta / 2;
  int optocoupler = P_OPTOTHETA;
  Stepper *stepper = stepper_theta;
  if (coordinate == PHICOORD) {
    optocoupler = P_OPTOPHI;
    stepper = stepper_phi;
  }

  while (!digitalRead(optocoupler) && n < n_max) {
    stepSlow(stepper, dir, DELAY_AFTER_MOVING_THETA);
    n ++;
  }
  if (!digitalRead(optocoupler)) {
    delay(1000);
    n = 0;
    n_max = spr_theta;
    while (!digitalRead(optocoupler) && n < n_max) {
      stepSlow(stepper, -dir, DELAY_AFTER_MOVING_THETA);
      n++;
    }
    if (!digitalRead(optocoupler)) {
      ref_found = false;
    }
  }
  return ref_found;
}

/*
   Steps at a rate less than 1rpm.
*/
void GPMOTORS::stepSlow(Stepper *stepper, int steps, int msPerStep) {
  int i;
  int dir = 1;
  if (steps < 0) {
    dir = -1;
    steps *= -1;
  }
  for (i = 0; i < steps; i++) {
    stepper->step(dir);
    delay(msPerStep);
  }
}

/*
   Returns the number of steps taken by the motor corresponding to
   the polar angle since the reference angle.
*/
int GPMOTORS::getThetaSteps() {
  return thetaSteps;
}

/*
   Returns the number of steps taken by the motor corresponding to
   the azimuth angle since the reference angle.
*/
int GPMOTORS::getPhiSteps() {
  return phiSteps;
}

/*
   Returns true if the measure ended and false otherwise.
*/
boolean GPMOTORS::getEOM() {
  return eom;
}

