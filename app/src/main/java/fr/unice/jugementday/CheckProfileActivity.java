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


        // Initialisation de l'adaptateur pour la liste
        adapter = new CustomArrayAdapter(this, items);
        listView.setAdapter(adapter);
        urlReader = new UrlReader();


        Intent intent = getIntent();
        String username = intent.getStringExtra("login");

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
                updatedItems.add(new ListItem(getString(R.string.judgedCommuText) + " (" + oeuvresList.size() + ")", oeuvresList));
            }
            if (!movieOeuvresList.isEmpty()) {
                updatedItems.add(new ListItem(getString(R.string.movieCommuText) + " (" + movieOeuvresList.size() + ")", movieOeuvresList));
            }
            if (!mangaOeuvresList.isEmpty()) {
                updatedItems.add(new ListItem(getString(R.string.mangaCommuText) + " (" + mangaOeuvresList.size() + ")", mangaOeuvresList));
            }
            if (!bookOeuvresList.isEmpty()) {
                updatedItems.add(new ListItem(getString(R.string.bookCommuText) + " (" + bookOeuvresList.size() + ")", bookOeuvresList));
            }
            if (!animeOeuvresList.isEmpty()) {
                updatedItems.add(new ListItem(getString(R.string.animeCommuText) + " (" + animeOeuvresList.size() + ")", animeOeuvresList));
            }
            if (!seriesOeuvresList.isEmpty()) {
                updatedItems.add(new ListItem(getString(R.string.seriesCommuText) + " (" + seriesOeuvresList.size() + ")", seriesOeuvresList));
            }
            if (!cartoonOeuvresList.isEmpty()) {
                updatedItems.add(new ListItem(getString(R.string.cartoonCommuText) + " (" + cartoonOeuvresList.size() + ")", cartoonOeuvresList));
            }

            // Mise à jour de l'adaptateur
            items.clear();
            items.addAll(updatedItems);
            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.errorText, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
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