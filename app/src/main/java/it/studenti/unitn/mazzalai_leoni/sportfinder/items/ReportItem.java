package it.studenti.unitn.mazzalai_leoni.sportfinder.items;

public class ReportItem {

    private final String id;
    private final String reviewID;
    private final String text;
    private final String timesReported;

    public ReportItem(String id, String reviewID, String text, String timesReported) {
        this.id = id;
        this.reviewID = reviewID;
        this.text = text;
        this.timesReported = timesReported;
    }


    public String getId() {
        return id;
    }

    public String getReviewID() {
        return reviewID;
    }

    public String getText() {
        return text;
    }


    public String getTimesReported() {
        return timesReported;
    }
}
