import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class ServerMain{
    public static String configPath = "./server.config";

    public static Map<String, User> users = new ConcurrentHashMap<>();
    public static Map<String, Hotel> hotels = new ConcurrentHashMap<>();
    public static Map<Long, Review> reviews = new ConcurrentHashMap<>();

    private static Gson gsonHotel;
    private static Gson gsonUser;
    private static Gson gsonReview;
    private static String hotelsFile;
    private static String usersFile;
    private static String reviewsFile;

    public static boolean done = false;

    public static int update_delay;

    public static int timeout;
    public static int port;
    public static ServerSocket serverSocket;

    private static int threadpool_size;
    public static ExecutorService threadPool;

    private static int RMIport;
    private static String RMIname;
    private static int callbackPort;
    private static String callbackName;

    ////////////////////////////////////////////////////////////////////////////////////////////

    private static void importConfig(String configPath){
        try{
            Properties prop = new Properties();
            prop.load(new FileInputStream(configPath));

            hotelsFile = prop.getProperty("hotelsFile");
            usersFile = prop.getProperty("usersFile");
            reviewsFile = prop.getProperty("reviewsFile");

            update_delay = Integer.parseInt(prop.getProperty("update_delay"));

            port = Integer.parseInt(prop.getProperty("port"));
            threadpool_size = Integer.parseInt(prop.getProperty("threadpool_size"));
            timeout = Integer.parseInt(prop.getProperty("timeout"));

            RMIname = prop.getProperty("RMIname");
            RMIport = Integer.parseInt(prop.getProperty("RMIport"));
            callbackName = prop.getProperty("callbackName");
            callbackPort = Integer.parseInt(prop.getProperty("callbackPort"));
        }catch(IOException e){
            System.err.println("server config file reading error");
            System.out.println(e.getMessage());
			System.exit(1);
        }
    }

    /*private static void importJson(String file, Gson gson, List<?> list){
        try{
            FileReader fileReader = new FileReader(file);

            gson = new GsonBuilder().create();
            Type type = new TypeToken<List<?>>(){}.getType();
            list = gson.fromJson(fileReader, type);

        }catch(FileNotFoundException e){
            System.err.println(file + " does not exist - file not imported");
        }catch(Exception e){
            System.err.println("error: import from json");
            System.out.println(e.getMessage());;
        }
    }*/

    private static void exportJson(String file, Gson gson, List<?> list){
        gson = new GsonBuilder().setPrettyPrinting().create();

        String json = gson.toJson(list);
        FileWriter writer = null;

        try{
            writer = new FileWriter(file);
            writer.write(json);
            writer.close();

        }catch(FileNotFoundException e){
            System.out.println("[server - toJson] file not found");
        }catch(IOException e){
            System.out.println(e.getMessage());
        }finally{
            if(writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.err.println("Error: Failed to close the writer.");
                    e.printStackTrace();
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    public static void main(String[] args){

        //import config
        importConfig(configPath);

        //import from json
        //hotels
        try{
            FileReader hotelFile = new FileReader(hotelsFile);
            gsonHotel = new Gson();
            Type typeHotel = new TypeToken<List<Hotel>>(){}.getType();
            List<Hotel> listHotel = gsonHotel.fromJson(hotelFile, typeHotel);

            for (Hotel item : listHotel) {
                hotels.put(item.getName(), item);
            }
        }
        catch(FileNotFoundException e){
            System.err.println(hotelsFile + " does not exist - file not imported");
        }catch(Exception e){
            System.err.println("error: import hotels - file not imported\n" + e.getMessage());
        }
        //users
        try{
            FileReader userFile = new FileReader(usersFile);
            gsonUser = new Gson();
            Type typeUser = new TypeToken<List<User>>(){}.getType();
            List<User> listUser = gsonUser.fromJson(userFile, typeUser);

            for (User item : listUser) {
                users.put(item.getName(), item);
            }
        }
        catch(FileNotFoundException e){
            System.err.println(usersFile + " does not exist - file not imported");
        }catch(Exception e){
            System.err.println("error: import users - file not imported\n" + e.getMessage());
        }
        //reviews
        try{
            FileReader reviewFile = new FileReader(reviewsFile);
            gsonReview = new GsonBuilder().create();
            Type typeReview = new TypeToken<List<Review>>(){}.getType();
            List<Review> listReview = gsonReview.fromJson(reviewFile, typeReview);

            for (Review item : listReview) {
                reviews.put(item.getTime(), item);
            }
        }
        catch(FileNotFoundException e){
            System.err.println(reviewsFile + " does not exist - file not imported");
        }catch(Exception e){
            System.err.println("error: import reviews - file not imported\n" + e.getMessage());
        }

        //rmi register init
        Registry registry = null;
        try{
            RMIRegister rmiregister = new RMIRegister(users);
            RMIRegisterInterface stub = (RMIRegisterInterface) UnicastRemoteObject.exportObject(rmiregister, 0);
            registry = LocateRegistry.createRegistry(RMIport); //registry.unbind()
            Registry r = LocateRegistry.getRegistry(RMIport);
            r.rebind(RMIname, stub);
        }catch(RemoteException e){
            e.printStackTrace();
        }

        //start updateranking
        UpdateRanking updateRanking = new UpdateRanking(update_delay, hotels, reviews, callbackName, callbackPort);
        updateRanking.start();

        //threadpool
        threadPool = Executors.newFixedThreadPool(threadpool_size);
        
        //connection
        try{
            serverSocket = new ServerSocket(port);

            //shutdown if exit
            ServerExit exit = new ServerExit(timeout, threadPool, serverSocket, updateRanking);
            exit.start();
            
            System.out.println("server running");

            while(!done){
                try{
                    Socket clientsocket = serverSocket.accept();
                    clientsocket.setSoTimeout(timeout);
                    threadPool.execute(new Worker(clientsocket, users, hotels, reviews));

                }catch(SocketTimeoutException e){
                    System.out.println("socket timeout");
                }catch(SocketException e){
                    done = true;
                    break;
                }
            }

            threadPool.shutdown();
            try{
                if(!threadPool.awaitTermination(timeout, TimeUnit.MILLISECONDS)){
                    threadPool.shutdownNow();
                }
            }catch(InterruptedException e){
                e.printStackTrace();
                threadPool.shutdownNow();
            }

            updateRanking.finish();
            updateRanking.join(timeout);

        }catch(Exception e){
            e.printStackTrace();
        }

        //unbind
        try{
            registry.unbind(RMIname);
            UnicastRemoteObject.unexportObject(registry,true);
        }catch(Exception e){
            e.printStackTrace();
        }

        //export to json
        System.out.println("exporting to json");

        List<Hotel> listHotel = new ArrayList<Hotel>();
        for(Map.Entry<String, Hotel> entry : hotels.entrySet()){
            listHotel.add(entry.getValue());
        }

        List<User> listUser = new ArrayList<User>();
        for(Map.Entry<String, User> entry : users.entrySet()){
            listUser.add(entry.getValue());
        }

        List<Review> listReview = new ArrayList<Review>();
        for(Map.Entry<Long, Review> entry : reviews.entrySet()){
            listReview.add(entry.getValue());
        }

        exportJson(hotelsFile, gsonHotel, listHotel);
        exportJson(usersFile, gsonUser, listUser);
        exportJson(reviewsFile, gsonReview, listReview);

        System.out.println("[server] shutting down");
        System.exit(0);
    }
}