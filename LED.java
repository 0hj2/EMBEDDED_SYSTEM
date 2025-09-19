package Final;

import java.util.List;
import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.rest.BasicCoapResource;
import org.ws4d.coap.core.rest.CoapData;
import org.ws4d.coap.core.tools.Encoder;
import com.pi4j.io.gpio.*;

public class LED extends BasicCoapResource {

    private static final GpioController gpio = GpioFactory.getInstance();

    private static final GpioPinDigitalOutput r_led = provisionPin(gpio, RaspiPin.GPIO_08, PinState.LOW);
    private static final GpioPinDigitalOutput g_led = provisionPin(gpio, RaspiPin.GPIO_09, PinState.LOW);
    private static final GpioPinDigitalOutput b_led = provisionPin(gpio, RaspiPin.GPIO_07, PinState.LOW);

    private String state = "off";

    private boolean isBeingObserved;

    public LED() {
        super("/led", "off", CoapMediaType.text_plain);

        // Register a shutdown hook to clean up GPIO resources
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            r_led.low();
            g_led.low();
            b_led.low();
            gpio.unprovisionPin(r_led, g_led, b_led);
        }));
    }

    private static GpioPinDigitalOutput provisionPin(GpioController gpio, Pin pin, PinState defaultState) {
        GpioPin existingPin = gpio.getProvisionedPin(pin);
        if (existingPin == null) {
            return gpio.provisionDigitalOutputPin(pin, defaultState);
        }
        return (GpioPinDigitalOutput) existingPin;
    }

    @Override
    public synchronized CoapData get(List<CoapMediaType> mediaTypesAccepted) {
        return new CoapData(Encoder.StringToByte(this.state), CoapMediaType.text_plain);
    }

    public synchronized void updateLEDColor(float temperature, float humidity) {
        if (temperature == -99.0f && humidity == -99.0f) {
            this.state = "off";  // 체크섬 오류 발생 시 상태를 'off'로 설정
        }else if (temperature <= 0) {
            this.state = "white";  // 온도가 0도 이하일 경우 흰색
        } else if (temperature >= 30) {
            this.state = "red";  // 온도가 30도 이상일 경우 빨간색
        } else if (humidity >= 80) {
            this.state = "blue";  // 습도가 80 이상일 경우 파란색
        } else {
            this.state = "green";  // 그 외에는 녹색
        }

        applyLEDState();  // 결정된 색상 적용
        
    }

    private void applyLEDState() {
        switch (this.state) {
            case "red":
                r_led.high();
                g_led.low();
                b_led.low();
                break;
            case "blue":
                r_led.low();
                g_led.low();
                b_led.high();
                break;
            case "white":
                r_led.high();
                g_led.high();
                b_led.high();
                break;
            case "off":
                r_led.low();
                g_led.low();
                b_led.low();
                break;
            default:
                r_led.low();
                g_led.high();
                b_led.low();
                break;
        }
    }
    
    @Override
    public synchronized void delete() {
        r_led.low();
        g_led.low();
        b_led.low();

        gpio.unprovisionPin(r_led);
        gpio.unprovisionPin(g_led);
        gpio.unprovisionPin(b_led);

        this.state = "off";
        super.delete();
        this.isBeingObserved = false;  
    }



    @Override
    public synchronized String getResourceType() {
        return "Raspberry Pi 4 LED";
    }
}
