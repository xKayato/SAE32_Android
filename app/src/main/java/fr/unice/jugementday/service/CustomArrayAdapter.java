package fr.unice.jugementday.service;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import fr.unice.jugementday.CheckJudgementActivity;
import fr.unice.jugementday.JudgementActivity;
import fr.unice.jugementday.ListItem;
import fr.unice.jugementday.R;

public class CustomArrayAdapter extends ArrayAdapter<ListItem> {
    private final String login;
    private Intent i;
    private JsonStock jsonStock = new JsonStock(getContext());
    private String WorksT = jsonStock.getWorks();  // Assurez-vous que WorksT contient un JSON valide.

    public CustomArrayAdapter(Context context, List<ListItem> items, String login) {
        super(context, 0, items);
        this.login = login;
    }

    private String getColorForCharacter(char c) {
        // Utiliser l'index ASCII du caractère, mais de manière plus diversifiée.
        int value = (int) c;
        value = value % 256; // Limiter à la plage 0-255 pour chaque composant de couleur

        // Appliquer une plus grande variation à chaque composant pour élargir la palette.
        int red = (value + 50) % 256;
        int green = (value + 100) % 256;
        int blue = (value + 10) % 256;

        // Retourner la couleur au format hexadécimal
        String color = String.format("#%02X%02X%02X", red, green, blue);
        Log.d("ColorGenerated", "Generated color: " + color);
        return color;
    }


    private boolean isValidColor(String color) {
        try {
            Color.parseColor(color);  // Tester si la couleur est valide
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String getTypeColor(String type) {
        int red = 0, green = 0, blue = 0;

        // Parcourir chaque caractère du type pour calculer les valeurs RGB
        for (char c : type.toCharArray()) {
            int value = (int) c;
            red += (value+50) % 256;   // Appliquer une variation basée sur la valeur ASCII du caractère
            green += (value + 100) % 256;  // Variation supplémentaire pour le vert
            blue += (value + 180) % 256;  // Variation supplémentaire pour le bleu
        }

        // Normaliser les composantes de couleur pour qu'elles restent dans la plage 0-255
        red = red % 256;
        green = green % 256;
        blue = blue % 256;

        // Construire la couleur hexadécimale
        String color = String.format("#%02X%02X%02X", red, green, blue);

        return isValidColor(color) ? color : "#FFFFFF"; // Retourner la couleur si valide, sinon blanc
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
        for (HashMap<String, Integer> works : item.getWorks()) {
            Button photoButton = new Button(getContext());

            switch (getContext().getClass().getSimpleName()) {
                case "ProfileActivity":
                    i = new Intent(getContext(), CheckJudgementActivity.class);
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

            // Parcourir les œuvres associées
            for (String key : works.keySet()) {
                i.putExtra("idOeuvre", works.get(key));

                // Essayer de convertir WorksT en JSONArray
                try {
                    // Parsez WorksT comme un tableau JSON
                    JSONArray jsonArray = new JSONArray(WorksT);
                    // Assurez-vous que le tableau contient des éléments
                    for (int j = 0; j < jsonArray.length(); j++) {
                        // Récupérer l'élément JSON de l'index j
                        JSONObject jsonObject = jsonArray.getJSONObject(j);

                        // Vérifiez si l'ID de l'œuvre correspond à la valeur de works.get(key)
                        if (jsonObject.getInt("idOeuvre") == works.get(key)) {
                            // Si correspondance, récupérer le type
                            String type = jsonObject.getString("type");

                            // Appliquer la couleur en fonction du type
                            String color = getTypeColor(type);

                            // Appliquer la couleur au bouton
                            photoButton.setBackgroundColor(Color.parseColor(color));
                            break;  // Sortir de la boucle une fois que l'œuvre correspondante est trouvée
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace(); // Gestion des erreurs JSON
                }

                // Appliquer le texte et la couleur au bouton
                Spannable text = new SpannableString(key);
                text.setSpan(new ForegroundColorSpan(Color.WHITE), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                text.setSpan(new BackgroundColorSpan(Color.BLACK), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                photoButton.setLayoutParams(new LinearLayout.LayoutParams(300, 500));
                photoButton.setText(text);
                photoButton.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL);
                photoButton.setTextColor(getContext().getResources().getColor(R.color.white));

                break; // Sortir après avoir traité un seul élément
            }

            // Gérer le clic sur le bouton pour démarrer l'activité
            photoButton.setOnClickListener(v -> {
                for (String key : works.keySet()) {
                    i.putExtra("title", key);
                    if (login != null) {
                        i.putExtra("login", login);
                    }
                }
                getContext().startActivity(i);
            });

            buttonContainer.addView(photoButton); // Ajouter le bouton à la vue
        }

        return convertView;
    }
}
