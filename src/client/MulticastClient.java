import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

public class MulticastClient extends Thread{
    private String hostname;
    private MulticastSocket socket;
    private InetAddress group;

    private byte[] buffer = new byte[1024];
    private DatagramPacket packet;

    private boolean done = false;

    public MulticastClient(String hostname, MulticastSocket socket){
        this.hostname = hostname;
        this.socket = socket;
    }

    public void finish(){
        //System.out.println("[multicast client] exiting...");
        done = true;
        
        try{
            socket.leaveGroup(group);
            socket.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        

        this.interrupt();
        
    }

    public void run(){
        try{
            group = InetAddress.getByName(hostname);
            socket.joinGroup(group);
            String received;

            System.out.println("***joined multicast group***");

            while(!done){
                packet = new DatagramPacket(buffer, buffer.length);

                try{
                    socket.receive(packet);
                
                    received = new String(packet.getData(), 0, packet.getLength());

                    System.out.println("***from multicast group***\nnuovi hotel in prima posizione: ");

                    while(received != null && !received.isEmpty() && !received.equals("END")){
                        System.out.println(received);
                    }
                }catch(SocketException e){}
            }

            System.out.println("***left multicast group***");

        }catch(Exception e){
            System.out.println("[CLIENT MULTICAST] errore");
            e.printStackTrace();
        }
    }
}
