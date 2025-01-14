package fr.unice.jugementday;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import fr.unice.jugementday.service.JsonStock;
import fr.unice.jugementday.service.UrlReader;
import fr.unice.jugementday.service.UserSessionManager;

public class ProfileActivity extends AppCompatActivity {

    private UserSessionManager sessionManager;
    private UrlReader urlReader;
    private CustomArrayAdapter adapter;
    private List<ListItem> items = new ArrayList<>();
    private String userLogin;
    private JsonStock jsonStock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        sessionManager = new UserSessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
        }

        jsonStock = new JsonStock(this);

        setupMenuButtons();
        setupListView();
        setupAdminPicture();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }



    /**
     * Redirige l'utilisateur vers la page de connexion.
     */
    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    /**
     * Configure les boutons du menu.
     */
    private void setupMenuButtons() {
        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> MenuButtons.homeClick(this));

        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> MenuButtons.searchClick(this));
    }

    /**
     * Configure la ListView et initialise l'adaptateur.
     */
    private void setupListView() {
        ListView listView = findViewById(R.id.allJudgementList);

        if (sessionManager.isLoggedIn()) {
            userLogin = sessionManager.getLogin();
            TextView usernameTextView = findViewById(R.id.AccountOfText);
            usernameTextView.setText("Bonjour, " + userLogin);
        } else {
            redirectToLogin();
        }

        adapter = new CustomArrayAdapter(this, items, userLogin);
        listView.setAdapter(adapter);
        urlReader = new UrlReader();

        String judgements = jsonStock.getJudged();
        if (judgements != null) {
            parseAndUpdateData(judgements);
        }
    }

    /**
     * Configure l'affichage de l'image d'admin.
     */
    private void setupAdminPicture() {
        ImageView adminPicture = findViewById(R.id.adminPicture);
        if (Objects.equals(sessionManager.getAccess(), "1")) {
            adminPicture.setVisibility(View.VISIBLE);
        } else {
            adminPicture.setVisibility(View.GONE);
        }
    }

    /**
     * Parse les données JSON et met à jour la liste des items.
     * @param jsonData Les données JSON à parser.
     */
    private void parseAndUpdateData(String jsonData) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);

            List<String> typesDisponibles = fetchTypesFromServer();
            Map<String, List<HashMap<String, Integer>>> oeuvresParType = initializeOeuvresParType(typesDisponibles);

            List<HashMap<String, Integer>> oeuvresList = new ArrayList<>();
            List<HashMap<String, Integer>> randomOeuvresList = new ArrayList<>();
            List<Integer> selectedIndices = new ArrayList<>();

            parseJsonArray(jsonArray, oeuvresParType, oeuvresList);
            selectRandomOeuvres(oeuvresList, randomOeuvresList, selectedIndices);

            updateAdapterItems(typesDisponibles, oeuvresParType, oeuvresList, randomOeuvresList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialise la map des œuvres par type.
     * @param typesDisponibles Les types disponibles.
     * @return La map initialisée.
     */
    private Map<String, List<HashMap<String, Integer>>> initializeOeuvresParType(List<String> typesDisponibles) {
        Map<String, List<HashMap<String, Integer>>> oeuvresParType = new HashMap<>();
        for (String type : typesDisponibles) {
            oeuvresParType.put(type, new ArrayList<>());
        }
        return oeuvresParType;
    }

    /**
     * Parse le tableau JSON et remplit les listes d'œuvres.
     * @param jsonArray Le tableau JSON à parser.
     * @param oeuvresParType La map des œuvres par type.
     * @param oeuvresList La liste des œuvres.
     */
    private void parseJsonArray(JSONArray jsonArray, Map<String, List<HashMap<String, Integer>>> oeuvresParType, List<HashMap<String, Integer>> oeuvresList) throws Exception {
        for (int i = jsonArray.length() - 1; i >= 0; i--) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String nomOeuvre = jsonObject.getString("nomOeuvre");
            Integer idOeuvre = jsonObject.getInt("idOeuvre");
            String type = jsonObject.getString("type");
            HashMap<String, Integer> oeuvreMap = createHashMap(nomOeuvre, idOeuvre);

            if (oeuvresParType.containsKey(type)) {
                oeuvresParType.get(type).add(oeuvreMap);
            }
            oeuvresList.add(oeuvreMap);
        }
    }

    /**
     * Sélectionne aléatoirement 10 œuvres.
     * @param oeuvresList La liste des œuvres.
     * @param randomOeuvresList La liste des œuvres aléatoires.
     * @param selectedIndices Les indices sélectionnés.
     */
    private void selectRandomOeuvres(List<HashMap<String, Integer>> oeuvresList, List<HashMap<String, Integer>> randomOeuvresList, List<Integer> selectedIndices) {
        while (randomOeuvresList.size() < 10 && selectedIndices.size() < oeuvresList.size()) {
            int randomIndex = (int) (Math.random() * oeuvresList.size());
            if (!selectedIndices.contains(randomIndex)) {
                selectedIndices.add(randomIndex);
                randomOeuvresList.add(oeuvresList.get(randomIndex));
            }
        }
    }

    /**
     * Met à jour les items de l'adaptateur.
     * @param typesDisponibles Les types disponibles.
     * @param oeuvresParType La map des œuvres par type.
     * @param oeuvresList La liste des œuvres.
     * @param randomOeuvresList La liste des œuvres aléatoires.
     */
    private void updateAdapterItems(List<String> typesDisponibles, Map<String, List<HashMap<String, Integer>>> oeuvresParType, List<HashMap<String, Integer>> oeuvresList, List<HashMap<String, Integer>> randomOeuvresList) {
        List<ListItem> updatedItems = new ArrayList<>();
        if (!oeuvresList.isEmpty()) {
            updatedItems.add(new ListItem(getString(R.string.lastRealeseText) + " (" + oeuvresList.size() + ")", oeuvresList));
        }
        for (String type : typesDisponibles) {
            List<HashMap<String, Integer>> typeList = oeuvresParType.get(type);
            if (!typeList.isEmpty()) {
                updatedItems.add(new ListItem(String.format(getString(R.string.XWorkText), type.toLowerCase()) + " (" + typeList.size() + ")", typeList));
            }
        }

        updatedItems.add(new ListItem(getString(R.string.randomOeuvresText), randomOeuvresList));

        items.clear();
        items.addAll(updatedItems);
        adapter.notifyDataSetChanged();
    }

    // Méthode pour récupérer les types dynamiques depuis le serveur
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

    private HashMap<String, Integer> createHashMap(String key, Integer value) {
        HashMap<String, Integer> map = new HashMap<>();
        map.put(key, value); // La valeur est l'image par défaut ici
        return map;
    }


    public void onSettingClick(View v) {
        Intent i = new Intent(this, AccountSettingsActivity.class);
        i.putExtra("title", "Settings");
        startActivity(i);
    }

}