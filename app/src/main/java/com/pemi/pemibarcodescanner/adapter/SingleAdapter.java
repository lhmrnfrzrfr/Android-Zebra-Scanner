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
import java.util.List;

public class SingleAdapter extends RecyclerView.Adapter<SingleAdapter.viewHolder>{

    private LayoutInflater layoutInflater;
    private Context context;
    private List<String> data;

    public SingleAdapter(Context context, List<String> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public SingleAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        layoutInflater = LayoutInflater.from(parent.getContext());
        View view =layoutInflater.inflate(R.layout.item_list_barcode, parent, false);
        final SingleAdapter.viewHolder viewHolder = new SingleAdapter.viewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SingleAdapter.viewHolder holder, int i) {
        holder.tvValue.setText(data.get(i).toString());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvType, tvValue, tvDate;
        public viewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.card_view_P);
            tvType = itemView.findViewById(R.id.tv_type);
            tvValue = itemView.findViewById(R.id.tv_value);
            tvDate = itemView.findViewById(R.id.tv_date);
        }
    }
}
