import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Properties;

public class MulticastService extends Thread{
    private int port;
    private String hostname;

    private InetAddress group;
    private DatagramSocket socket;
    private DatagramPacket packet;
    private byte[] buffer;

    private String message;

    private String city;
    private String hotelName;

    public MulticastService(String city, String hotelName){
        this.city = city;
        this.hotelName = hotelName;
    }

    private void importConfig(String configPath){
        try{
            Properties prop = new Properties();
            prop.load(new FileInputStream(configPath));

            port = Integer.parseInt(prop.getProperty("mport"));
            hostname = prop.getProperty("mhostname");

        }catch(IOException e){
            System.err.println("[MULTICAST SERVICE] config file reading error");
            e.printStackTrace();
			System.exit(1);
        }
    }

    public void run(){
        //import config
        importConfig("./server.config");

        try{
            socket = new DatagramSocket();
            group = InetAddress.getByName(hostname);

            message = city + ": " + hotelName;
            buffer = message.getBytes();
            packet = new DatagramPacket(buffer, buffer.length, group, port);
            socket.send(packet);

            message = "END";
            buffer = message.getBytes();
            packet = new DatagramPacket(buffer, buffer.length, group, port);
            socket.send(packet);

            socket.close();
            
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    
}
