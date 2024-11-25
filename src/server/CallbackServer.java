import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CallbackServer implements RMIevent_interface{
    private Map<String, List<RMIlistener_interface>> listeners;
    private Map<String, List<Hotel>> hotelsByCity;

    private boolean isCity(String string){
        for(Cities c : Cities.values()){
            if(c.name().equals(string))
                return true;
        }
        return false;
    }

    public CallbackServer(Map<String, List<RMIlistener_interface>> listeners, Map<String, List<Hotel>> hotelsByCity){
        this.listeners = listeners;
        this.hotelsByCity = hotelsByCity;
    }

    public void addListener(RMIlistener_interface listener, ArrayList<String> cities) throws java.rmi.RemoteException{
        for(String c : cities){
            if(c != null && isCity(c))
                listeners.get(c).add(listener);
        }
    }
    public void removeListener(RMIlistener_interface listener, ArrayList<String> cities) throws java.rmi.RemoteException{
        for(String c : cities){
            if(c != null && isCity(c))
                listeners.get(c).remove(listener);
        }
    }

    public void notifyListeners(String city) throws java.rmi.RemoteException{
        if(listeners.get(city) != null){
            List<String> rankedHotels = new ArrayList<>();

            for(Hotel h : hotelsByCity.get(city)){
                rankedHotels.add(h.getName());
            }

            for(RMIlistener_interface l : listeners.get(city)){
                l.event_happened(city, rankedHotels);
            }
        }else{
            System.out.println("[callback server]: no listeners for " + city);
        }
    }
}
