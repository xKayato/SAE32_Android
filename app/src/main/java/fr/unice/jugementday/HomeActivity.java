package fr.unice.jugementday;

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

import fr.unice.jugementday.button.MenuButtons;
import fr.unice.jugementday.service.UrlReader;

public class HomeActivity extends AppCompatActivity {

    private UrlReader urlReader;
    private CustomArrayAdapter adapter;
    private List<ListItem> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ListView listView = findViewById(R.id.listView);

        // Initialisation de l'adaptateur pour la liste
        adapter = new CustomArrayAdapter(this, items);
        listView.setAdapter(adapter);

        // Initialisation du lecteur d'URL
        urlReader = new UrlReader();

        // URL pour récupérer les données
        String url = "http://10.3.122.146/getdata.php?passid=SalutJeSuisUnMotDePassePourGet&table=Oeuvre&fields=nomOeuvre";
        fetchDataFromUrl(url);

        // Boutons de navigation
        ImageButton profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> MenuButtons.profileClick(this));

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> MenuButtons.homeClick(this));

        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> MenuButtons.searchClick(this));

        // Pour sur elever un peu le menu par rapport au bas de l'écran
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
                    Toast.makeText(this, result, Toast.LENGTH_LONG).show();
                } else {
                    parseAndUpdateData(result);
                }
            });
        }).start();
    }

    private void parseAndUpdateData(String jsonData) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);

            // Liste temporaire pour stocker les nouveaux items
            List<ListItem> updatedItems = new ArrayList<>();

            // Parsing des données
            List<HashMap<String, Integer>> oeuvresList = new ArrayList<>();
            List<HashMap<String, Integer>> randomOeuvresList = new ArrayList<>();

            // Liste des indices déjà sélectionnés pour éviter les doublons
            List<Integer> selectedIndices = new ArrayList<>();

            // Ajouter les œuvres dans la liste principale
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String nomOeuvre = jsonObject.getString("nomOeuvre");

                // Ajouter le titre avec une image par défaut
                HashMap<String, Integer> oeuvreMap = createHashMap(nomOeuvre, R.drawable.chainsawman);
                oeuvresList.add(oeuvreMap);
            }

            // Choisir 10 œuvres au hasard parmi celles disponibles
            while (randomOeuvresList.size() < 10 && selectedIndices.size() < oeuvresList.size()) {
                int randomIndex = (int) (Math.random() * oeuvresList.size());
                if (!selectedIndices.contains(randomIndex)) {
                    selectedIndices.add(randomIndex);
                    randomOeuvresList.add(oeuvresList.get(randomIndex));
                }
            }

            // Mise à jour des sections avec les nouvelles données
            updatedItems.add(new ListItem(getString(R.string.lastRealeseText), oeuvresList));
            updatedItems.add(new ListItem(getString(R.string.randomOeuvresText), randomOeuvresList));

            // Mise à jour de l'adaptateur
            items.clear();
            items.addAll(updatedItems);
            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors de l'analyse des données", Toast.LENGTH_LONG).show();
        }
    }


    private HashMap<String, Integer> createHashMap(String key, Integer value) {
        HashMap<String, Integer> map = new HashMap<>();
        map.put(key, value); // La valeur est l'image par défaut ici
        return map;
    }
}
