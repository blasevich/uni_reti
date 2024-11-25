import java.rmi.RemoteException;
import java.util.Map;

public class RMIRegister implements RMIRegisterInterface{
    private Map<String, User> users;
    private User newUser;

    protected RMIRegister(Map<String, User> users) throws RemoteException{
        super();
        this.users = users;
    }

    public boolean goodString(String string){
        if(string != null && !string.isEmpty()){
            String regex = "^[a-zA-Z0-9]+$";
            return string.matches(regex);
        }else{
            return false;
        }
    }
    
    public boolean register(String username, String password) throws RemoteException{
        if(!goodString(username) || !goodString(password))
            return false;

        newUser = new User(username, password);

        if(users.putIfAbsent(newUser.getName(), newUser) == null){
            System.out.println("[rmi register server] new user created " + username);
            return true;
        }

        return false;
    }    
}
