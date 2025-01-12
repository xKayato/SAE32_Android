package fr.unice.jugementday;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import fr.unice.jugementday.service.JsonStock;
import fr.unice.jugementday.service.MenuButtons;
import fr.unice.jugementday.service.UrlReader;
import fr.unice.jugementday.service.UserSessionManager;

public class CheckJudgementActivity extends AppCompatActivity {

    private String title;
    private int id;
    private TextView JudgementField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_judgement);
        EdgeToEdge.enable(this);

        checkUserSession();  // Vérifie si l'utilisateur est connecté
        setupUI();           // Initialise les éléments de l'interface utilisateur
        setupMenuButtons();  // Configure les boutons du menu
        fetchData();         // Récupère les données d'avis depuis l'URL
    }

    /**
     * Vérifie si l'utilisateur est connecté et redirige vers la page de connexion si nécessaire.
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
        // Récupère les données de l'intent
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        String userLogin = intent.getStringExtra("login");
        id = intent.getIntExtra("idOeuvre", 0);

        // Initialisation des TextViews
        TextView titleText = findViewById(R.id.titleText);
        titleText.setText(title);

        TextView pseudo = findViewById(R.id.JudgementOfText);
        String formattedText = getString(R.string.critiqueOfText, userLogin);
        pseudo.setText(formattedText);

        // Récupère les informations sur l'œuvre
        JsonStock jsonStock = new JsonStock(this);
        String works = jsonStock.getWorks();
        updateWorkInfo(works);  // Met à jour les informations de l'œuvre

        // Initialisation du champ d'avis
        JudgementField = findViewById(R.id.JudgementField);
    }

    /**
     * Met à jour les informations relatives à l'œuvre (type, auteur, date).
     */
    private void updateWorkInfo(String works) {
        try {
            JSONArray jsonArray = new JSONArray(works);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int idOeuvre = jsonObject.getInt("idOeuvre");
                if (idOeuvre == id) {
                    // Mise à jour des TextViews avec les informations de l'œuvre
                    TextView typeText = findViewById(R.id.typeText);
                    typeText.setText(String.format("Genre : %s", capitalizeFirstLetter(jsonObject.getString("type"))));

                    TextView auteurText = findViewById(R.id.auteurText);
                    auteurText.setText(String.format("Auteur/Studio : %s", capitalizeFirstLetter(jsonObject.getString("auteur_studio"))));

                    TextView dateText = findViewById(R.id.dateText);
                    dateText.setText(String.format("Date : %s", jsonObject.getString("dateSortie")));
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Capitalise la première lettre de chaque mot de la chaîne donnée.
     */
    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    private void showToast(int messageId) {
        Toast.makeText(this, getString(messageId), Toast.LENGTH_SHORT).show();
    }

    /**
     * Configure les boutons de navigation du menu.
     */
    private void setupMenuButtons() {
        ImageButton profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> MenuButtons.profileClick(this));

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> MenuButtons.homeClick(this));

        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> MenuButtons.searchClick(this));

        ImageButton community = findViewById(R.id.communityButton);
        community.setOnClickListener(this::onClickAllJudgement);
    }

    /**
     * Gère l'événement de clic sur le bouton pour afficher tous les avis.
     */
    public void onClickAllJudgement(View view) {
        Intent intent2 = new Intent(this, AlljudgmentActivity.class);
        intent2.putExtra("title", title);
        intent2.putExtra("idOeuvre", id);
        startActivity(intent2);
    }

    /**
     * Méthode pour récupérer les données depuis l'URL et mettre à jour l'affichage.
     */
    private void fetchData() {
        String options = "&table=Avis&idOeuvre=" + id + "&login=" + getIntent().getStringExtra("login");
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
     * Analyse les données JSON et met à jour le champ d'avis avec le texte et la note.
     *
     * @param jsonData Les données JSON des avis.
     */
    private void parseAndUpdateData(String jsonData) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            JSONObject jsonObject = jsonArray.getJSONObject(0); // Prend le premier avis
            String texteAvis = HtmlCompat.fromHtml(jsonObject.getString("texteAvis"), HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
            String note = jsonObject.getString("note");

            // Mise à jour du champ d'avis et de la note
            JudgementField.setText(texteAvis);
            TextView noteText = findViewById(R.id.noteText);
            noteText.setText(String.format("%s/5", note));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
