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
import java.util.Objects;

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
        jsonStock = new JsonStock(this);

        ImageButton profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> MenuButtons.profileClick(this));

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> MenuButtons.homeClick(this));

        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> MenuButtons.searchClick(this));

        ListView listView = findViewById(R.id.allJudgementList);

        // Initialisation de l'adaptateur pour la liste
        adapter = new CustomArrayAdapter(this, items);
        listView.setAdapter(adapter);
        urlReader = new UrlReader();


        // Vérifier si l'utilisateur est connecté
        if (sessionManager.isLoggedIn()) {
            userLogin = sessionManager.getLogin();
            // Utilisez le login comme vous voulez
            TextView usernameTextView = findViewById(R.id.AccountOfText);
            usernameTextView.setText("Bonjour, " + userLogin);
        } else {
            // Si l'utilisateur n'est pas connecté, rediriger vers la page de connexion
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        String judgements = jsonStock.getJudged();
        if (judgements != null) {
            parseAndUpdateData(judgements);
        }

        ImageView adminPicture = findViewById(R.id.adminPicture);
        if(Objects.equals(sessionManager.getAccess(), "1")){
            adminPicture.setVisibility(View.VISIBLE);
        } else {
            adminPicture.setVisibility(View.GONE);
        }



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
            List<HashMap<String, Integer>> movieOeuvresList = new ArrayList<>();
            List<HashMap<String, Integer>> mangaOeuvresList = new ArrayList<>();
            List<HashMap<String, Integer>> bookOeuvresList = new ArrayList<>();
            List<HashMap<String, Integer>> animeOeuvresList = new ArrayList<>();
            List<HashMap<String, Integer>> seriesOeuvresList = new ArrayList<>();
            List<HashMap<String, Integer>> cartoonOeuvresList = new ArrayList<>();

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





            // Mise à jour des sections avec les nouvelles données
            if (!oeuvresList.isEmpty()) {
                updatedItems.add(new ListItem(getString(R.string.judgedText) + " (" + oeuvresList.size() + ")", oeuvresList));
            }
            if (!movieOeuvresList.isEmpty()) {
                updatedItems.add(new ListItem(getString(R.string.movieWorkText) + " (" + movieOeuvresList.size() + ")", movieOeuvresList));
            }
            if (!mangaOeuvresList.isEmpty()) {
                updatedItems.add(new ListItem(getString(R.string.mangaWorkText) + " (" + mangaOeuvresList.size() + ")", mangaOeuvresList));
            }
            if (!bookOeuvresList.isEmpty()) {
                updatedItems.add(new ListItem(getString(R.string.bookWorkText) + " (" + bookOeuvresList.size() + ")", bookOeuvresList));
            }
            if (!animeOeuvresList.isEmpty()) {
                updatedItems.add(new ListItem(getString(R.string.animeWorkText) + " (" + animeOeuvresList.size() + ")", animeOeuvresList));
            }
            if (!seriesOeuvresList.isEmpty()) {
                updatedItems.add(new ListItem(getString(R.string.seriesWorkText) + " (" + seriesOeuvresList.size() + ")", seriesOeuvresList));
            }
            if (!cartoonOeuvresList.isEmpty()) {
                updatedItems.add(new ListItem(getString(R.string.cartoonWorkText) + " (" + cartoonOeuvresList.size() + ")", cartoonOeuvresList));
            }


            // Mise à jour de l'adaptateur
            items.clear();
            items.addAll(updatedItems);
            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            Toast.makeText(this, "Erreur lors de l'analyse des données", Toast.LENGTH_LONG).show();
        }
    }

    private HashMap<String, Integer> createHashMap(String key, Integer value) {
        HashMap<String, Integer> map = new HashMap<>();
        map.put(key, value); // La valeur est l'image par défaut ici
        return map;
    }

    public void onSettingClick(View v) {
        Intent i = new Intent(this, activitySettingsAccount.class);
        i.putExtra("title", "Settings");
        startActivity(i);
    }
}