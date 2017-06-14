/*
   @file     gpSensor.cppp
   @author   Abdon Alejandro Vivas Imparato.

   Description:
   Handle the control of the sensing subsystem. It can use two different types
   of sensing systems: MCP342X + (any lux meter with analog output) or TSL2561.
   The selection of the subsystem to be used is done in gpSensor.h.
   
   If this you want to change your sensing subsystem, change this file and and gpSensor.h
   keeping the names and interfaces of the fnctions and reuse the rest of the arduino code.

   For more information about KNDL go to https://github.com/abdonvivas/KNDL
*/
#include "GPSENSOR.h"

GPSENSOR::GPSENSOR() {
}

/*
   Makes the necessary initializations to start taking measures.
*/
void GPSENSOR::sensorInit() {
  if (ILLUMINANCE_MEASURE) {
    tsl = new TSL2561(TSL_ADDR);
    tsl->setGain(TSL2561_GAIN_0X);
    tsl->setTiming(TSL2561_INTEGRATIONTIME_402MS);
  } else {
    adc = new MCP342X();
    adc->configure( MCP342X_MODE_ONESHOT |
                    MCP342X_CHANNEL_1 |
                    MCP342X_SIZE_16BIT |
                    MCP342X_GAIN_1X
                  );
  }
}

/*
   Tests the communication with the sensor.
*/
bool GPSENSOR::sensorTest() {
  bool testResult;
  if (ILLUMINANCE_MEASURE) {
    testResult = tsl->begin();
  } else {
    testResult = adc->testConnection();
  }
  return testResult;
}

/*
   Takes a measure and returns its value.
*/
double GPSENSOR::getValue() {
  double result;
  if (ILLUMINANCE_MEASURE) {
    result = (double) tsl->calculateLux(tsl->getLuminosity(TSL2561_FULLSPECTRUM), tsl->getLuminosity(TSL2561_INFRARED));
  } else {
    adc->startConversion();
    adc->getResult(&adc_result);
    result = adc_result * stepSize;
  }
  return result;
}

