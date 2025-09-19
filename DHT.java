package Final;

import java.util.List;
import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.rest.BasicCoapResource;
import org.ws4d.coap.core.rest.CoapData;
import org.ws4d.coap.core.tools.Encoder;


public class DHT extends BasicCoapResource {

    private String value = "0";
    DHT11_S dht = new DHT11_S();
    LED led = new LED();  
    private boolean isBeingObserved = false;  
    private boolean isSensorActive = true;  

    private DHT(String path, String value, CoapMediaType mediaType) {
        super(path, value, mediaType);
    }

    public DHT() {
        this("/temp", "0", CoapMediaType.text_plain);
    }

    @Override
    public synchronized CoapData get(List<String> query, List<CoapMediaType> mediaTypesAccepted) {
        return get(mediaTypesAccepted);
    }

    @Override
    public synchronized CoapData get(List<CoapMediaType> mediaTypesAccepted) {
        if (!isSensorActive) {
            return new CoapData(Encoder.StringToByte(this.value), CoapMediaType.text_plain); // Data stopped
        }

        float[] sensing_data = dht.getData(15); // Read sensor data
        this.value = sensing_data[1] + "," + sensing_data[0]; 
        led.updateLEDColor(sensing_data[1], sensing_data[0]); 
        return new CoapData(Encoder.StringToByte(this.value), CoapMediaType.text_plain); 
    }

    @Override
    public synchronized void delete() {
        dht.stopSensor();
        led.delete();
        isSensorActive = false;  // sensor stopped

        this.isBeingObserved = false;
        
        super.delete();
    }
    
    
    public synchronized void observe() {
            this.isBeingObserved = true;  
            this.changed(); 
    }

    @Override
    public synchronized boolean put(byte[] data, CoapMediaType type) {
        if (data == null || data.length == 0) {
            return false;  
        }
        return this.setValue(data); 
    }

    @Override
    public synchronized boolean setValue(byte[] value) {
        try {
            String[] values = Encoder.ByteToString(value).split(","); // Parse data in "32,88" format
            float temperature = Float.parseFloat(values[0]);
            float humidity = Float.parseFloat(values[1]);

            if (temperature == -99.0 || humidity == -99.0) {
                led.updateLEDColor(-99.0f, -99.0f);  
                this.value = "Checksum Error"; 
                return false;
            }

            this.value = "Temp: " + temperature + "°C, Humidity: " + humidity + "%";
            led.updateLEDColor(temperature, humidity);  
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public synchronized String getResourceType() {
        return "Raspberry pi 4 Temperature Sensor";
    }
}
