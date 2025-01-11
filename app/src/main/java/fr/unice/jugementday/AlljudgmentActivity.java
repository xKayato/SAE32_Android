package fr.unice.jugementday;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.text.HtmlCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fr.unice.jugementday.service.MenuButtons;
import fr.unice.jugementday.service.SearchCustomArrayAdapter;
import fr.unice.jugementday.service.UrlReader;

public class AlljudgmentActivity extends AppCompatActivity {

    private ArrayAdapter<String> myAdapter;
    private final ArrayList<String> items = new ArrayList<>();
    private final List<String> oeuvresList = new ArrayList<>();
    private float notes = 0;
    private int nb = 0;
    private TextView noteText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alljudgment);

        TextView critiqueText = findViewById(R.id.critiqueText);
        Intent intent = getIntent();
        int id = intent.getIntExtra("idOeuvre", 0);
        String title = getIntent().getStringExtra("title");
        String newTitle = getString(R.string.critiqueOfText, title);
        critiqueText.setText(newTitle);
        noteText = findViewById(R.id.noteText);
        myAdapter = new SearchCustomArrayAdapter(this, items);
        ListView judgementsList = findViewById(R.id.allJudgementList);
        judgementsList.setAdapter(myAdapter);

        // Appel de la méthode pour récupérer les données depuis l'URL
        fetchDataFromUrl("&table=Avis&fields=texteAvis,login,note&idOeuvre=" + id);

        // Gestion des boutons du menu
        ImageButton profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> MenuButtons.profileClick(this));

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> MenuButtons.homeClick(this));

        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> MenuButtons.searchClick(this));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Méthode pour récupérer les données depuis l'URL et mettre à jour la liste
    private void fetchDataFromUrl(String options) {
        new Thread(() -> {
            // Ici, on appelle la méthode pour récupérer les données de l'URL
            String result = new UrlReader().fetchData(options);

            // Mise à jour de l'interface (doit être effectué sur le thread principal)
            runOnUiThread(() -> {
                if (result.startsWith("Erreur")) {
                    Toast.makeText(this, R.string.errorText, Toast.LENGTH_LONG).show();
                } else {
                    parseAndUpdateData(result);
                }
            });
        }).start();
    }

    // Méthode pour analyser les données JSON et mettre à jour la liste
    private void parseAndUpdateData(String jsonData) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String texteAvis = HtmlCompat.fromHtml(jsonObject.getString("texteAvis"), HtmlCompat.FROM_HTML_MODE_LEGACY).toString(); // Pour contrer le htmlspecialchars
                String pseudo = jsonObject.getString("login");
                String note = jsonObject.getString("note");
                notes += Float.parseFloat(note);
                nb += 1;

                float noteTot = notes/nb;

                if (noteTot % 1 == 0) {
                    noteText.setText(String.format(Locale.getDefault(), "%.0f/5", noteTot));
                } else {
                    noteText.setText(String.format(Locale.getDefault(), "%.2f/5", noteTot));
                }

                oeuvresList.add(pseudo + " (" + note + "/5) : " + texteAvis);
            }

            // Mise à jour de la liste des éléments
            items.clear();
            items.addAll(oeuvresList);

            // Mise à jour de l'adaptateur
            myAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.errorText, Toast.LENGTH_LONG).show();
        }
    }
}