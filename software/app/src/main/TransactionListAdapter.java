package com.christo.moneyplant.helpers;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.christo.moneyplant.R;
import com.christo.moneyplant.models.transaction.TransactionInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class TransactionListAdapter extends RecyclerView.Adapter<TransactionListAdapter.ViewHolder> {

    private ArrayList<String> uids;
    private HashMap<String, TransactionInfo> data;

    public TransactionListAdapter (HashMap<String, TransactionInfo> data) {
        this.data = data;
        this.uids = new ArrayList<>(data.keySet());
    }

    public TransactionListAdapter () {
        this.data = new HashMap<>();
        this.uids = new ArrayList<>();
    }


    @NonNull
    @Override
    public TransactionListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem =  layoutInflater.inflate(R.layout.transaction_list_view, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionListAdapter.ViewHolder holder, int position) {
        final TransactionInfo item = data.get(uids.get(position));
//      // Set the waste icon
        switch (item.waste_type) {
            case "plastic" :
                holder.wasteIcon.setImageResource(R.drawable.plastic_icon);
                break;
            case "metal":
                holder.wasteIcon.setImageResource(R.drawable.metal_icon);
                break;
            case  "organic" :
                holder.wasteIcon.setImageResource(R.drawable.organic_icon);
                break;
            default:

        }
        String wasteType = item.getWaste_type().substring(0,1).toUpperCase() + item.getWaste_type().substring(1).toLowerCase();
        holder.weight.setText(String.format("%s %.2f %s",holder.resources.getText(R.string.weight), item.getWeight()*100, "grams"));
        holder.wasteType.setText(String.format("%s %s", wasteType, holder.resources.getText(R.string.type_waste)));
        holder.uid.setText(String.format("%s %s", holder.resources.getText(R.string.id), item.getUid()));
        holder.credits.setText(String.format("%.2f", item.getCredits()));
    }

    @Override
    public int getItemCount() {
        return data.size();

    }

    public void addItem (TransactionInfo item) {
        if (data.getOrDefault(item.uid, null) == null){
            uids.add(0, item.uid);
            data.put(item.uid, item);
            this.notifyItemInserted(0);
        }
    }

    public static  class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView wasteIcon;
        public TextView wasteType;
        public TextView uid;
        public TextView credits;
        public TextView weight;
        public Resources resources;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.getResources();
            this.resources = (Resources) itemView.getResources();
            this.credits = (TextView) itemView.findViewById(R.id.transaction_list_view_credits);
            this.uid = (TextView) itemView.findViewById(R.id.transaction_list_view_uid);
            this.wasteType = (TextView) itemView.findViewById(R.id.transaction_list_view_title);
            this.weight = (TextView) itemView.findViewById(R.id.transaction_list_view_weight);
            this.wasteIcon = (ImageView) itemView.findViewById(R.id.transaction_list_item_icon);
        }
    }
}
