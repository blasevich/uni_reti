public enum Cities {
    Ancona(1),
    Aosta(2),
    Bari(3),
    Bologna(4),
    Cagliari(5),
    Campobasso(6),
    Catanzaro(7),
    Firenze(8),
    Genova(9),
    LAquila(10),
    Milano(11),
    Napoli(12),
    Palermo(13),
    Perugia(14),
    Potenza(15),
    Roma(16),
    Torino(17),
    Trento(18),
    Trieste(19),
    Venezia(20);

    private final int i;

    Cities(int i){
        this.i = i;
    }

    public int getNumber(){
        return i;
    }

    public static Cities getEnum(int number){
        for(Cities cities : Cities.values()){
            if(cities.i == number){
                return cities;
            }
        }
        
        return null;
    }
    
    public String toString(){
        return name();
    }
}

