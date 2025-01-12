package fr.unice.jugementday;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import fr.unice.jugementday.service.ListItem;
import fr.unice.jugementday.service.MenuButtons;
import fr.unice.jugementday.service.CustomArrayAdapter;
import fr.unice.jugementday.service.UrlReader;
import fr.unice.jugementday.service.UserSessionManager;

public class CheckProfileActivity extends AppCompatActivity {

    private CustomArrayAdapter adapter;
    private final List<ListItem> items = new ArrayList<>(); // Liste pour contenir les éléments à afficher

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);  // Permet l'affichage complet (plein écran)
        setContentView(R.layout.activity_check_profile);  // Charge le layout

        // Vérification de la session utilisateur
        checkUserSession();

        // Initialisation des éléments de l'interface utilisateur
        TextView pseudo = findViewById(R.id.AccountOfText);
        ListView listView = findViewById(R.id.allJudgementList);

        // Récupération des informations de l'intent
        Intent intent = getIntent();
        String username = intent.getStringExtra("login");

        // Configuration de l'adaptateur pour la ListView
        adapter = new CustomArrayAdapter(this, items, username);
        listView.setAdapter(adapter);

        // Mise à jour du texte pour afficher le pseudo de l'utilisateur
        pseudo.setText(getString(R.string.accountCommuText, username));

        setupMenuButtons();

        // Récupération des données à partir de l'URL
        String options = "&table=Avis&fields=nomOeuvre,idOeuvre&login=" + username;
        fetchDataFromUrl(options);

        // Gestion des fenêtres et des barres système
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Vérifie si l'utilisateur est connecté, sinon le redirige vers la page de connexion.
     */
    private void checkUserSession() {
        UserSessionManager sessionManager = new UserSessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
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
    }

    /**
     * Récupère les données depuis l'URL et met à jour la liste.
     */
    private void fetchDataFromUrl(String url) {
        new Thread(() -> {
            String result = new UrlReader().fetchData(url);

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
     * Analyse les données JSON et met à jour la liste d'affichage.
     */
    private void parseAndUpdateData(String jsonData) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);

            // Récupération des types dynamiques depuis le serveur
            List<String> typesDisponibles = fetchTypesFromServer();

            // Map pour stocker les œuvres par type
            Map<String, List<HashMap<String, Integer>>> oeuvresParType = new HashMap<>();
            for (String type : typesDisponibles) {
                oeuvresParType.put(type, new ArrayList<>());
            }

            // Listes pour les œuvres (toutes et aléatoires)
            List<HashMap<String, Integer>> oeuvresList = new ArrayList<>();
            List<HashMap<String, Integer>> randomOeuvresList = new ArrayList<>();
            List<Integer> selectedIndices = new ArrayList<>();

            // Parsing des œuvres
            for (int i = jsonArray.length() - 1; i >= 0; i--) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String nomOeuvre = jsonObject.getString("nomOeuvre");
                Integer idOeuvre = jsonObject.getInt("idOeuvre");
                String type = jsonObject.getString("type");
                HashMap<String, Integer> oeuvreMap = createHashMap(nomOeuvre, idOeuvre);

                // Ajout dans la liste correspondante selon le type
                if (oeuvresParType.containsKey(type)) {
                    Objects.requireNonNull(oeuvresParType.get(type)).add(oeuvreMap);
                }
                oeuvresList.add(oeuvreMap);
            }

            // Sélection de 10 œuvres aléatoires
            while (randomOeuvresList.size() < 10 && selectedIndices.size() < oeuvresList.size()) {
                int randomIndex = (int) (Math.random() * oeuvresList.size());
                if (!selectedIndices.contains(randomIndex)) {
                    selectedIndices.add(randomIndex);
                    randomOeuvresList.add(oeuvresList.get(randomIndex));
                }
            }

            // Mise à jour des sections d'affichage
            List<ListItem> updatedItems = new ArrayList<>();
            if (!oeuvresList.isEmpty()) {
                updatedItems.add(new ListItem(getString(R.string.lastRealeseText) + " (" + oeuvresList.size() + ")", oeuvresList));
            }
            for (String type : typesDisponibles) {
                List<HashMap<String, Integer>> typeList = oeuvresParType.get(type);
                if (!typeList.isEmpty()) {
                    updatedItems.add(new ListItem(String.format(getString(R.string.XCommuText), type.toLowerCase()) + " (" + typeList.size() + ")", typeList));
                }
            }

            // Ajout des œuvres aléatoires
            updatedItems.add(new ListItem(getString(R.string.randomOeuvresText), randomOeuvresList));

            // Mise à jour de l'adaptateur
            items.clear();
            items.addAll(updatedItems);
            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Récupère la liste des types d'œuvres depuis le serveur.
     */
    private List<String> fetchTypesFromServer() {
        List<String> types = new ArrayList<>();
        try {
            UrlReader urlReader = new UrlReader();
            String result = urlReader.fetchData("&table=Type");
            if (!(result.contains("Erreur") || result.contains("Aucune"))) {
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    types.add(jsonObject.getString("nomType"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return types;
    }

    /**
     * Crée un HashMap avec le nom de l'œuvre et son ID.
     */
    private HashMap<String, Integer> createHashMap(String key, Integer value) {
        HashMap<String, Integer> map = new HashMap<>();
        map.put(key, value);
        return map;
    }
}
