package it.studenti.unitn.mazzalai_leoni.sportfinder.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import it.studenti.unitn.mazzalai_leoni.sportfinder.R;
import it.studenti.unitn.mazzalai_leoni.sportfinder.items.SuggestedChangesItem;

public class SuggestedChangesAdapter extends  RecyclerView.Adapter<SuggestedChangesAdapter.SuggestedChangesViewHolder>{

    private ArrayList<SuggestedChangesItem> suggestedList;
    private OnSuggestedChangeClickListener monSuggestedChangeClickListener;

    public SuggestedChangesAdapter(ArrayList<SuggestedChangesItem> suggestedList, OnSuggestedChangeClickListener onSuggestedChangeClickListener) {
        this.suggestedList = suggestedList;
        this.monSuggestedChangeClickListener = onSuggestedChangeClickListener;
    }

    @NonNull
    @Override
    public SuggestedChangesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_suggested_change, parent, false);
        SuggestedChangesViewHolder scvh = new SuggestedChangesViewHolder(view, monSuggestedChangeClickListener);
        return scvh;
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestedChangesViewHolder holder, int position) {
        SuggestedChangesItem currentItem = suggestedList.get(position);
        holder.autoreTV.setText(currentItem.getAuthor());
        holder.messaggioTV.setText(currentItem.getMessage());
        holder.immaginePresenteTV.setText(currentItem.isImagePresent() ? "Sì" : "No");
        holder.tempoPresenteTV.setText(currentItem.isTimePresent() ? "Sì" : "No");
    }

    @Override
    public int getItemCount() {
        return suggestedList.size();
    }

    public interface OnSuggestedChangeClickListener {
        void onClick(int position);
    }

    public static class SuggestedChangesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView autoreTV, messaggioTV, immaginePresenteTV, tempoPresenteTV;
        OnSuggestedChangeClickListener onSuggestedChangeClickListener;

        public SuggestedChangesViewHolder(@NonNull View itemView, OnSuggestedChangeClickListener onSuggestedChangeClickListener) {
            super(itemView);
            this.autoreTV = itemView.findViewById(R.id.autoreTV);
            this.messaggioTV = itemView.findViewById(R.id.messaggioTV);
            this.immaginePresenteTV = itemView.findViewById(R.id.immaginePresenteTV);
            this.tempoPresenteTV = itemView.findViewById(R.id.tempoPresenteTV);
            this.onSuggestedChangeClickListener = onSuggestedChangeClickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onSuggestedChangeClickListener.onClick(getAdapterPosition());
        }
    }

}
