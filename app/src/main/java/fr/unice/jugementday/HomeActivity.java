package fr.unice.jugementday;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
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

import fr.unice.jugementday.service.ListItem;
import fr.unice.jugementday.service.MenuButtons;
import fr.unice.jugementday.service.CustomArrayAdapter;
import fr.unice.jugementday.service.UrlReader;
import fr.unice.jugementday.service.JsonStock;
import fr.unice.jugementday.service.UserSessionManager;

public class HomeActivity extends AppCompatActivity {

    private CustomArrayAdapter adapter;
    private final List<ListItem> items = new ArrayList<>();
    private String login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);  // Active les bords d'écran pour une expérience visuelle optimisée
        setContentView(R.layout.activity_home);

        // Initialisation des services nécessaires
        JsonStock jsonStock = new JsonStock(this);
        UserSessionManager sessionManager = new UserSessionManager(this);

        // Vérifier si l'utilisateur est connecté, sinon rediriger vers la page de connexion
        checkUserSession(sessionManager);


        // Configuration de l'interface utilisateur
        setupUI();

        // Chargement et parsing des données d'œuvres
        String works = jsonStock.getWorks();
        parseAndUpdateData(works);

        // Configuration des boutons de navigation
        setupMenuButtons();
    }

    /**
     * Méthode pour vérifier si l'utilisateur est connecté.
     * Si l'utilisateur n'est pas connecté, il est redirigé vers la page de connexion.
     */
    private void checkUserSession(UserSessionManager sessionManager) {
        if (!sessionManager.isLoggedIn()) {
            // Si l'utilisateur n'est pas connecté, rediriger vers la page de connexion
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else{
            login = sessionManager.getLogin();
        }
    }



    /**
     * Configure les composants de l'interface utilisateur pour l'activité Home.
     */
    private void setupUI() {
        // Initialisation de la ListView et de l'adaptateur
        ListView listView = findViewById(R.id.allJudgementList);
        adapter = new CustomArrayAdapter(this, items, login);
        listView.setAdapter(adapter);

        // Initialisation du bouton d'ajout d'œuvre
        Button addWorkButton = findViewById(R.id.AddWorkButton);
        addWorkButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddWorkActivity.class);
            startActivity(intent);
        });

        // Application des paddings sur la vue principale pour gérer les fenêtres système
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Configure les boutons de navigation (profil, accueil, recherche).
     */
    private void setupMenuButtons() {
        // Bouton Profil
        ImageButton profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> MenuButtons.profileClick(this));

        // Bouton Recherche
        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> MenuButtons.searchClick(this));
    }

    /**
     * Méthode pour analyser les données JSON récupérées et mettre à jour l'interface.
     * @param jsonData Données JSON contenant les informations des œuvres.
     */
    private void parseAndUpdateData(String jsonData) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);

            // Récupération des types d'œuvres disponibles depuis le serveur
            List<String> typesDisponibles = fetchTypesFromServer();

            // Map pour organiser les œuvres par type
            Map<String, List<HashMap<String, Integer>>> oeuvresParType = new HashMap<>();
            for (String type : typesDisponibles) {
                oeuvresParType.put(type, new ArrayList<>());
            }

            // Listes pour organiser les œuvres et sélectionner les œuvres aléatoires
            List<HashMap<String, Integer>> oeuvresList = new ArrayList<>();
            List<HashMap<String, Integer>> randomOeuvresList = new ArrayList<>();
            List<Integer> selectedIndices = new ArrayList<>();

            // Parcours des œuvres et ajout dans les catégories correspondantes
            for (int i = jsonArray.length() - 1; i >= 0; i--) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String nomOeuvre = jsonObject.getString("nomOeuvre");
                Integer idOeuvre = jsonObject.getInt("idOeuvre");
                String type = jsonObject.getString("type");
                HashMap<String, Integer> oeuvreMap = createHashMap(nomOeuvre, idOeuvre);

                // Ajout de l'œuvre dans la liste du type approprié
                if (oeuvresParType.containsKey(type)) {
                    oeuvresParType.get(type).add(oeuvreMap);
                }
                oeuvresList.add(oeuvreMap);
            }

            // Sélection aléatoire de 20 œuvres si disponible
            while (randomOeuvresList.size() < 20 && selectedIndices.size() < oeuvresList.size()) {
                int randomIndex = (int) (Math.random() * oeuvresList.size());
                if (!selectedIndices.contains(randomIndex)) {
                    selectedIndices.add(randomIndex);
                    randomOeuvresList.add(oeuvresList.get(randomIndex));
                }
            }

            // Mise à jour de l'adaptateur avec les nouvelles données
            updateListView(oeuvresList, typesDisponibles, oeuvresParType, randomOeuvresList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Récupère les types d'œuvres disponibles depuis le serveur.
     * @return Une liste de types d'œuvres.
     */
    private List<String> fetchTypesFromServer() {
        List<String> types = new ArrayList<>();
        try {
            UrlReader urlReader = new UrlReader();
            String result = urlReader.fetchData("&table=Type");

            // Si la réponse ne contient pas d'erreur, on parse les types
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
     * Met à jour la ListView avec les nouvelles œuvres et les sections dynamiques.
     * @param oeuvresList Liste des œuvres récupérées.
     * @param typesDisponibles Liste des types disponibles.
     * @param oeuvresParType Map des œuvres par type.
     * @param randomOeuvresList Liste des œuvres sélectionnées aléatoirement.
     */
    private void updateListView(List<HashMap<String, Integer>> oeuvresList, List<String> typesDisponibles,
                                Map<String, List<HashMap<String, Integer>>> oeuvresParType,
                                List<HashMap<String, Integer>> randomOeuvresList) {
        List<ListItem> updatedItems = new ArrayList<>();

        // Ajout de la section des œuvres récentes
        if (!oeuvresList.isEmpty()) {
            updatedItems.add(new ListItem(getString(R.string.lastRealeseText) + " (" + oeuvresList.size() + ")", oeuvresList));
        }

        // Ajout des sections par type d'œuvre
        for (String type : typesDisponibles) {
            List<HashMap<String, Integer>> typeList = oeuvresParType.get(type);
            if (!typeList.isEmpty()) {
                updatedItems.add(new ListItem(String.format(getString(R.string.lastXadded), type.toLowerCase()) + " (" + typeList.size() + ")", typeList));
            }
        }

        // Ajout des œuvres aléatoires
        updatedItems.add(new ListItem(getString(R.string.randomOeuvresText), randomOeuvresList));

        // Mise à jour de la liste des items affichés dans l'adaptateur
        items.clear();
        items.addAll(updatedItems);
        adapter.notifyDataSetChanged();
    }

    /**
     * Crée un HashMap avec un nom d'œuvre et son identifiant.
     * @param key Nom de l'œuvre.
     * @param value Identifiant de l'œuvre.
     * @return Un HashMap contenant le nom et l'ID de l'œuvre.
     */
    private HashMap<String, Integer> createHashMap(String key, Integer value) {
        HashMap<String, Integer> map = new HashMap<>();
        map.put(key, value); // La valeur ici est l'ID de l'œuvre
        return map;
    }
}
