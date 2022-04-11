package it.studenti.unitn.mazzalai_leoni.sportfinder.adapters;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import it.studenti.unitn.mazzalai_leoni.sportfinder.R;
import it.studenti.unitn.mazzalai_leoni.sportfinder.activities.LocationActivity;
import it.studenti.unitn.mazzalai_leoni.sportfinder.items.ReviewItem;

/**
 * this class is needed for RedcyclerView to work
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private static final String TAG = "ReviewAdapter";
    private ArrayList<ReviewItem> reviewList;
    private OnReviewActionsListener mOnReviewActionsListener;

    public ReviewAdapter(ArrayList<ReviewItem> reviewList, OnReviewActionsListener onReviewActionsListener) {
        this.reviewList = reviewList;
        this.mOnReviewActionsListener = onReviewActionsListener;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_review, parent, false);
        return new ReviewViewHolder(view, mOnReviewActionsListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ReviewItem currentItem = reviewList.get(position);
        holder.reviewTV.setText(currentItem.getReview());
        holder.autoreTV.setVisibility((currentItem.isAuthor() ? View.VISIBLE : View.INVISIBLE));
        holder.timestampTV.setText(currentItem.getTimestamp());
        holder.votesTV.setText(String.valueOf(currentItem.getTotalVotes()));

        holder.status = currentItem.getStatus();

        if ("upvoted".equals(currentItem.getStatus())) {
            holder.upvoteIV.setColorFilter(Color.parseColor("#FF8b60"));
        }
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public interface OnReviewActionsListener {
        void onMoreActions(int position);

        void onUpvoteReview(int position);

        void onNoVoteReview(int position);
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView reviewTV, autoreTV, timestampTV, votesTV;
        ImageView moreIW, upvoteIV;
        OnReviewActionsListener onReviewActionsListener;
        String status;

        public ReviewViewHolder(View itemView, OnReviewActionsListener onReviewActionsListener) {
            super(itemView);
            reviewTV = itemView.findViewById(R.id.reviewTV);
            autoreTV = itemView.findViewById(R.id.autoreTV);
            timestampTV = itemView.findViewById(R.id.timestampTV);
            votesTV = itemView.findViewById(R.id.votesTV);
            moreIW = itemView.findViewById(R.id.moreIW);
            upvoteIV = itemView.findViewById(R.id.upvoteIV);
            this.onReviewActionsListener = onReviewActionsListener;

            moreIW.setOnClickListener(this);

            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                upvoteIV.setOnClickListener(this);
            } else {
                upvoteIV.setEnabled(false);
            }
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.moreIW:
                    onReviewActionsListener.onMoreActions(getAdapterPosition());
                    break;
                case R.id.upvoteIV:
                    switch (status) {
                        case "upvoted":
                            upvoteIV.setColorFilter(R.color.black);
                            onReviewActionsListener.onNoVoteReview(getAdapterPosition());
                            status = "novote";
                            break;
                        case "novote":
                            upvoteIV.setColorFilter(Color.parseColor("#FF8b60"));
                            onReviewActionsListener.onUpvoteReview(getAdapterPosition());
                            status = "upvoted";
                            break;
                    }
                    break;
                default:
                    break;
            }
        }

    }
}
