import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIRegisterInterface extends Remote{
    boolean register(String username, String password) throws RemoteException;    
}
