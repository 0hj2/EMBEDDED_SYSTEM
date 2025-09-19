package Final;

import com.pi4j.wiringpi.Gpio;

public class DHT11_S {
   private static final int MAXTIMINGS = 85;
   private final int[] dht11_f = { 0, 0, 0, 0, 0 }; // DHT11 data format (5 bytes)
   private boolean isActive = true; // Flag to control whether the sensor is active

   public DHT11_S() {
      // Setup wiringPi
      if (Gpio.wiringPiSetup() == -1) {
         System.out.println(" ==>> GPIO SETUP FAILED");
         return;
      }
   }

   public float[] getData(final int pin) {
      // If the sensor is not active, return error values
      if (!isActive) {
         System.out.println("Sensor is inactive, no data fetched.");
         return new float[] { -99.0f, -99.0f };  // Return error values if inactive
      }

      int laststate = Gpio.HIGH; // signal 상태 변화를 알기 위해 기존 상태를 기억
      int j = 0; // 수신한 Bit의 index counter
      float h = -99; // 습도
      float c = -99; // 섭씨 온도

      // Integral RH, Decimal RH, Integral T, Decimal T
      dht11_f[0] = dht11_f[1] = dht11_f[2] = dht11_f[3] = dht11_f[4] = 0;

      // 1. DHT11 센서에게 start signal 전달
      Gpio.pinMode(pin, Gpio.OUTPUT);
      Gpio.digitalWrite(pin, Gpio.LOW);
      Gpio.delay(18);

      // 2. Pull-up -> 수신 모드로 전환 -> 센서의 응답 대기
      Gpio.digitalWrite(pin, Gpio.HIGH);
      Gpio.pinMode(pin, Gpio.INPUT);

      // 3. 센서의 응답에 따른 동작
      for (int i = 0; i < MAXTIMINGS; i++) {
         int counter = 0;
         while (Gpio.digitalRead(pin) == laststate) { // Gpio pin 상태가 바뀌지 않으면 대기
            counter++;
            Gpio.delayMicroseconds(1);
            if (counter == 255) {
               break;
            }
         }
         laststate = Gpio.digitalRead(pin);
         if (counter == 255) {
            break;
         }

         // 각각의 bit 데이터 저장
         if (i >= 4 && i % 2 == 0) { // 첫 3개의 상태 변화는 무시, laststate가 low에서 high로 바뀔 때만 값을 저장
            // data 저장
            dht11_f[j / 8] <<= 1; // 0 bit
            if (counter > 16) {
               dht11_f[j / 8] |= 1; // 1 bit
            }
            j++;
         }
      }

      // Checksum 확인
      if (j >= 40 && getChecksum()) {
         h = (float) ((dht11_f[0] << 8) + dht11_f[1]) / 10;
         if (h > 100) {
            h = dht11_f[0]; // for DHT11
         }
         c = (float) (((dht11_f[2] & 0x7F) << 8) + dht11_f[3]) / 10;
         if (c > 125) {
            c = dht11_f[2]; // for DHT11
         }
         if ((dht11_f[2] & 0x80) != 0) {
            c = -c;
         }
         System.out.println("Humidity = " + h + "% Temperature = " + c + "℃ ");
      }
      else {
         System.out.println("Checksum Error");
      }

      float[] result = { h, c };
      return result;
   }

   private boolean getChecksum() {
      return dht11_f[4] == (dht11_f[0] + dht11_f[1] + dht11_f[2] + dht11_f[3] & 0xFF);
   }

   public void stopSensor() {
      this.isActive = false;  // inactive
      System.out.println("Sensor stopped.");
   }

   public void startSensor() {
      this.isActive = true;  //active
   }
}
