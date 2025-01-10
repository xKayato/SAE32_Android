package fr.unice.jugementday.service;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
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

import java.io.File;
import java.util.HashMap;
import java.util.List;

import fr.unice.jugementday.CheckJudgementActivity;
import fr.unice.jugementday.JudgementActivity;
import fr.unice.jugementday.ListItem;
import fr.unice.jugementday.R;

public class CustomArrayAdapter extends ArrayAdapter<ListItem> {
    private final String login;
    private final JsonStock jsonStock;

    public CustomArrayAdapter(Context context, List<ListItem> items, String login) {
        super(context, 0, items);
        this.login = login;
        this.jsonStock = new JsonStock(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_items_home, parent, false);
        }

        ListItem item = getItem(position);
        TextView titleTextView = convertView.findViewById(R.id.titleTextView);
        LinearLayout buttonContainer = convertView.findViewById(R.id.buttonContainer);

        if (item != null) {
            titleTextView.setText(item.getTitle());
        }

        buttonContainer.removeAllViews(); // Nettoyer les anciens boutons

        // Obtenir les images associées aux œuvres

        // Ajouter les boutons dynamiquement
        for (HashMap<String, Integer> works : item.getWorks()) {
            for (String key : works.keySet()) {
                int idOeuvre = works.get(key);
                createAndAddButton(buttonContainer, idOeuvre, key);
            }
        }

        return convertView;
    }


    private void createAndAddButton(LinearLayout container, int idOeuvre, String title) {
        // Créer un nouveau bouton
        Button photoButton = new Button(getContext());
        photoButton.setGravity(Gravity.CENTER);

        // Vérifier si une image est disponible dans le cache
        Bitmap bitmap = getImageFromCache(idOeuvre);
        if (bitmap != null) {
            // Si l'image est présente dans le cache, l'utiliser
            photoButton.setBackground(new BitmapDrawable(getContext().getResources(), bitmap));
        } else {
            // Définir un style par défaut si aucune image n'est trouvée
            photoButton.setBackgroundColor(Color.GRAY);
        }

        // Appliquer du texte avec un fond et un texte coloré
        Spannable text = new SpannableString(title);
        text.setSpan(new ForegroundColorSpan(Color.WHITE), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new BackgroundColorSpan(Color.BLACK), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        photoButton.setText(text);
        photoButton.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL);
        photoButton.setTextColor(getContext().getResources().getColor(R.color.white));

        // Configurer les dimensions et le placement du bouton
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(300, 500);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        photoButton.setLayoutParams(params);

        // Ajouter un clic listener pour démarrer l'activité correspondante
        photoButton.setOnClickListener(v -> {
            Intent intent = getActivityIntent(idOeuvre, title);
            if (intent != null) {
                getContext().startActivity(intent);
            }
        });

        // Ajouter le bouton au conteneur
        container.addView(photoButton);
    }

    private Bitmap getImageFromCache(int idOeuvre) {
        // Accéder au répertoire images dans le cache
        File cacheDir = new File(getContext().getCacheDir(), "images");
        File imageFile = new File(cacheDir, idOeuvre + ".png");

        if (imageFile.exists()) {
            // Si le fichier d'image existe, charger l'image en tant que Bitmap
            return BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        }

        return null; // Retourner null si l'image n'existe pas
    }



    private Intent getActivityIntent(int idOeuvre, String title) {
        Intent intent;
        String currentActivity = getContext().getClass().getSimpleName();

        // Déterminer l'activité à lancer
        switch (currentActivity) {
            case "ProfileActivity":
            case "CheckProfileActivity":
                intent = new Intent(getContext(), CheckJudgementActivity.class);
                break;
            case "HomeActivity":
            default:
                intent = new Intent(getContext(), JudgementActivity.class);
                break;
        }

        intent.putExtra("idOeuvre", idOeuvre);
        intent.putExtra("title", title);

        if (login != null) {
            intent.putExtra("login", login);
        }

        return intent;
    }
}
