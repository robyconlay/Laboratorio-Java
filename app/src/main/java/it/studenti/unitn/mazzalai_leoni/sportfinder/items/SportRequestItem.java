package it.studenti.unitn.mazzalai_leoni.sportfinder.items;

public class SportRequestItem {

    private final String id;
//    private final String user;
    private final String sport;

    public SportRequestItem(String id, String sport) {
        this.id = id;
//        this.user = user;
        this.sport = sport;
    }

    public String getId() {
        return id;
    }

//    public String getUser() {
//        return user;
//    }

    public String getSport() {
        return sport;
    }
}
