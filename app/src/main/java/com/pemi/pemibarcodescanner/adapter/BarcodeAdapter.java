package com.pemi.pemibarcodescanner.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.pemi.pemibarcodescanner.R;
import com.pemi.pemibarcodescanner.model.Barcode;

import java.util.ArrayList;

public class BarcodeAdapter extends RecyclerView.Adapter<BarcodeAdapter.viewHolder>{

    private LayoutInflater layoutInflater;
    private Context context;
    private ArrayList<Barcode> barcodes;

    public BarcodeAdapter(Context context, ArrayList<Barcode> barcodes) {
        this.context = context;
        this.barcodes = barcodes;
    }

    public void setAdapter(ArrayList<Barcode> barcodes) {
        this.barcodes = barcodes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        layoutInflater = LayoutInflater.from(parent.getContext());
        View view =layoutInflater.inflate(R.layout.item_list_barcode, parent, false);
        final BarcodeAdapter.viewHolder viewHolder = new BarcodeAdapter.viewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, final int position) {
        holder.tvType.setText(barcodes.get(position).getType());
        holder.tvValue.setText(barcodes.get(position).getValue());
        holder.tvDate.setText(barcodes.get(position).getCreatedAt());

    }

    @Override
    public int getItemCount() {
        return barcodes.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView  tvType, tvValue, tvDate;
        public viewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.card_view_P);
            tvType = itemView.findViewById(R.id.tv_type);
            tvValue = itemView.findViewById(R.id.tv_value);
            tvDate = itemView.findViewById(R.id.tv_date);
        }
    }
}
