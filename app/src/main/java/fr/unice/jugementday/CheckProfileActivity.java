package fr.unice.jugementday;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

import fr.unice.jugementday.service.MenuButtons;
import fr.unice.jugementday.service.CustomArrayAdapter;
import fr.unice.jugementday.service.UrlReader;
import fr.unice.jugementday.service.UserSessionManager;

public class CheckProfileActivity extends AppCompatActivity {

    private UrlReader urlReader;
    private CustomArrayAdapter adapter;
    private List<ListItem> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_check_profile);

        TextView pseudo = findViewById(R.id.AccountOfText);

        ListView listView = findViewById(R.id.allJudgementList);





        Intent intent = getIntent();
        String username = intent.getStringExtra("login");

        // Initialisation de l'adaptateur pour la liste
        adapter = new CustomArrayAdapter(this, items, username);
        listView.setAdapter(adapter);
        urlReader = new UrlReader();

        pseudo.setText(getString(R.string.accountCommuText, username));

        ImageButton profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> MenuButtons.profileClick(this));

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> MenuButtons.homeClick(this));

        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> MenuButtons.searchClick(this));

        String url = UrlReader.address + "?table=Avis&fields=nomOeuvre,idOeuvre&login=" + username;
        fetchDataFromUrl(url);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    private void fetchDataFromUrl(String url) {
        new Thread(() -> {
            String result = urlReader.fetchData(url);

            runOnUiThread(() -> {
                if (result.startsWith("Erreur")) {
                    Toast.makeText(this, R.string.errorText, Toast.LENGTH_LONG).show();
                } else {
                    parseAndUpdateData(result);
                }
            });
        }).start();
    }

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

            // Liste principale et liste pour les œuvres aléatoires
            List<HashMap<String, Integer>> oeuvresList = new ArrayList<>();
            List<HashMap<String, Integer>> randomOeuvresList = new ArrayList<>();
            List<Integer> selectedIndices = new ArrayList<>();

            // Parsing des données
            for (int i = jsonArray.length() - 1; i >= 0; i--) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String nomOeuvre = jsonObject.getString("nomOeuvre");
                Integer idOeuvre = jsonObject.getInt("idOeuvre");
                String type = jsonObject.getString("type");
                HashMap<String, Integer> oeuvreMap = createHashMap(nomOeuvre, idOeuvre);

                // Ajout dans la liste du type approprié
                if (oeuvresParType.containsKey(type)) {
                    oeuvresParType.get(type).add(oeuvreMap);
                }
                oeuvresList.add(oeuvreMap);
            }

            // Sélection aléatoire de 10 œuvres
            while (randomOeuvresList.size() < 10 && selectedIndices.size() < oeuvresList.size()) {
                int randomIndex = (int) (Math.random() * oeuvresList.size());
                if (!selectedIndices.contains(randomIndex)) {
                    selectedIndices.add(randomIndex);
                    randomOeuvresList.add(oeuvresList.get(randomIndex));
                }
            }

            // Mise à jour des sections dynamiques (si un type est rajouté, il sera affiché)
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
            Toast.makeText(this, "" + e, Toast.LENGTH_LONG).show();
        }
    }

    // Méthode pour récupérer les types dynamiques depuis le serveur
    private List<String> fetchTypesFromServer() {
        List<String> types = new ArrayList<>();
        try {
            UrlReader urlReader = new UrlReader();
            String result = urlReader.fetchData(UrlReader.address + "?table=Type");
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

}