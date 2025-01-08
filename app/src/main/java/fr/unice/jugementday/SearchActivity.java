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

public class SearchActivity extends AppCompatActivity {

    private ArrayAdapter<String> myAdapter;
    private ArrayList<String> items = new ArrayList<>();
    private List<HashMap<Integer, String>> oeuvresList = new ArrayList<>();
    private List<String> personnesList = new ArrayList<>();
    private List<Integer> randomOeuvresList = new ArrayList<>();
    private List<String> randomPersonneList = new ArrayList<>();
    private List<Integer> selectedIndices = new ArrayList<>();
    private EditText searchField;
    private String category;
    private String worksJson;
    private String peoplesJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        ImageButton profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> MenuButtons.profileClick(this));

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> MenuButtons.homeClick(this));

        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> MenuButtons.searchClick(this));

        category = "Oeuvre";

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

    private void changeCategoryToOeuvre() {
        category = "Oeuvre";
        randomOeuvresList.clear();
        selectedIndices.clear();
        parseAndUpdateData(worksJson);
        Button peopleButton = findViewById(R.id.PeopleButtonSearch);
        peopleButton.setBackgroundColor(getResources().getColor(R.color.primary_button));
        peopleButton.setTextColor(getResources().getColor(R.color.white));
        Button titleButton = findViewById(R.id.TitleButtonSearch);
        titleButton.setBackgroundColor(getResources().getColor(R.color.secondary_button));
        titleButton.setTextColor(getResources().getColor(R.color.black));
    }

    private void changeCategoryToPersonne() {
        category = "Personne";
        personnesList.clear();
        randomPersonneList.clear();
        selectedIndices.clear();
        parseAndUpdateData(peoplesJson);
        Button peopleButton = findViewById(R.id.PeopleButtonSearch);
        peopleButton.setBackgroundColor(getResources().getColor(R.color.secondary_button));
        peopleButton.setTextColor(getResources().getColor(R.color.black));
        Button titleButton = findViewById(R.id.TitleButtonSearch);
        titleButton.setBackgroundColor(getResources().getColor(R.color.primary_button));
        titleButton.setTextColor(getResources().getColor(R.color.white));
    }

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
                    HashMap<Integer, String> oeuvreMap = new HashMap<>();
                    oeuvreMap.put(idOeuvre, nomOeuvre);
                    oeuvresList.add(oeuvreMap);
                }
            }

            if (Objects.equals(category, "Personne")) {
                items.addAll(getRandomItems(personnesList, randomPersonneList, 10));
            } else if (Objects.equals(category, "Oeuvre")) {
                for (int randomIndex : getRandomIndices(oeuvresList.size(), 10)) {
                    randomOeuvresList.add(getIdByIndex(randomIndex));
                    items.add(getTitleByIndex(randomIndex));
                }
            }

            myAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors de l'analyse des données: " + e, Toast.LENGTH_LONG).show();
        }
    }

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
        return -1; // ID not found
    }

    private void performSearch(String query) {
        List<String> searchResults = new ArrayList<>();

        if (Objects.equals(category, "Oeuvre")) {
            for (HashMap<Integer, String> oeuvreMap : oeuvresList) {
                String oeuvre = oeuvreMap.values().iterator().next();
                if (oeuvre.toLowerCase().contains(query.toLowerCase())) {
                    searchResults.add(oeuvre);
                }
            }
        } else if (Objects.equals(category, "Personne")) {
            for (String personne : personnesList) {
                if (personne.toLowerCase().contains(query.toLowerCase())) {
                    searchResults.add(personne);
                }
            }
        }

        items.clear();
        items.addAll(searchResults);
        myAdapter.notifyDataSetChanged();
    }

    private void openDetailActivityOeuvre(String title, int id) {
        Intent intent = new Intent(SearchActivity.this, JudgementActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("idOeuvre", id);
        startActivity(intent);
    }

    private void openDetailActivityPeople(String login) {
        Intent intent = new Intent(SearchActivity.this, CheckProfileActivity.class);
        intent.putExtra("login", login);
        startActivity(intent);
    }
}