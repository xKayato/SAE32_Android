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

import fr.unice.jugementday.service.MenuButtons;
import fr.unice.jugementday.service.CustomArrayAdapter;
import fr.unice.jugementday.service.UrlReader;
import fr.unice.jugementday.service.JsonStock;
import fr.unice.jugementday.service.UserSessionManager;

public class HomeActivity extends AppCompatActivity {

    private UrlReader urlReader;
    private CustomArrayAdapter adapter;
    private List<ListItem> items = new ArrayList<>();
    private JsonStock jsonStock;
    private Button addWorkButton;
    private String userLogin;
    private UserSessionManager sessionManager;
    private String Works;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        jsonStock = new JsonStock(this);
        sessionManager = new UserSessionManager(this);
        // Vérifier si l'utilisateur est connecté
        if (sessionManager.isLoggedIn()) {
            userLogin = sessionManager.getLogin();
        } else {
            // Si l'utilisateur n'est pas connecté, rediriger vers la page de connexion
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
        }



        ListView listView = findViewById(R.id.allJudgementList);

        // Initialisation de l'adaptateur pour la liste
        adapter = new CustomArrayAdapter(this, items);
        listView.setAdapter(adapter);

        // Initialisation du lecteur d'URL
        urlReader = new UrlReader();

        Works = jsonStock.getWorks();
        parseAndUpdateData(Works);


        addWorkButton = findViewById(R.id.AddWorkButton);
        addWorkButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddWorkActivity.class);
            startActivity(intent);
        });

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

    private void parseAndUpdateData(String jsonData) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);

            // Liste temporaire pour stocker les nouveaux items
            List<ListItem> updatedItems = new ArrayList<>();

            // Parsing des données
            List<HashMap<String, Integer>> oeuvresList = new ArrayList<>();
            List<HashMap<String, Integer>> randomOeuvresList = new ArrayList<>();
            List<HashMap<String, Integer>> movieOeuvresList = new ArrayList<>();
            List<HashMap<String, Integer>> mangaOeuvresList = new ArrayList<>();
            List<HashMap<String, Integer>> bookOeuvresList = new ArrayList<>();
            List<HashMap<String, Integer>> animeOeuvresList = new ArrayList<>();
            List<HashMap<String, Integer>> seriesOeuvresList = new ArrayList<>();
            List<HashMap<String, Integer>> cartoonOeuvresList = new ArrayList<>();
            List<HashMap<String, Object>> bestOeuvresList = new ArrayList<>();


            // Liste des indices déjà sélectionnés pour éviter les doublons
            List<Integer> selectedIndices = new ArrayList<>();

            // Ajouter les œuvres dans la liste principale
            for (int i = jsonArray.length()-1; i >= 0; i--) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String nomOeuvre = jsonObject.getString("nomOeuvre");
                Integer idOeuvre = jsonObject.getInt("idOeuvre");
                String type = jsonObject.getString("type");
                HashMap<String, Integer> oeuvreMap = createHashMap(nomOeuvre, idOeuvre);

                switch(type){
                    case "Film":
                        movieOeuvresList.add(oeuvreMap);
                        break;
                    case "Manga":
                        mangaOeuvresList.add(oeuvreMap);
                        break;
                    case "Livre":
                        bookOeuvresList.add(oeuvreMap);
                        break;
                    case "Anime":
                        animeOeuvresList.add(oeuvreMap);
                        break;
                    case "Serie":
                        seriesOeuvresList.add(oeuvreMap);
                        break;
                    case "Dessin Anime":
                        cartoonOeuvresList.add(oeuvreMap);
                        break;
                    default:
                        break;

                }
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
            if (!oeuvresList.isEmpty()) {
                updatedItems.add(new ListItem(getString(R.string.lastRealeseText) + " (" + oeuvresList.size() + ")", oeuvresList));
            }
            if (!movieOeuvresList.isEmpty()) {
                updatedItems.add(new ListItem(getString(R.string.lastMoviesText) + " (" + movieOeuvresList.size() + ")", movieOeuvresList));
            }
            if (!mangaOeuvresList.isEmpty()) {
                updatedItems.add(new ListItem(getString(R.string.lastMangasText) + " (" + mangaOeuvresList.size() + ")", mangaOeuvresList));
            }
            if (!bookOeuvresList.isEmpty()) {
                updatedItems.add(new ListItem(getString(R.string.lastBooksText) + " (" + bookOeuvresList.size() + ")", bookOeuvresList));
            }
            if (!animeOeuvresList.isEmpty()) {
                updatedItems.add(new ListItem(getString(R.string.lastAnimesText) + " (" + animeOeuvresList.size() + ")", animeOeuvresList));
            }
            if (!seriesOeuvresList.isEmpty()) {
                updatedItems.add(new ListItem(getString(R.string.lastSeriesText) + " (" + seriesOeuvresList.size() + ")", seriesOeuvresList));
            }
            if (!cartoonOeuvresList.isEmpty()) {
                updatedItems.add(new ListItem(getString(R.string.lastCartoonsText) + " (" + cartoonOeuvresList.size() + ")", cartoonOeuvresList));
            }
            // Mise à jour des sections avec les nouvelles données
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


    private HashMap<String, Integer> createHashMap(String key, Integer value) {
        HashMap<String, Integer> map = new HashMap<>();
        map.put(key, value); // La valeur est l'image par défaut ici
        return map;
    }
}
