package com.christo.moneyplant.helpers;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.christo.moneyplant.R;
import com.christo.moneyplant.models.ui.IconListItem;

import java.util.ArrayList;
import java.util.List;

public class IconListAdapter extends RecyclerView.Adapter<IconListAdapter.ViewHolder> {

    private List<IconListItem> data;

    public IconListAdapter (List<IconListItem> data) {
        this.data = data;
    }

    public IconListAdapter () {
        this.data = new ArrayList<>();
    }

    public void addItemToBottom (IconListItem item) {
        data.add(item);
        this.notifyItemInserted(data.size()-1);
    }
    public void addItemToTop (IconListItem item) {
        data.add(0, item);
        this.notifyItemInserted(0);
    }

    public void updateItem (int index, IconListItem item) {
        data.set(index, item);
        this.notifyItemChanged(index);
    }

    public void clear () {
        data.clear();
        this.notifyDataSetChanged();
    }

    public void remove (int index) {
        data.remove(index);
        this.notifyItemRemoved(index);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem =  layoutInflater.inflate(R.layout.icon_list_view, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final IconListItem item = data.get(position);
        holder.textView.setText(item.getDescription());
        holder.imageView.setImageResource(item.getImgId());
        holder.imageView.setColorFilter(holder.resources.getColor(item.getIconColor(), holder.resources.newTheme()));
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView;
        public RelativeLayout relativeLayout;
        public Resources resources;
        public ViewHolder(View itemView) {
            super(itemView);
            resources = itemView.getResources();
            this.imageView = (ImageView) itemView.findViewById(R.id.icon_list_view__image_view);
            this.textView = (TextView) itemView.findViewById(R.id.icon_list_view__text_view);
            relativeLayout = (RelativeLayout)itemView.findViewById(R.id.icon_list_view__relative_layout);
        }
    }
}
