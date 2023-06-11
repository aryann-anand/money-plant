package com.christo.moneyplant.helpers;

import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.christo.moneyplant.R;
import com.christo.moneyplant.models.transaction.TransactionInfo;

import java.text.SimpleDateFormat;

public class SortedTransactionListAdapter extends RecyclerView.Adapter<SortedTransactionListAdapter.ViewHolder> {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd HH:mm:ss yyy");
    SortedList<TransactionInfo> sortedTransactionList = new SortedList<TransactionInfo> (TransactionInfo.class, new SortedList.Callback<TransactionInfo>() {

        @Override
        public void onInserted(int position, int count) {
            notifyItemRangeChanged(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public int compare(TransactionInfo o1, TransactionInfo o2) {
            return o1.getTimestamp().compareTo(o2.getTimestamp());
        }

        @Override
        public void onChanged(int position, int count) {
            notifyItemRangeChanged(position, count);
        }

        @Override
        public boolean areContentsTheSame(TransactionInfo oldItem, TransactionInfo newItem) {
            return oldItem.getUid() == newItem.getUid();
        }

        @Override
        public boolean areItemsTheSame(TransactionInfo item1, TransactionInfo item2) {
            return item1.getUid() == item2.getUid();
        }
    });


    @NonNull
    @Override
    public SortedTransactionListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem =  layoutInflater.inflate(R.layout.transaction_list_view, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull SortedTransactionListAdapter.ViewHolder holder, int position) {
        final TransactionInfo item = sortedTransactionList.get(position);
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
        Log.e("trnascti lis", "onBindViewHolder: "+item.getTimestamp().getTime());
        holder.date.setText(dateFormat.format(item.timestamp).toString());
    }

    @Override
    public int getItemCount() {
        return sortedTransactionList.size();

    }

    public void addItem (TransactionInfo item) {
        sortedTransactionList.add(item);
    }

    public static  class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView wasteIcon;
        public TextView wasteType;
        public TextView uid;
        public TextView credits;
        public TextView weight;
        public TextView date;
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
            this.date = (TextView) itemView.findViewById((R.id.transaction_list_date));
        }
    }
}
