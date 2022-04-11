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
import it.studenti.unitn.mazzalai_leoni.sportfinder.items.ReportItem;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private ArrayList<ReportItem> reportList;
    private OnReportActionsListener mOnReportActionsListener;

    public ReportAdapter(ArrayList<ReportItem> reportList, OnReportActionsListener onReportActionsListener) {
        this.reportList = reportList;
        this.mOnReportActionsListener = onReportActionsListener;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_report, parent, false);
        ReportViewHolder rvh = new ReportViewHolder(view, mOnReportActionsListener);
        return rvh;
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        ReportItem currentItem = reportList.get(position);
        holder.reportTV.setText(currentItem.getText());
        holder.timesTV.setText("x" + currentItem.getTimesReported());
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public interface OnReportActionsListener {
        void onMoreActions(int position);
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView reportTV, timesTV;
        ImageView actionIV;
        OnReportActionsListener onReportActionsListener;

        public ReportViewHolder(View itemView, OnReportActionsListener onReportActionsListener) {
            super(itemView);
            this.reportTV = itemView.findViewById(R.id.reportTV);
            this.timesTV = itemView.findViewById(R.id.timesTV);
            this.actionIV = itemView.findViewById(R.id.actionIV);
            this.onReportActionsListener = onReportActionsListener;

            actionIV.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.actionIV) {
                onReportActionsListener.onMoreActions(getAdapterPosition());
            }
        }
    }
}
