package com.example.teamloosers.behereandroid;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;

/**
 * Created by teamloosers on 01/05/17.
 */

public class ItemViewHolder extends RecyclerView.ViewHolder {

    public ItemViewHolder(View itemView) {

        super(itemView);
    }

    public void bind(final View itemView, final AdapterView.OnItemClickListener listener)   {

        final int position = getAdapterPosition();

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                listener.onItemClick(null, v, position, 0);
            }
        });
    }
}
