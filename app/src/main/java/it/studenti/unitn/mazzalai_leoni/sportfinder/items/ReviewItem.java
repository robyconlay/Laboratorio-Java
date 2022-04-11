package it.studenti.unitn.mazzalai_leoni.sportfinder.items;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

/**
 * this class is needed for RedcyclerView to work
 */
public class ReviewItem {

    private final String id;
    private final String review;
    private final boolean author;
    private final String user;
    private final String timestamp;
    private final ArrayList<String> upvotes;


    public ReviewItem(String id, String review, String user, boolean author, String timestamp) {
        this.id = id;
        this.review = review;
        this.user = user;
        this.author = author;
        this.timestamp = timestamp;
        this.upvotes = new ArrayList<>();
    }

    public ReviewItem(String id, String review, String user, boolean author, String timestamp, ArrayList<String> upvotes) {
        this.id = id;
        this.review = review;
        this.user = user;
        this.author = author;
        this.timestamp = timestamp;
        this.upvotes = upvotes;
    }


    public String getId() {
        return id;
    }

    public String getReview() {
        return review;
    }

    public String getUser() {
        return user;
    }

    public boolean isAuthor() {
        return author;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public ArrayList<String> getUpvotes() {
        return upvotes;
    }

    public void upvote(String user) {
        if (!upvotes.contains(user)) {
            upvotes.add(user);
        }
    }

    public void novote(String user) {
        upvotes.remove(user);
    }

    public int getTotalVotes() {
        return upvotes.size();
    }

    public String getStatus() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null && upvotes.contains(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
            return "upvoted";
        }
        return "novote";
    }

}
