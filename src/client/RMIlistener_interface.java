import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RMIlistener_interface extends Remote{//client
    public void event_happened(String city, List<String> rankedHotels) throws RemoteException;
}
