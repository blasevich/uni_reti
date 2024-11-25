public class Rating{
    private double cleaning;
    private double position;
    private double services;
    private double quality;

    public Rating(double c, double p, double s, double q){
        this.cleaning = c;
        this.position = p;
        this.services = s;
        this.quality = q;
    }

    public double cleaning(){
        return cleaning;
    }
    public double position(){
        return position;
    }
    public double services(){
        return services;
    }
    public double quality(){
        return quality;
    }

    public String toString(){
        return cleaning + " " + position + " " + services + " " + quality;
    }
}