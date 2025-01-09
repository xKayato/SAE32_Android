package fr.unice.jugementday.service;// HomeActivity.java
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import fr.unice.jugementday.CheckJudgementActivity;
import fr.unice.jugementday.JudgementActivity;
import fr.unice.jugementday.ListItem;
import fr.unice.jugementday.R;

public class CustomArrayAdapter extends ArrayAdapter<ListItem> {
    private final String login;
    private Intent i;

    public CustomArrayAdapter(Context context, List<ListItem> items, String login) {
        super(context, 0, items);
        this.login = login;
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
        for (HashMap<String, Integer> Works : item.getWorks()) {
            Button photoButton = new Button(getContext());

            switch (getContext().getClass().getSimpleName()) {
                case "ProfileActivity":
                    i = new Intent(getContext(), JudgementActivity.class);
                    break;
                case "HomeActivity":
                    i = new Intent(getContext(), JudgementActivity.class);
                    break;
                case "CheckProfileActivity":
                    i = new Intent(getContext(), CheckJudgementActivity.class);
                    break;
                default:
                    i = new Intent(getContext(), JudgementActivity.class);
                    break;
            }
            for (String key : Works.keySet()) {
                i.putExtra("idOeuvre", Works.get(key));
                photoButton.setBackgroundResource(R.drawable.chainsawman);
                Spannable text = new SpannableString(key);
                text.setSpan(new ForegroundColorSpan(Color.WHITE), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                text.setSpan(new BackgroundColorSpan(Color.BLACK), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                photoButton.setText(text);
                photoButton.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL);

                photoButton.setTextColor(getContext().getResources().getColor(R.color.white));
                break;
            }
            photoButton.setOnClickListener(v -> {
                for (String key : Works.keySet()) {
                    i.putExtra("title", key);
                    if (login != null) {
                        i.putExtra("login", login);
                    }
                    break;
                }
                getContext().startActivity(i);
            });
            buttonContainer.addView(photoButton);
        }

        return convertView;
    }
}
