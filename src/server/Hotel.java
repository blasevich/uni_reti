import java.util.Set;

public class Hotel {

    private int id;
    private String name;
    private String description;
    private String city;
    private String phone;
    private String[] services;
    private double rate;
    private Rating ratings; //cleaning, position, services, quality
    private int review_number;
    private long last_reviewed;

    public Hotel(int id, String name, String description, String city, String phone, String[] services, double rate, Rating ratings){
        this.id = id;
        this.name = name;
        this.description = description;
        this.city = city;
        this.phone = phone;
        this.services = services;
        this.rate = rate;
        this.ratings = ratings;
        this.review_number = 0;
        this.last_reviewed = 0;
    }

    public int getId(){
        return this.id;
    }
    public String getName(){
        return this.name;
    }
    public String getDescription(){
        return this.description;
    }
    public String getCity(){
        return this.city;
    }
    public String getPhone(){
        return this.phone;
    }
    public String getServices(){
        String temp = String.join(", ",this.services);
        return temp;
    }
    public double getRate(){
        return this.rate;
    }
    public String getRatings(){
        return ratings.toString();
    }
    public int getReview_number(){
        return review_number;
    }
    public long getLast_reviewd(){
            return last_reviewed;
        }

    public String toString(){
        return getId() + " - " + getName() + " - " + getRatings() + " - " + getServices();
    }

    public void updateRate(int tot_reviews, long time_now, Set<Review> reviews){
        if(reviews != null){
            review_number = reviews.size();

            double sum = 0;
            for(Review i : reviews){
                sum = sum + i.getRate();
            }

            double quality = sum/reviews.size();

            rate = quality*(3/5) + review_number/tot_reviews + last_reviewed/time_now;

        /************************************************************
        double quality = sum/reviews.size();
        double quantity = (review_number*5)/tot_reviews;
        double actuality = (last_reviewed*5)/time_now;
        rate = quality*(3/5) + quantity*(1/5) + actuality*(1/5);}
        ************************************************************/
        }
    }
}