package fr.unice.jugementday;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import fr.unice.jugementday.service.CustomArrayAdapter;
import fr.unice.jugementday.service.MenuButtons;
import fr.unice.jugementday.service.JsonStock;
import fr.unice.jugementday.service.SearchCustomArrayAdapter;
import fr.unice.jugementday.service.UserSessionManager;

public class SearchActivity extends AppCompatActivity {

    private ArrayAdapter<String> myAdapter;
    private final ArrayList<String> items = new ArrayList<>();
    private final List<HashMap<Integer, String>> oeuvresList = new ArrayList<>();
    private final List<String> personnesList = new ArrayList<>();
    private final List<Integer> randomOeuvresList = new ArrayList<>();
    private final List<String> randomPersonneList = new ArrayList<>();
    private final List<Integer> selectedIndices = new ArrayList<>();
    private EditText searchField;
    private String category;
    private String worksJson;
    private String peoplesJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        UserSessionManager sessionManager = new UserSessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            // Si l'utilisateur n'est pas connecté, rediriger vers la page de connexion
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        ImageButton profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> MenuButtons.profileClick(this));

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> MenuButtons.homeClick(this));

        category = "Oeuvre";

        Button peopleButton = findViewById(R.id.PeopleButtonSearch);
        peopleButton.setBackgroundColor(getResources().getColor(R.color.primary_button));
        peopleButton.setTextColor(getResources().getColor(R.color.white));
        Button titleButton = findViewById(R.id.TitleButtonSearch);
        titleButton.setBackgroundColor(getResources().getColor(R.color.secondary_button));
        titleButton.setTextColor(getResources().getColor(R.color.black));

        JsonStock jsonStock = new JsonStock(this);
        worksJson = jsonStock.getWorks();
        peoplesJson = jsonStock.getPeople();

        myAdapter = new SearchCustomArrayAdapter(this, items);
        ListView searchList = findViewById(R.id.searchList);
        searchList.setAdapter(myAdapter);


        searchField = findViewById(R.id.searchField);
        ImageButton confirmSearchButton = findViewById(R.id.confirmSearchButton);

        parseAndUpdateData(worksJson);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        searchList.setOnItemClickListener((parent, view, position, id) -> {
            if (Objects.equals(category, "Oeuvre")) {
                String selectedTitle = items.get(position);
                int selectedId = getIdByTitle(selectedTitle);
                openDetailActivityOeuvre(selectedTitle, selectedId);
            } else if (Objects.equals(category, "Personne")) {
                String selectedLogin = items.get(position);
                openDetailActivityPeople(selectedLogin);
            }
        });

        confirmSearchButton.setOnClickListener(v -> performSearch(searchField.getText().toString()));

        findViewById(R.id.TitleButtonSearch).setOnClickListener(v -> changeCategoryToOeuvre());

        findViewById(R.id.PeopleButtonSearch).setOnClickListener(v -> changeCategoryToPersonne());
    }


    /**
     * Changer la catégorie de recherche en "Oeuvre".
     */
    private void changeCategoryToOeuvre() {
        category = "Oeuvre";
        randomOeuvresList.clear();
        selectedIndices.clear();
        parseAndUpdateData(worksJson);
        // Changer les couleurs
        Button peopleButton = findViewById(R.id.PeopleButtonSearch);
        peopleButton.setBackgroundColor(getResources().getColor(R.color.primary_button));
        peopleButton.setTextColor(getResources().getColor(R.color.white));
        Button titleButton = findViewById(R.id.TitleButtonSearch);
        titleButton.setBackgroundColor(getResources().getColor(R.color.secondary_button));
        titleButton.setTextColor(getResources().getColor(R.color.black));
    }


    /**
     * Changer la catégorie de recherche en "Personne".
     */
    private void changeCategoryToPersonne() {
        category = "Personne";
        personnesList.clear();
        randomPersonneList.clear();
        selectedIndices.clear();
        parseAndUpdateData(peoplesJson);
        // Changer les couleurs
        Button peopleButton = findViewById(R.id.PeopleButtonSearch);
        peopleButton.setBackgroundColor(getResources().getColor(R.color.secondary_button));
        peopleButton.setTextColor(getResources().getColor(R.color.black));
        Button titleButton = findViewById(R.id.TitleButtonSearch);
        titleButton.setBackgroundColor(getResources().getColor(R.color.primary_button));
        titleButton.setTextColor(getResources().getColor(R.color.white));
    }


    /**
     * Analyser les données JSON et mettre à jour la liste d'affichage.
     * @param jsonData Données JSON contenant les informations des œuvres ou des personnes.
     */
    private void parseAndUpdateData(String jsonData) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);

            items.clear();
            oeuvresList.clear();
            personnesList.clear();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                if (Objects.equals(category, "Personne") && jsonObject.has("login")) {
                    personnesList.add(jsonObject.getString("login"));
                } else if (Objects.equals(category, "Oeuvre") && jsonObject.has("idOeuvre") && jsonObject.has("nomOeuvre")) {
                    int idOeuvre = jsonObject.getInt("idOeuvre");
                    String nomOeuvre = jsonObject.getString("nomOeuvre");
                    String type = jsonObject.getString("type");
                    HashMap<Integer, String> oeuvreMap = new HashMap<>();
                    oeuvreMap.put(idOeuvre, nomOeuvre + " [" + type + "]");
                    oeuvresList.add(oeuvreMap);
                }
            }

            if (Objects.equals(category, "Personne")) {
                items.addAll(getRandomItems(personnesList, randomPersonneList, 30));
            } else if (Objects.equals(category, "Oeuvre")) {
                for (int randomIndex : getRandomIndices(oeuvresList.size(), 30)) {
                    randomOeuvresList.add(getIdByIndex(randomIndex));
                    items.add(getTitleByIndex(randomIndex));
                }
            }

            myAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Obtenir une liste d'indices aléatoires.
     * @param size Taille de la liste source
     * @param limit Limite de la liste aléatoire
     * @return
     */
    private List<Integer> getRandomIndices(int size, int limit) {
        List<Integer> indices = new ArrayList<>();
        while (indices.size() < limit && selectedIndices.size() < size) {
            int randomIndex = (int) (Math.random() * size);
            if (!selectedIndices.contains(randomIndex)) {
                selectedIndices.add(randomIndex);
                indices.add(randomIndex);
            }
        }
        return indices;
    }

    /**
     * Obtenir une liste d'éléments aléatoires.
     * @param sourceList Liste source
     * @param targetList Liste cible
     * @param limit Limite de la liste aléatoire
     * @return
     */
    private List<String> getRandomItems(List<String> sourceList, List<String> targetList, int limit) {
        for (int randomIndex : getRandomIndices(sourceList.size(), limit)) {
            targetList.add(sourceList.get(randomIndex));
        }
        return targetList;
    }


    private int getIdByIndex(int index) {
        return oeuvresList.get(index).keySet().iterator().next();
    }

    private String getTitleByIndex(int index) {
        return oeuvresList.get(index).values().iterator().next();
    }

    private int getIdByTitle(String title) {
        for (HashMap<Integer, String> oeuvre : oeuvresList) {
            if (oeuvre.containsValue(title)) {
                return oeuvre.keySet().iterator().next();
            }
        }
        return -1; // Si l'œuvre n'est pas trouvée
    }

    /**
     * Effectuer une recherche dans la liste d'œuvres ou de personnes.
     * @param query La chaîne de recherche saisie par l'utilisateur.
     */
    private void performSearch(String query) {
        // Supprimer les espaces en début et fin de chaîne
        query = query.trim();

        List<String> searchResults = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase();

        if (Objects.equals(category, "Oeuvre")) {
            for (HashMap<Integer, String> oeuvreMap : oeuvresList) {
                String oeuvre = oeuvreMap.values().iterator().next();
                // Séparer le titre et le type
                String titre = oeuvre.replaceAll("\\[.*?\\]", "").trim(); // Enlève le type [type]
                String type = oeuvre.replaceAll(".*\\[(.*?)\\].*", "$1").toLowerCase(); // Extrait le type [type]

                // Vérifie si la requête correspond soit au titre (partielle) soit au type (exacte)
                if (titre.toLowerCase().contains(lowerCaseQuery) || type.equals(lowerCaseQuery)) {
                    searchResults.add(oeuvre);
                }
            }
        } else if (Objects.equals(category, "Personne")) {
            for (String personne : personnesList) {
                if (personne.toLowerCase().contains(lowerCaseQuery)) {
                    searchResults.add(personne);
                }
            }
        }

        // Met à jour les éléments affichés avec les résultats de recherche
        items.clear();
        items.addAll(searchResults);
        myAdapter.notifyDataSetChanged();
    }



    /**
     * Ouvrir l'activité de détail d'une œuvre.
     * @param title Titre de l'œuvre
     * @param id ID de l'œuvre
     */
    private void openDetailActivityOeuvre(String title, int id) {
        Intent intent;
        if(alreadyJudged(id)){
            intent = new Intent(this, CheckJudgementActivity.class);
        } else {
            intent = new Intent(this, JudgementActivity.class);
        }
        intent.putExtra("title", title.split("\\[")[0]);
        intent.putExtra("idOeuvre", id);
        intent.putExtra("login", new UserSessionManager(this).getLogin());
        startActivity(intent);
    }

    /**
     * Ouvrir l'activité de détail d'une personne.
     * @param login Login de la personne
     */
    private void openDetailActivityPeople(String login) {
        Intent intent = new Intent(SearchActivity.this, CheckProfileActivity.class);
        intent.putExtra("login", login);
        startActivity(intent);
    }

    /**
     * Vérifier si l'utilisateur a déjà jugé une œuvre.
     * @param idOeuvre ID de l'œuvre
     * @return true si l'utilisateur a déjà jugé l'œuvre, false sinon
     */
    private boolean alreadyJudged(int idOeuvre) {
        JsonStock jsonStock = new JsonStock(this);
        String judged = jsonStock.getJudged();
        try {
            JSONArray jsonArray = new JSONArray(judged);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getInt("idOeuvre") == idOeuvre) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}