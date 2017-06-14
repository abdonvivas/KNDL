#ifndef _GPSENSOR_H_
  #define _GPSENSOR_H_

  #ifdef ARDUINO
      #if ARDUINO < 100
          #include "WProgram.h"
      #else
          #include "Arduino.h"
      #endif
  #else
      #include "ArduinoWrapper.h"
  #endif
  
  #include "./MCP342X-master/MCP342X.h"
  #include "./TSL2561-Arduino-Library-master/TSL2561.h"

  /*
   * 0 -> MCP342X
   * 1 -> TSL2561
   */
  #define ILLUMINANCE_MEASURE 0

  /*
   * TSL2561_ADDR_LOW  0x29
   * TSL2561_ADDR_FLOAT 0x39
   * TSL2561_ADDR_HIGH 0x49
   */
  #define TSL_ADDR TSL2561_ADDR_FLOAT

  /* MCP3422 STEP SIZE */
//  const float stepSize = 0.000015625;  // 18-bit, 1X Gain
  const float stepSize = 0.0000625;  // 16-bit, 1X Gain
  static int16_t adc_result;
  
  class GPSENSOR {
      public:
        GPSENSOR();
        void sensorInit();
        bool sensorTest();
        double getValue();
      private:
        MCP342X *adc;
        TSL2561 *tsl;
        float meas_val;
  };

#endif /* _MCP342X_H_ */

