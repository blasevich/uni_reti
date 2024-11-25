import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MulticastSocket;
import java.net.Socket;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

public class ClientMain {
    private static String configPath = "./client.config";
    private static int port;
    private static String hostname;
    //public static int buffer_size;
    private static Socket socket;

    private static int mport;
    private static String mhostname;
    private static MulticastSocket msocket;
    private static MulticastClient multicastClient;
    
    private static Scanner scanner = new Scanner(System.in);
    private static PrintWriter out;
    private static BufferedReader in;

    private static boolean done = false;

    private static boolean loggedin = false;
    private static String _username = null;
    private static boolean notifications = false;
    private static String reply;

    private static int RMIport;
    private static String RMIname;
    private static int callbackPort;
    private static String callbackName;
    private static ArrayList<String> cities;
    private static Map<String, List<String>> hotelsRanking = new HashMap<>();

    ////////////////////////////////////////////////////////////////////////////////////////////

    private static void importConfig(String configPath){
        try{
            Properties prop = new Properties();
            prop.load(new FileInputStream(configPath));

            hostname = prop.getProperty("hostname");
            port = Integer.parseInt(prop.getProperty("port"));
            mhostname = prop.getProperty("mhostname");
            mport = Integer.parseInt(prop.getProperty("mport"));
            RMIname = prop.getProperty("RMIname");
            RMIport = Integer.parseInt(prop.getProperty("RMIport"));
            callbackName = prop.getProperty("callbackName");
            callbackPort = Integer.parseInt(prop.getProperty("callbackPort"));

        }catch(IOException e){
            System.err.println("client config file reading error");
            e.printStackTrace();
			System.exit(1);
        }
    }

    private static void callback(ArrayList<String> cities){
        try{
            Registry registry = LocateRegistry.getRegistry(callbackPort);
            RMIevent_interface server = (RMIevent_interface) registry.lookup(callbackName);
            RMIlistener_interface client = new CallbackClient(hotelsRanking); //
            RMIlistener_interface stub = (RMIlistener_interface) UnicastRemoteObject.exportObject(client, 0);

            if(notifications){
                server.addListener(stub, cities);
            }else{
                server.removeListener(stub, cities);
            }
            
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private static void ClientExit(){
        if(loggedin){
            out.println("logout");
            loggedin = false;

            //leave multicast group
            multicastClient.finish();
        }

        if(notifications){
            notifications = false;
            callback(cities);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    
    public static void main(String args[]){
        //import config
        importConfig(configPath);

        //connect to server
        try{
            socket = new Socket(hostname, port);
            System.out.println("connected to: " + socket);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            while(!done){
                System.out.printf("insert command: ");
                if(loggedin){
                    System.out.println("logout, searchHotel, searchAllHotel, insertReview, showMyBadges, quit");
                }else{System.out.println("register, login, searchHotel, searchAllHotel, quit");}

                String command = scanner.nextLine();

                switch(command){
                    case "register":
                        register();
                        break;
                    case "login":
                        login();
                        break;
                    case "logout":
                        logout();
                        break;
                    case "searchHotel":
                        searchHotel();
                        break;
                    case "searchAllHotel":
                        searchAllHotel();
                        break;
                    case "insertReview":
                        insertReview();
                        break;
                    case "showMyBadges":
                        showMyBadges();
                        break;
                    case "quit":
                        quit();
                        break;
                    default:
                        System.out.println("invalid command");
                        break;
                }
            }

            socket.close();

            System.out.println("disconnected from: " + socket);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    private static void quit(){
        out.println("quit");

        ClientExit();
        done = true;
    }

    private static void register(){
        System.out.println("username");
        String username = scanner.nextLine();
        System.out.println("password");
        String password = scanner.nextLine();

        RMIRegisterInterface server;
        Remote remote;

        try{
            Registry r = LocateRegistry.getRegistry(RMIport);
            remote = r.lookup(RMIname);
            server = (RMIRegisterInterface) remote;

            boolean res = server.register(username, password);

            if(res){
                System.out.println(username + " registered");
                System.out.println("login:");
                login();
            }else{
                System.out.println("registration failed");
            }

        }catch(Exception e){
            e.printStackTrace();
        }   
    }

    private static void login(){
        System.out.println("username");
        String username = scanner.nextLine();
        System.out.println("password");
        String password = scanner.nextLine();

        System.out.println("register for notifications? [y/n]");
        String res = scanner.nextLine();
        if(res.equals("y")){
            notifications = true;
            //ask for cities
            cities = new ArrayList<String>();
            System.out.println("insert cities:");
            while((res = scanner.nextLine()) != null && !res.isEmpty()){
                cities.add(res);
            }
        }

        out.printf("login,%s,%s\n", username, password);

        try{
            while((reply = in.readLine()) != null && !reply.equals("END")){
                System.out.println(reply);

                if(reply.equals("logged in")){
                    _username = username;
                    loggedin = true;

                    //multicast
                    msocket = new MulticastSocket(mport);
                    multicastClient = new MulticastClient(mhostname, msocket);
                    multicastClient.start();

                    if(notifications){
                        //rmi callback
                        callback(cities);
                    }
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private static void logout(){
        if(loggedin && _username != null){
            loggedin = false;
            _username = null;

            out.println("logout");

            try{
                while((reply = in.readLine()) != null && !reply.equals("END")){
                    System.out.println(reply);
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    private static void searchHotel(){
        System.out.println("hotel name: ");
        String hotelName = scanner.nextLine();

        System.out.println("city name: ");
        String city = scanner.nextLine();

        out.printf("searchHotel,%s,%s\n", hotelName, city);

        try{
            while((reply = in.readLine()) != null && !reply.equals("END")){
                System.out.println(reply);
            }
        }catch(IOException e){
            e.printStackTrace();
        } 
    }

    private static void searchAllHotel(){
        System.out.println("city name: ");
        String city = scanner.nextLine();

        out.printf("searchAllHotel,%s\n", city);

        try{
            while((reply = in.readLine()) != null && !reply.equals("END")){
                System.out.println(reply);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private static void insertReview(){
        System.out.println("hotel: ");
        String hotelName = scanner.nextLine();
        System.out.println("city name: ");
        String city = scanner.nextLine();
        System.out.println("rate: ");
        double rate = scanner.nextDouble();

        System.out.println("ratings (cleaning, position, services, quality): ");
        String[] askRatings = {"cleaning", "position", "services", "quality"};
        double ratings[] = new double[4];
        for(int i=0; i<4; i++){
            System.out.println(askRatings[i] + ": ");
            ratings[i] = scanner.nextDouble();
        }

        out.printf("insertReview,%s,%s,%.1f,%.1f,%.1f,%.1f,%.1f\n", hotelName, city, rate, ratings[0], ratings[1], ratings[2], ratings[3]);

        try{
            while((reply = in.readLine()) != null && !reply.equals("END")){
                System.out.println(reply);
            }
        }catch(IOException e){ 
            e.printStackTrace();
        }
    }

    private static void showMyBadges(){
        out.println("showMyBadges");

        try{
            while((reply = in.readLine()) != null && !reply.equals("END")){
                System.out.println(reply);
            }
        }catch(IOException e){
            e.printStackTrace();
        } 
    }
}
