package fr.unice.jugementday;// HomeActivity.java
import android.app.LauncherActivity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class CustomArrayAdapter extends ArrayAdapter<ListItem> {
    public CustomArrayAdapter(Context context, List<ListItem> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_items_home, parent, false);
        }

        ListItem item = getItem(position);
        TextView titleTextView = convertView.findViewById(R.id.titleTextView);
        titleTextView.setText(item.getTitle());

        LinearLayout buttonContainer = convertView.findViewById(R.id.buttonContainer);
        buttonContainer.removeAllViews();

        // Ajouter les boutons dynamiquement
        for (int photoId : item.getPhotoIds()) {
            Button photoButton = new Button(getContext());
            photoButton.setBackgroundResource(photoId);
            photoButton.setOnClickListener(v -> {
                // Logique pour gérer le clic
            });
            buttonContainer.addView(photoButton);
        }

        return convertView;
    }
}
