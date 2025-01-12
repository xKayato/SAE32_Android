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
import fr.unice.jugementday.service.UserSessionManager;

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

        checkUserSession(); // Vérifie la session utilisateur
        setupUI();          // Initialise les éléments de l'interface
        setupMenuButtons(); // Configure les boutons du menu
        fetchData();        // Charge les données depuis le serveur
    }

    /**
     * Vérifie si l'utilisateur est connecté et le redirige si nécessaire.
     */
    private void checkUserSession() {
        UserSessionManager sessionManager = new UserSessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * Initialise les éléments de l'interface utilisateur.
     */
    private void setupUI() {
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");

        TextView critiqueText = findViewById(R.id.critiqueText);
        String newTitle = getString(R.string.critiqueOfText, title);
        critiqueText.setText(newTitle);

        noteText = findViewById(R.id.noteText);

        myAdapter = new SearchCustomArrayAdapter(this, items);
        ListView judgementsList = findViewById(R.id.allJudgementList);
        judgementsList.setAdapter(myAdapter);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void showToast(int messageId) {
        Toast.makeText(this, getString(messageId), Toast.LENGTH_SHORT).show();
    }

    /**
     * Configure les boutons de navigation.
     */
    private void setupMenuButtons() {
        ImageButton profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> MenuButtons.profileClick(this));

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> MenuButtons.homeClick(this));

        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> MenuButtons.searchClick(this));
    }

    /**
     * Charge les données d'avis depuis le serveur.
     */
    private void fetchData() {
        int id = getIntent().getIntExtra("idOeuvre", 0);
        String options = "&table=Avis&fields=texteAvis,login,note&idOeuvre=" + id;

        new Thread(() -> {
            String result = new UrlReader().fetchData(options);
            runOnUiThread(() -> {
                if (result.startsWith("Erreur")) {
                    showToast(R.string.errorText);
                } else {
                    parseAndUpdateData(result);
                }
            });
        }).start();
    }

    /**
     * Analyse les données JSON et met à jour la liste des avis.
     *
     * @param jsonData Données JSON des avis
     */
    private void parseAndUpdateData(String jsonData) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String texteAvis = HtmlCompat.fromHtml(jsonObject.getString("texteAvis"), HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
                String pseudo = jsonObject.getString("login");
                String note = jsonObject.getString("note");

                notes += Float.parseFloat(note);
                nb++;
                updateNoteDisplay();

                oeuvresList.add(String.format("%s (%s/5) : %s", pseudo, note, texteAvis));
            }

            items.clear();
            items.addAll(oeuvresList);
            myAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
            showToast(R.string.errorText);
        }
    }

    /**
     * Met à jour l'affichage de la note moyenne.
     */
    private void updateNoteDisplay() {
        float average = notes / nb;
        if (average % 1 == 0) {
            noteText.setText(String.format(Locale.getDefault(), "%.0f/5", average));
        } else {
            noteText.setText(String.format(Locale.getDefault(), "%.2f/5", average));
        }
    }
}
