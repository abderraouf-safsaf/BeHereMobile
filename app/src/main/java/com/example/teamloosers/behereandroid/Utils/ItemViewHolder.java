package com.example.teamloosers.behereandroid.Utils;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;

/**
 * Created by teamloosers on 01/05/17.
 */

public class ItemViewHolder extends RecyclerView.ViewHolder {

    public Boolean visible = true;

    public ItemViewHolder(View itemView) {

        super(itemView);
    }

    public void bind(final View itemView, final AdapterView.OnItemClickListener listener)   {

        final int position = getAdapterPosition();

        if (itemView != null)
            itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (listener != null)
                    listener.onItemClick(null, v, position, 0);
            }
        });
    }
}
