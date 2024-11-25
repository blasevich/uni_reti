public class Review {
    //private int Hotel_id;
    private String userName;
    private String hotelName;
    private String city;
    private double rate;
    private Rating ratings; //cleaning, position, services, quality
    private long time;

    public Review(String userName, String hotelName, String city, double rate, Rating ratings){
        this.userName = userName;
        this.hotelName = hotelName;
        this.city = city;
        this.rate = rate;
        this.ratings = ratings;
        this.time = System.currentTimeMillis();
    }

    /*public int getHotel_id(){
        return Hotel_id;
    }*/

    public String getUserName(){
        return userName;
    }

    public String getHotelName(){
        return hotelName;
    }

    public String getCity(){
        return city;
    }

    public double getRate(){
        return rate;
    }

    public String getRatings(){
        return ratings.toString();
    }

    public long getTime(){
        return time;
    }

}
