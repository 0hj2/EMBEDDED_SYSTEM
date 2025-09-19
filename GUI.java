package Final;

import java.awt.Font;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.ws4d.coap.core.CoapClient;
import org.ws4d.coap.core.CoapConstants;
import org.ws4d.coap.core.connection.BasicCoapChannelManager;
import org.ws4d.coap.core.connection.api.CoapChannelManager;
import org.ws4d.coap.core.connection.api.CoapClientChannel;
import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.enumerations.CoapRequestCode;
import org.ws4d.coap.core.messages.api.CoapRequest;
import org.ws4d.coap.core.messages.api.CoapResponse;
import org.ws4d.coap.core.rest.CoapData;
import org.ws4d.coap.core.tools.Encoder;

public class GUI extends JFrame implements CoapClient {
   private static final boolean exitAfterResponse = false;
   JButton btn_get = new JButton("GET");
   JButton btn_delete = new JButton("EXIT");
   JButton btn_observe = new JButton("OBSERVE");
   JButton btn_put = new JButton("PUT");

   JLabel path_label = new JLabel("Path");
   JTextArea path_text = new JTextArea("/temp", 1, 1);
   JLabel payload_label = new JLabel("Payload");
   JTextArea payload_text = new JTextArea("", 1, 1);
   JTextArea display_text = new JTextArea();
   JScrollPane display_text_jp = new JScrollPane(display_text);
   JLabel display_label = new JLabel("Display");

   CoapClientChannel clientChannel = null;
   private boolean observing = false;
   private boolean deleteCalled = false;  // Flag to track if DELETE was called

