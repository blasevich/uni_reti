import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Worker implements Runnable{
    class Aux{
        String command;
        int dim;

        public Aux(String command, int dim){
            this.command = command;
            this.dim = dim;
        }

        public String toString(){return command+" "+dim;}
    }

    private Map<String, User> users;
    private Map<String, Hotel> hotels;
    private Map<Long, Review> reviews;
    private Map<Long, Review> reviewsLocal;

    private boolean done = false;

    private Socket socket;

    private PrintWriter out;
    private BufferedReader in;

    private String _username = null;

    ////////////////////////////////////////////////////////////////////////////////////////////

    public Worker(Socket socket, Map<String, User> users, Map<String, Hotel> hotels, Map<Long, Review> reviews){
        this.socket = socket;
        this.users = users;
        this.hotels = hotels;
        this.reviews = reviews;
        this.reviewsLocal = new HashMap<>();        
    }

    public boolean goodString(String string){
        if(string != null && !string.isEmpty()){
            String regex = "^[a-zA-Z0-9]+$";
            return string.matches(regex);
        }else{
            return false;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    public void run(){
        System.out.println(Thread.currentThread() + " connected " + socket);

        try{
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while(!done){
                String request = in.readLine();

                if(request != null){
                String splitted[] = request.split(",");
                String command = splitted[0];
                int dim = splitted.length;

                Aux aux = new Aux(command, dim);

                switch(aux.toString()){
                    case "login 3":
                        login(splitted[1], splitted[2]);
                        break;
                    case "logout 1":
                        logout();
                        break;
                    case "searchHotel 3":
                        searchHotel(splitted[1], splitted[2]);
                        break;
                    case "searchAllHotel 2":
                        searchAllHotel(splitted[1]);
                        break;
                    case "insertReview 8":
                        try{
                            double rate = Double.parseDouble(splitted[3]);
                            double[] ratings = new double[4];

                            for(int i=0; i<4; i++){
                                ratings[i] = Double.parseDouble(splitted[i+4]);
                            }

                            insertReview(splitted[1], splitted[2], rate, ratings);

                        }catch(NumberFormatException e){
                            out.printf("invalid input\nEND\n");
                            break;
                        }catch(Exception e){
                            out.printf("invalid input\nEND\n");
                            break;
                        }
                        break;
                    case "showMyBadges 1":
                        showMyBadges();
                        break;
                    case "quit 1":
                        quit();
                        break;
                    default:
                        out.printf("invalid input\nEND\n");
                        break;
                }
            }else{
                out.printf("invalid input\nEND\n");
            }

            }

            socket.close();
            System.out.println(Thread.currentThread() + " disconnected " + socket);

            //sync
            for(var entry : reviewsLocal.entrySet()){
                reviews.putIfAbsent(entry.getKey(), entry.getValue());
            }

        }catch(Exception e){
            System.out.println("[WORKER] error");
            e.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    private void quit(){
        if(_username != null){
            users.get(_username).logout();
        }
        done = true;
    }

    private void login(String usr, String psw){
        if(_username == null){
            if(!goodString(usr) || !goodString(psw) || !users.containsKey(usr) || !users.get(usr).getPassword().equals(psw)){
                out.printf("wrong username or password\n");        
            }
            else if(users.get(usr).isLoggedin()){
                out.printf("already logged in in another session\n");
            }
            else{
                _username = usr;
                users.get(usr).login();
                out.printf("logged in\n");
            }

        }else{
            out.println("already logged in\n");
        }

        out.printf("END\n");
    }

    private void logout(){
        if(_username != null){
            users.get(_username).logout();
            out.println(_username + " logged out");
            _username = null;
        }else{
            out.println("not logged in");
        }

        out.println("END");
    }

    private void searchHotel(String hotelName, String city){
        if(hotelName != null && city != null){
            if(hotels.containsKey(hotelName)){
                out.printf("%s\n", hotels.get(hotelName).toString());
            }else{
                out.printf("%s hotel not found\n", hotelName);
            }
        }
        else{
            out.printf("invalid input\n");
        }

        out.printf("END\n");
    }

    private void searchAllHotel(String city){
        try{
            if(city != null){
                for(Map.Entry<String, Hotel> h : hotels.entrySet()){
                    String hotelCity = h.getValue().getCity();
                    if(hotelCity!=null && hotelCity.equals(city)){
                        out.printf("%s\n", h.getValue().toString());
                    }
                }
            }
            else{
                out.printf("invalid input\n");
            }

            out.printf("END\n");
        }
        catch(Exception e2){
            e2.printStackTrace();
        }
    }

    private void insertReview(String hotelName, String city, double rate, double[] ratings){
        if(_username != null){
            Rating newRatings = new Rating(ratings[0], ratings[1], ratings[2], ratings[3]);
            Review newReview = new Review(_username, hotelName, city, rate, newRatings);
            reviewsLocal.put(newReview.getTime(), newReview);

            users.get(_username).review_added();

            out.println("review added");
        }else{
            out.println("you must be logged in to insert a review");
        }
        out.printf("END\n");
    }

    private void showMyBadges(){
        if(_username != null){
            out.println(users.get(_username).getBadge());
        }else{
            out.println("you must be logged in to access your badge");
        }

        out.printf("END\n");
    }
}
