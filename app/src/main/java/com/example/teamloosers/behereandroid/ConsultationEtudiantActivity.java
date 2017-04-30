package com.example.teamloosers.behereandroid;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterViewFlipper;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.teamloosers.behereandroid.Structures.Etudiant;
import com.example.teamloosers.behereandroid.Structures.Personne;

import java.util.ArrayList;

public class ConsultationEtudiantActivity extends AppCompatActivity {

    private AdapterViewFlipper etudiantsAdapterViewFlipper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultation_etudiant);

        etudiantsAdapterViewFlipper = (AdapterViewFlipper) findViewById(R.id.etudiantsAdapterViewFlipper);

        ArrayList<Etudiant> etudiantsList = new ArrayList<>();

        EtudiantsConsultationAdapter etudiantsArrayAdapter = new EtudiantsConsultationAdapter(this,
                etudiantsList);

        etudiantsAdapterViewFlipper.setAdapter(etudiantsArrayAdapter);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // TODO ajouter les animation de gestures
        return false;
    }

    public class EtudiantsConsultationAdapter extends BaseAdapter   {

        private Context context;
        private ArrayList<Etudiant> etudiantsList;
        private LayoutInflater inflater;

        public EtudiantsConsultationAdapter(Context context, ArrayList<Etudiant> etudiantsList) {

            this.context = context;
            this.etudiantsList = etudiantsList;

            inflater = (LayoutInflater.from(this.context));
        }

        @Override
        public int getCount() {
            return etudiantsList.size();
        }

        @Override
        public Object getItem(int position) {
            return etudiantsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            convertView = inflater.inflate(R.layout.etudiant_consultation_view, null);

            TextView nomTextView = (TextView) convertView.findViewById(R.id.nomTextView);
            TextView prenomTextView = (TextView) convertView.findViewById(R.id.prenomTextView);

            Etudiant etudiant = (Etudiant) getItem(position);

            nomTextView.setText(etudiant.getNom());
            prenomTextView.setText(etudiant.getPrenom());

            return convertView;
        }
    }
}
