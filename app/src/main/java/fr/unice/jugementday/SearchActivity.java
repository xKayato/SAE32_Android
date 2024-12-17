package fr.unice.jugementday;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import fr.unice.jugementday.button.MenuButtons;
import fr.unice.jugementday.service.UrlReader;

public class SearchActivity extends AppCompatActivity {

    private ArrayAdapter<String> myAdapter;
    private ArrayList<String> items = new ArrayList<>();
    private List<String> oeuvresList = new ArrayList<>();
    private List<String> randomOeuvresList = new ArrayList<>();
    private List<Integer> selectedIndices = new ArrayList<>(); // Liste des indices déjà sélectionnés
    private EditText searchField;
    private ImageButton confirmSearchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        // Initialisation des boutons du menu
        ImageButton profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> MenuButtons.profileClick(this));

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> MenuButtons.homeClick(this));

        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> MenuButtons.searchClick(this));

        // Initialisation de l'adaptateur pour la liste
        myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        ListView searchList = findViewById(R.id.searchList);
        searchList.setAdapter(myAdapter);

        // Initialisation du champ de recherche et du bouton de confirmation
        searchField = findViewById(R.id.searchField);
        confirmSearchButton = findViewById(R.id.confirmSearchButton);

        // Appel de la méthode pour récupérer et analyser les données
        fetchDataFromUrl("http://10.3.122.146/getdata.php?passid=SalutJeSuisUnMotDePassePourGet&table=Oeuvre");

        // Pour sur elever un peu le menu par rapport au bas de l'écran
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ajouter un OnClickListener sur chaque item de la liste (Pour juger les oeuvres)
        searchList.setOnItemClickListener((parent, view, position, id) -> {
            String selectedTitle = items.get(position);  // Nom de l'œuvre cliquée
            openDetailActivity(selectedTitle);
        });

        // Ajouter un listener pour le bouton de recherche (Pour appliquer la recherche)
        confirmSearchButton.setOnClickListener(v -> performSearch(searchField.getText().toString()));
    }

    // Méthode pour récupérer les données depuis l'URL et mettre à jour la liste
    private void fetchDataFromUrl(String url) {
        new Thread(() -> {
            // Ici, on appelle la méthode pour récupérer les données de l'URL
            String result = new UrlReader().fetchData(url);

            // Mise à jour de l'interface (doit être effectué sur le thread principal)
            runOnUiThread(() -> {
                if (result.startsWith("Erreur")) {
                    Toast.makeText(this, result, Toast.LENGTH_LONG).show();
                } else {
                    // Une fois les données récupérées, on les analyse
                    parseAndUpdateData(result);
                }
            });
        }).start();
    }

    // Méthode pour analyser les données JSON et mettre à jour la liste
    private void parseAndUpdateData(String jsonData) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);

            // Liste temporaire pour stocker les nouveaux items
            List<String> updatedItems = new ArrayList<>();

            // Ajouter toutes les œuvres dans la liste principale
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String nomOeuvre = jsonObject.getString("nomOeuvre");

                // Ajouter l'œuvre sans image
                oeuvresList.add(nomOeuvre);
            }

            // Choisir 20 oevres au hasard parmi celles disponibles
            while (randomOeuvresList.size() < 20 && selectedIndices.size() < oeuvresList.size()) {
                int randomIndex = new Random().nextInt(oeuvresList.size());
                if (!selectedIndices.contains(randomIndex)) {
                    selectedIndices.add(randomIndex);
                    randomOeuvresList.add(oeuvresList.get(randomIndex));
                    updatedItems.add(oeuvresList.get(randomIndex)); // Ajouter le nom de l'œuvre
                }
            }

            // Mise à jour de la liste des éléments
            items.clear();
            items.addAll(updatedItems);

            // Mise à jour de l'adaptateur
            myAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors de l'analyse des données", Toast.LENGTH_LONG).show();
        }
    }

    // Méthode pour effectuer la recherche dans la liste des œuvres
    private void performSearch(String query) {
        List<String> searchResults = new ArrayList<>();

        // Rechercher parmi toutes les œuvres
        for (String oeuvre : oeuvresList) {
            if (oeuvre.toLowerCase().contains(query.toLowerCase())) {
                searchResults.add(oeuvre);
            }
        }

        // Mettre à jour les éléments affichés
        items.clear();
        items.addAll(searchResults);
        myAdapter.notifyDataSetChanged();
    }

    // Méthode pour juger l'oeuvre
    private void openDetailActivity(String title) {
        Intent intent = new Intent(SearchActivity.this, JudgementActivity.class);
        intent.putExtra("title", title); // Passage du titre de l'œuvre
        startActivity(intent);
    }
}
