import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface RMIevent_interface extends Remote{//server
    public void addListener(RMIlistener_interface listener, ArrayList<String> cities) throws RemoteException;

    public void removeListener(RMIlistener_interface listener, ArrayList<String> cities) throws RemoteException;

    public void notifyListeners(String city) throws RemoteException;
}
