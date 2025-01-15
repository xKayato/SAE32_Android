package fr.unice.jugementday.service;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class SearchCustomArrayAdapter extends ArrayAdapter<String> {

    public SearchCustomArrayAdapter(Context context, List<String> items) {
        super(context, android.R.layout.simple_list_item_1, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        // Obtenir la vue du TextView pour l'élément
        TextView textView = view.findViewById(android.R.id.text1);

        // Définir la couleur du texte ici
        textView.setText(getItem(position));

        textView.setTextColor(Color.WHITE);


        return view;
    }
}
