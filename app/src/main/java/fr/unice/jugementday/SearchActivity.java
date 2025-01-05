package fr.unice.jugementday;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
import java.util.Objects;

import fr.unice.jugementday.service.MenuButtons;
import fr.unice.jugementday.service.JsonStock;

public class SearchActivity extends AppCompatActivity {

    private ArrayAdapter<String> myAdapter;
    private ArrayList<String> items = new ArrayList<>();
    private List<HashMap<String, String>> oeuvresList = new ArrayList<>();
    private List<String> personnesList = new ArrayList<>();
    private List<String> randomOeuvresList = new ArrayList<>();
    private List<String> randomPersonneList = new ArrayList<>();
    private List<Integer> selectedIndices = new ArrayList<>(); // Liste des indices déjà sélectionnés
    private EditText searchField;
    private ImageButton confirmSearchButton;
    private String category;
    private JsonStock jsonStock;
    private String Works;
    private String Peoples;

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

        category = "Oeuvre";

        jsonStock = new JsonStock(this);

        Works = jsonStock.getWorks();
        Peoples = jsonStock.getPeople();

        // Initialisation de l'adaptateur pour la liste
        myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        ListView searchList = findViewById(R.id.searchList);
        searchList.setAdapter(myAdapter);

        // Initialisation du champ de recherche et du bouton de confirmation
        searchField = findViewById(R.id.searchField);
        confirmSearchButton = findViewById(R.id.confirmSearchButton);

        parseAndUpdateData(Works);

        // Pour sur elever un peu le menu par rapport au bas de l'écran
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ajouter un OnClickListener sur chaque item de la liste (Pour juger les oeuvres)
        searchList.setOnItemClickListener((parent, view, position, id) -> {
            String selectedTitle = items.get(position);  // Nom de l'œuvre cliquée
            String selectedId = oeuvresList.get(position).get(selectedTitle);  // Récupérer l'id de l'œuvre cliquée
            openDetailActivity(selectedTitle, selectedId);

        });

        // Ajouter un listener pour le bouton de recherche (Pour appliquer la recherche)
        confirmSearchButton.setOnClickListener(v -> performSearch(searchField.getText().toString()));

        // Ajouter un listener pour le bouton de recherche par oeuvre
        findViewById(R.id.TitleButtonSearch).setOnClickListener(v -> {changeCategoryToOeuvre();});

        // Ajouter un listener pour le bouton de recherche par personne
        findViewById(R.id.PeopleButtonSearch).setOnClickListener(v -> {changeCategoryToPersonne();});
    }

    // Méthode pour changer la catégorie de recherche en Titre
    private void changeCategoryToOeuvre() {
        category = "Oeuvre";
        randomOeuvresList.clear();
        selectedIndices.clear();
        parseAndUpdateData(Works);
        findViewById(R.id.PeopleButtonSearch).setBackgroundColor(getResources().getColor(R.color.primary_button));
        findViewById(R.id.TitleButtonSearch).setBackgroundColor(getResources().getColor(R.color.secondary_button));
    }

    // Méthode pour changer la catégorie de recherche en Personne
    private void changeCategoryToPersonne() {
        category = "Personne";
        personnesList.clear();
        randomPersonneList.clear();
        selectedIndices.clear();
        parseAndUpdateData(Peoples);
        findViewById(R.id.PeopleButtonSearch).setBackgroundColor(getResources().getColor(R.color.secondary_button));
        findViewById(R.id.TitleButtonSearch).setBackgroundColor(getResources().getColor(R.color.primary_button));
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

                if(Objects.equals(category, "Personne")){
                    if(jsonObject.has("pseudo")){
                        String nomPersonne = jsonObject.getString("pseudo");
                        personnesList.add(nomPersonne);
                    }
                } else if(Objects.equals(category, "Oeuvre")){
                    if(jsonObject.has("idOeuvre")){
                        String idOeuvre = jsonObject.getString("idOeuvre");
                        String nomOeuvre = jsonObject.getString("nomOeuvre");
                        HashMap<String, String> oeuvreMap = new HashMap<>();
                        oeuvreMap.put(nomOeuvre, idOeuvre);
                        oeuvresList.add(oeuvreMap);
                    }

                }

            }


            if(Objects.equals(category, "Personne")){
                // Choisir 10 personnes au hasard parmi celles disponibles
                while (randomPersonneList.size() < 10 && selectedIndices.size() < personnesList.size()) {
                    int randomIndex = (int) (Math.random() * personnesList.size());
                    if (!selectedIndices.contains(randomIndex)) {
                        selectedIndices.add(randomIndex);
                        randomPersonneList.add(personnesList.get(randomIndex));
                        updatedItems.add(personnesList.get(randomIndex));
                    }
                }
            } else if(Objects.equals(category, "Oeuvre")) {
                // Choisir 10 œuvres au hasard parmi celles disponibles
                while (randomOeuvresList.size() < 10 && selectedIndices.size() < oeuvresList.size()) {
                    int randomIndex = (int) (Math.random() * oeuvresList.size());
                    if (!selectedIndices.contains(randomIndex)) {
                        selectedIndices.add(randomIndex);
                        String oeuvreName = oeuvresList.get(randomIndex).keySet().iterator().next();
                        randomOeuvresList.add(oeuvreName);
                        updatedItems.add(oeuvreName);
                    }
                }
            }


            // Mise à jour de la liste des éléments
            items.clear();
            items.addAll(updatedItems);

            // Mise à jour de l'adaptateur
            myAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors de l'analyse des données" + e, Toast.LENGTH_LONG).show();
        }
    }

    // Méthode pour effectuer la recherche dans la liste des œuvres
    private void performSearch(String query) {
        List<String> searchResults = new ArrayList<>();

        if(Objects.equals(category, "Oeuvre")){
            // Rechercher parmi toutes les œuvres
            for (HashMap<String, String> oeuvreMap : oeuvresList) {
                String oeuvre = oeuvreMap.keySet().iterator().next();
                if (oeuvre.toLowerCase().contains(query.toLowerCase())) {
                    searchResults.add(oeuvre);
                }
            }
        } else if(Objects.equals(category, "Personne")){
            // Rechercher parmi toutes les personnes
            for (String personne : personnesList) {
                if (personne.toLowerCase().contains(query.toLowerCase())) {
                    searchResults.add(personne);
                }
            }
        }

        // Mettre à jour les éléments affichés
        items.clear();
        items.addAll(searchResults);
        myAdapter.notifyDataSetChanged();
    }

    // Méthode pour le clique sur un élément de la liste (Personne ou Oeuvre)
    private void openDetailActivity(String title, String id) {
        if(Objects.equals(category, "Oeuvre")){
            Intent intent = new Intent(SearchActivity.this, JudgementActivity.class);
            intent.putExtra("title", title); // Passage du titre de l'œuvre
            intent.putExtra("id", id); // Passage de l'id de l'œuvre
            startActivity(intent);
        } else if(Objects.equals(category, "Personne")){
            Intent intent = new Intent(SearchActivity.this, CheckProfileActivity.class);
            intent.putExtra("pseudo", title); // Passage du pseudo
            startActivity(intent);
        }

    }
}
