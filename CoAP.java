package Final;

import org.ws4d.coap.core.rest.CoapResourceServer;

public class CoAP {
   private static CoAP coapServer;
   private CoapResourceServer resourceServer;

   public static void main(String[] args) {
      coapServer = new CoAP();
      coapServer.start();
   }

   public void start() {
      System.out.println("===Run Test Server ===");

      // create server
      if (this.resourceServer != null)
         this.resourceServer.stop();
      this.resourceServer = new CoapResourceServer();

      // initialize resource
      LED led = new LED();
      DHT dht = new DHT();
      
      //CoapResourceServer에 observe하려는 resource 등록
      dht.registerServerListener(resourceServer);

      // add resource to server
      this.resourceServer.createResource(dht);
      this.resourceServer.createResource(led);

      // run the server
      try {
         this.resourceServer.start();
      } catch (Exception e) {
         e.printStackTrace();
      }
      while (true) {
         try {
            Thread.sleep(5000);
            dht.changed();
            led.changed();
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }
}

