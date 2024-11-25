import java.io.Serializable;

public class User implements Serializable{
    private String name;
    private String password;
    private boolean loggedin;
    private int reviews_number;
    private String badge;

    public User(String name, String password){
        this.name = name;
        this.password = password;
        this.reviews_number = 0;
        this.badge = null;
        this.loggedin = false;
    }

    public String getName(){
        return name;
    }

    public String getPassword(){
        return password;
    }

    public int getReviews_number(){
        return reviews_number;
    }

    public String getBadge(){
        if(badge != null)
            return badge;
        else
            return "no badge yet :(";
    }

    public boolean isLoggedin(){
        return loggedin;
    }
    public void login(){
        loggedin = true;
    }
    public void logout(){
        loggedin = false;
    }

    public void review_added(){
        reviews_number++;

        switch(reviews_number){
            case 8:
                badge = "reviewer";
                break;
            case 16:
                badge = "expert_reviewer";
                break;
            case 32:
                badge = "contributor";
                break;
            case 64:
                badge = "expert_contributor";
                break;
            case 128:
                badge = "super_contributor";
                break;
            default:
                break;
        }
    }

}
