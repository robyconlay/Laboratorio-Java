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
import it.studenti.unitn.mazzalai_leoni.sportfinder.items.SportRequestItem;

public class SportRequestAdapter extends RecyclerView.Adapter<SportRequestAdapter.SportRequestViewHolder> {

    private ArrayList<SportRequestItem> sportRequestList;
    private OnCheckListener mOnCheckListener;

    public SportRequestAdapter(ArrayList<SportRequestItem> sportRequestList, OnCheckListener onCheckListener) {
        this.sportRequestList = sportRequestList;
        this.mOnCheckListener = onCheckListener;
    }

    @NonNull
    @Override
    public SportRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_sport_request, parent, false);
        SportRequestViewHolder srvh = new SportRequestViewHolder(view, mOnCheckListener);
        return srvh;
    }

    @Override
    public void onBindViewHolder(@NonNull SportRequestViewHolder holder, int position) {
        SportRequestItem currentItem = sportRequestList.get(position);
        holder.sportTV.setText(currentItem.getSport());
//        holder.userTV.setText(currentItem.getUser());
    }

    @Override
    public int getItemCount() {
        return sportRequestList.size();
    }

    public interface OnCheckListener {
        void onAcceptClick(int position);

        void onRejectClick(int position);

//        void onMoreActions(int position);
    }

    public static class SportRequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView sportTV, userTV;
        ImageView rifiutaIV, accettaIV, moreIV;
        OnCheckListener onCheckListener;

        public SportRequestViewHolder(View itemView, OnCheckListener onCheckListener) {
            super(itemView);
            this.sportTV = itemView.findViewById(R.id.sportTV);
//            this.userTV = itemView.findViewById(R.id.userTV);
            this.rifiutaIV = itemView.findViewById(R.id.rifiutaIV);
            this.accettaIV = itemView.findViewById(R.id.accettaIV);
//            this.moreIV = itemView.findViewById(R.id.moreIV);
            this.onCheckListener = onCheckListener;

            accettaIV.setOnClickListener(this);
            rifiutaIV.setOnClickListener(this);
//            moreIV.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.accettaIV:
                    onCheckListener.onAcceptClick(getAdapterPosition());
                    break;
                case R.id.rifiutaIV:
                    onCheckListener.onRejectClick(getAdapterPosition());
                    break;
//                case R.id.moreIV:
//                    onCheckListener.onMoreActions(getAdapterPosition());
//                    break;
                default:
                    break;
            }
        }
    }
}