   public GUI(String serverAddress, int serverPort) {
      super("GUI CLIENT");
      this.setLayout(null);
      String sAddress = serverAddress;
      int sPort = serverPort;

      CoapChannelManager channelManager = BasicCoapChannelManager.getInstance();

      try {
         clientChannel = channelManager.connect(this, InetAddress.getByName(sAddress), sPort);
      } catch (UnknownHostException e) {
         e.printStackTrace();
         System.exit(-1);
      }

      if (null == clientChannel) {
         return;
      }
      
      // 색상 정의
      Color backgroundColor = new Color(214, 230, 245);  // 전체 배경 (파스텔 블루)
      Color displayBackgroundColor = new Color(255, 250, 240);  // display_text 배경 (크림색)
      Color textColor = new Color(85, 58, 48);             // 일반 텍스트 (다크 브라운)
      Color buttonBackgroundColor = new Color(255, 250, 240);  // 버튼 배경 (크림색)
      Color buttonTextColor = new Color(85, 58, 48);      // 버튼 텍스트 (다크 브라운)

      // 프레임 배경색 설정
      this.getContentPane().setBackground(backgroundColor);

      // display_text 색상 설정
      display_text.setBackground(displayBackgroundColor);
      display_text.setForeground(textColor);

      // 버튼 색상 설정
      btn_get.setBackground(buttonBackgroundColor);
      btn_get.setForeground(buttonTextColor);
      btn_observe.setBackground(buttonBackgroundColor);
      btn_observe.setForeground(buttonTextColor);
      btn_put.setBackground(buttonBackgroundColor);
      btn_put.setForeground(buttonTextColor);
      btn_delete.setBackground(buttonBackgroundColor);
      btn_delete.setForeground(buttonTextColor);

      // 텍스트 필드 설정
      payload_text.setBackground(displayBackgroundColor);
      payload_text.setForeground(textColor);
      path_text.setBackground(displayBackgroundColor);
      path_text.setForeground(textColor);

      // 레이블 색상 설정
      path_label.setForeground(textColor);
      payload_label.setForeground(textColor);
      display_label.setForeground(textColor);
      
      
      // Button setup
      btn_get.setBounds(20, 670, 150, 50);
      btn_observe.setBounds(180, 670, 150, 50);
      btn_put.setBounds(340, 670, 150, 50);
      btn_delete.setBounds(600, 670, 150, 50);
      
      
      // GET button action listener
      btn_get.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            if (deleteCalled) return; // Skip action if DELETE was called
            String path = path_text.getText();
            String payload = payload_text.getText();
            CoapRequest request = clientChannel.createRequest(CoapRequestCode.GET, path, true);
            displayRequest(request);
            clientChannel.sendMessage(request);
         }
      });

      // PUT button action listener
      btn_put.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            if (deleteCalled) return; // Skip action if DELETE was called
            String path = path_text.getText();
            String payload = payload_text.getText();
            CoapRequest request = clientChannel.createRequest(CoapRequestCode.PUT, path, true);
            request.setPayload(new CoapData(payload, CoapMediaType.text_plain));
            displayRequest(request);
            clientChannel.sendMessage(request);
         }
      });

      // DELETE button action listener (Stop observing and delete the resource)
      btn_delete.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            deleteCalled = true;  // Set the flag to true when delete is triggered
            String path = path_text.getText();
            CoapRequest request = clientChannel.createRequest(CoapRequestCode.DELETE, path, true);
            displayRequest(request);
            clientChannel.sendMessage(request);

            // Stop observation when DELETE is called
            if (observing) {
               stopObservation();
            }
            
            //System.exit(0);
         }
      });

      // Observe button action listener
      btn_observe.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            if (deleteCalled) return; // Skip action if DELETE was called
            String path = path_text.getText();
            String payload = payload_text.getText();
            if (observing) {
               stopObservation();  // Stop observation if it's already active
            } else {
               startObservation(path);
            }
         }
      });

      payload_label.setBounds(20, 570, 350, 30);
      payload_text.setBounds(20, 600, 470, 30);
      payload_text.setFont(new Font("Arial", Font.BOLD, 15));
      
      path_label.setBounds(20, 500, 350, 30);
      path_text.setBounds(20, 530, 470, 30);
      path_text.setFont(new Font("Arial", Font.BOLD, 15));

      display_label.setBounds(20, 10, 100, 20);
      display_text.setLineWrap(true);
      display_text.setFont(new Font("Arial", Font.BOLD, 15));
      display_text_jp.setBounds(20, 40, 740, 430);

      this.add(btn_get);
      this.add(btn_delete);
      this.add(btn_observe);
      this.add(btn_put);
      this.add(path_text);
      this.add(path_label);
      this.add(payload_label);
      this.add(payload_text);
      this.add(display_text_jp);
      this.add(display_label);

      this.setSize(800, 800);
      this.setVisible(true);
      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   }
   
   @Override
   public void onConnectionFailed(CoapClientChannel channel, boolean notReachable, boolean resetByServer) {
      System.out.println("Connection Failed");
      System.exit(-1);
   }

   @Override
   public void onResponse(CoapClientChannel channel, CoapResponse response) {
      if (deleteCalled) {
         System.exit(0);
          return;
      }
      
      if (response.getPayload() != null) {
           String payload = Encoder.ByteToString(response.getPayload());

           try {
               String[] parts = payload.split(",");
               if (parts.length == 2) {
                   try {
                       double temperature = Double.parseDouble(parts[0].trim());
                       double humidity = Double.parseDouble(parts[1].trim());
                       String ledColor = decideLedColor(temperature, humidity);
                       display_text.append("Temperature: " + temperature + "°C, Humidity: " + humidity + "%\n");
                       display_text.append("LED Color: " + ledColor + "\n");
                   } catch (NumberFormatException e) {
                       display_text.append("Error: Invalid number format in payload.\n");
                   }
               } else {
                   display_text.append("Error: Payload format - 'Temperature,Humidity'.\n");
               }
           } catch (Exception e) {
               display_text.append("Error: " + e.getMessage() + "\n");
           }
       } else {
           // Handle if there's no payload
       }
       display_text.append(System.lineSeparator());
       display_text.append("*");
       display_text.append(System.lineSeparator());
   }

   private String decideLedColor(double temperature, double humidity) {
      if (temperature == -99.0 || humidity == -99.0) {
           return "off";
      } else if (temperature <= 0) {
           return "White";
      } else if (temperature >= 30) {
           return "Red";
      } else if (humidity >= 80) {
           return "Blue";
      } else {
           return "Green";
      }
   }

   @Override
   public void onMCResponse(CoapClientChannel channel, CoapResponse response, InetAddress srcAddress, int srcPort) {
      // Not implemented
   }

   private void displayRequest(CoapRequest request) {
      if (request.getPayload() != null) {
           display_text.append("Path: " + request.getUriPath() + "\n");
           display_text.append("Payload: " + Encoder.ByteToString(request.getPayload()) + "\n");
      }
      display_text.append(System.lineSeparator());
      display_text.append("*");
      display_text.append(System.lineSeparator());
   }

   // Start observing the resource
   private void startObservation(String path) {
      CoapRequest request = clientChannel.createRequest(CoapRequestCode.GET, path, true);
      request.setObserveOption(0);  // Start observation
      displayRequest(request);
      clientChannel.sendMessage(request);
      observing = true;
   }

   // Stop observing the resource
   private void stopObservation() {
      CoapRequest request = clientChannel.createRequest(CoapRequestCode.GET, path_text.getText(), true);
      request.setObserveOption(-1); // Cancel observation
      displayRequest(request);
      clientChannel.sendMessage(request);
      observing = false;
   }

   public static void main(String[] args) {
      GUI gui = new GUI("fe80::1eab:f314:a654:9a3b", CoapConstants.COAP_DEFAULT_PORT);
   }
}
