package it.studenti.unitn.mazzalai_leoni.sportfinder.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import it.studenti.unitn.mazzalai_leoni.sportfinder.R;
import it.studenti.unitn.mazzalai_leoni.sportfinder.items.MyLocationItem;

public class MyLocationAdapter extends RecyclerView.Adapter<MyLocationAdapter.LocationViewHolder> {

    private ArrayList<MyLocationItem> locationList;
    private OnLocationActionsListener mOnLocationActionsListener;

    public MyLocationAdapter(ArrayList<MyLocationItem> locationList, OnLocationActionsListener mOnLocationActionsListener) {
        this.locationList = locationList;
        this.mOnLocationActionsListener = mOnLocationActionsListener;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_my_locations, parent, false);
        LocationViewHolder lvh = new LocationViewHolder(view, mOnLocationActionsListener);
        return lvh;
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        MyLocationItem currentItem = locationList.get(position);
        holder.nomeLocationTV.setText(currentItem.getName());
        holder.dataLocationTV.setText(currentItem.getDateCreated());
        holder.numeroProposteTV.setText("Ci sono " + currentItem.getSuggestionCount() + " modifiche proposte");
        if (Integer.parseInt(currentItem.getSuggestionCount()) == 0) {
            holder.visualizzaProposteButton.setEnabled(false);
        }
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    public interface OnLocationActionsListener {
        void onOpenLocation(int position);

        void onOpenSuggestions(int position);
    }

    public static class LocationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView nomeLocationTV, dataLocationTV, numeroProposteTV;
        Button visualizzaLocationButton, visualizzaProposteButton;
        OnLocationActionsListener onLocationActionsListener;

        public LocationViewHolder(View itemView, OnLocationActionsListener onLocationActionsListener) {
            super(itemView);
            this.nomeLocationTV = itemView.findViewById(R.id.nomeLocationTV);
            this.dataLocationTV = itemView.findViewById(R.id.dataLocationTV);
            this.numeroProposteTV = itemView.findViewById(R.id.numeroProposteTV);
            this.visualizzaLocationButton = itemView.findViewById(R.id.visualizzaLocationButton);
            this.visualizzaProposteButton = itemView.findViewById(R.id.visualizzaProposteButton);
            this.onLocationActionsListener = onLocationActionsListener;

            visualizzaLocationButton.setOnClickListener(this);
            visualizzaProposteButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.visualizzaLocationButton) {
                onLocationActionsListener.onOpenLocation(getAdapterPosition());
            } else if (v.getId() == R.id.visualizzaProposteButton) {
                onLocationActionsListener.onOpenSuggestions(getAdapterPosition());
            }
        }
    }
}
