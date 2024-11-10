package fr.unice.jugementday;// HomeActivity.java
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomArrayAdapter extends ArrayAdapter<String> {

    public CustomArrayAdapter(Context context, List<String> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_items_home, parent, false);
        }

        TextView textViewItem = convertView.findViewById(R.id.lastReleaseText);
        String item = getItem(position);
        textViewItem.setText(item);

        return convertView;
    }
}