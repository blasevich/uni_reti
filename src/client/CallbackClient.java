import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public class CallbackClient implements RMIlistener_interface{
    private Map<String, List<String>> hotelsRanking;

    public CallbackClient(Map<String, List<String>> hotelsRanking){
        super();
        this.hotelsRanking = hotelsRanking;
    }
    
    public void event_happened(String city, List<String> rankedHotels) throws RemoteException{
        System.out.println("***new ranking for " + city + " ***");

        hotelsRanking.put(city, rankedHotels);

        for(String s : hotelsRanking.get(city)){
            System.out.println(s);
        }
    }
}
