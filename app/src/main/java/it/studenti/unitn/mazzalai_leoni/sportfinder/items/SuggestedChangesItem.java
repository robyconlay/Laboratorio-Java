package it.studenti.unitn.mazzalai_leoni.sportfinder.items;

public class SuggestedChangesItem {

    private String id;
    private String author;
    private String message;
    private boolean hasImage;
    private boolean hasTime;

    public SuggestedChangesItem(String id, String author, String message, boolean hasImage, boolean hasTime) {
        this.id = id;
        this.author = author;
        this.message = message;
        this.hasImage = hasImage;
        this.hasTime = hasTime;
    }

    public String getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getMessage() {
        return message;
    }

    public boolean isImagePresent() {
        return hasImage;
    }

    public boolean isTimePresent() {
        return hasTime;
    }

}
