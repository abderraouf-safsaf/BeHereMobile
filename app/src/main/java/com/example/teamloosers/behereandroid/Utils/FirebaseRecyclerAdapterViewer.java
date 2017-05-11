package com.example.teamloosers.behereandroid.Utils;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.Filterable;

import com.example.teamloosers.behereandroid.R;
import com.example.teamloosers.behereandroid.Structures.Etudiant;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;

import java.util.ArrayList;

/**
 * Created by teamloosers on 30/04/17.
 */

public abstract class FirebaseRecyclerAdapterViewer<T, VH extends ItemViewHolder>
        extends FirebaseRecyclerAdapter<T, VH>{

    private ArrayList<T> deletedItems;

    private AdapterView.OnItemClickListener listener;

    /**
     * @param modelClass      Firebase will marshall the data at a location into
     *                        an instance of a class that you provide
     * @param modelLayout     This is the layout used to represent a single item in the list.
     *                        You will be responsible for populating an instance of the corresponding
     *                        view with the data from an instance of modelClass.
     * @param viewHolderClass The class that hold references to all sub-views in an instance modelLayout.
     * @param ref             The Firebase location to watch for data changes. Can also be a slice of a location,
     *                        using some combination of {@code limit()}, {@code startAt()}, and {@code endAt()}.
     */
    public FirebaseRecyclerAdapterViewer(Class<T> modelClass, int modelLayout, Class<VH> viewHolderClass, Query ref) {

        super(modelClass, modelLayout, viewHolderClass, ref);

    }

    public ArrayList<T> getItems()  {

        ArrayList<T> itemsList = new ArrayList<>();
        for (int i = 0; i < getItemCount(); i++)
            itemsList.add(getItem(i));

        return itemsList;
    }
    @Override
    protected void populateViewHolder(VH viewHolder, T model, int position) {

        if (model != null)
            populateView(viewHolder, model, position);
    }

    @Override
    protected T parseSnapshot(DataSnapshot snapshot) {

        return super.parseSnapshot(snapshot);
    }

    @Override
    public void onBindViewHolder(VH viewHolder, int position) {

        super.onBindViewHolder(viewHolder, position);

        if (viewHolder.visible)
            viewHolder.bind(viewHolder.itemView, listener);
    }


    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        this.listener = listener;
    }

    protected abstract void populateView(VH viewHolder, T model, int position);

}
