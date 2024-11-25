import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UpdateRanking extends Thread{
    private boolean done = false;
    private int wait;

    private Map<String, Hotel> hotels;
    private Map<Long, Review> reviews;
    private Map<String, List<Hotel>> hotelsByCity = new HashMap<>();
    private Map<String, Set<Review>> reviewsByHotel = new HashMap<>();

    private Comparator<Hotel> byRate = Comparator.comparing(Hotel::getRate);

    private Map<String, List<RMIlistener_interface>> lsiteners = new ConcurrentHashMap<>(); //utenti registrati per la callback
    private int callbackPort;
    private String callbackName;

    ////////////////////////////////////////////////////////////////////////////////////////////

    public UpdateRanking(int wait, Map<String, Hotel> hotels, Map<Long, Review> reviews, String callbackName, int callbackPort){
        this.wait = wait;
        this.hotels = hotels;
        this.reviews = reviews;
        this.callbackName = callbackName;
        this.callbackPort = callbackPort;
    }

    public void finish(){
        done = true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    public void run(){

        //dividi hotel in base alla citta
        for(Map.Entry<String, Hotel> entry : hotels.entrySet()){
            hotelsByCity.computeIfAbsent(entry.getValue().getCity(), k -> new ArrayList<>()).add(entry.getValue()); 
        }

        //listeners init - per ogni citta aggiungi chiave (nome citta)
        for(Cities c : Cities.values()){
            lsiteners.put(c.toString(), new ArrayList<RMIlistener_interface>());
        }

        //callback init
        CallbackServer callbackserver = new CallbackServer(lsiteners, hotelsByCity);
        Registry registry = null;
        try{
            RMIevent_interface stub = (RMIevent_interface) UnicastRemoteObject.exportObject(callbackserver, callbackPort);
            registry = LocateRegistry.createRegistry(callbackPort);
            Registry r = LocateRegistry.getRegistry(callbackPort);
            r.bind(callbackName, stub);
            
        }catch(Exception e){
            e.printStackTrace();
        }

        while(!done){
            try{
                Thread.sleep(wait);
            }catch(InterruptedException e){
                e.printStackTrace();
            }

            //dividi reviews in base ad hotel
            for(Map.Entry<Long, Review> entry : reviews.entrySet()){
                reviewsByHotel.computeIfAbsent(entry.getValue().getHotelName(), k -> new HashSet<>()).add(entry.getValue());
            }

            //per ogni hotel aggiorna il rate: hotels.updateRate(int tot_reviews, long time_now, List<Review> reviews)
            long time_now = System.currentTimeMillis();
            for(Map.Entry<String, Hotel> h : hotels.entrySet()){
                h.getValue().updateRate(reviews.size(), time_now, reviewsByHotel.get(h.getValue().getName()));
            }

            //struttura dati con ranking -> hotelsByCity
            for(List<Hotel> h : hotelsByCity.values()){
                boolean first = h==null;
                int old = 0;
                List<Hotel> oldList = h;
                
                if(!first){
                    old = h.get(0).getId();
                }
                
                Collections.sort(h, byRate);

                boolean startMulticast = first || h.get(0).getId() != old;
                if(startMulticast){
                    //multicast group notification
                    MulticastService multicast = new MulticastService(h.get(0).getCity(), h.get(0).getName());
                    multicast.start();
                }

                if(first || !oldList.equals(h)){
                    //rmi callback
                    String city = h.get(0).getCity();
                    
                    try{
                        callbackserver.notifyListeners(city);
                    }catch(RemoteException e){
                        e.printStackTrace();
                    }

                }
            }
        }

        //unbind
        try{
            registry.unbind(callbackName);
            UnicastRemoteObject.unexportObject(registry,true);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
