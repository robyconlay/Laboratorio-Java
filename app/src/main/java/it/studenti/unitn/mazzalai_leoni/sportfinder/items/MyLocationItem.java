package it.studenti.unitn.mazzalai_leoni.sportfinder.items;

public class MyLocationItem {

    private final String id;
    private final String name;
    private final String dateCreated;
    private final String suggestionCount;

    public MyLocationItem(String id, String name, String dateCreated, String suggestionCount) {
        this.id = id;
        this.name = name;
        this.dateCreated = dateCreated;
        this.suggestionCount = suggestionCount;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public String getSuggestionCount() {
        return suggestionCount;
    }
}
