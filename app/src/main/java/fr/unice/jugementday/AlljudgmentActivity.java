package fr.unice.jugementday;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
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
import java.util.List;
import java.util.Random;

import fr.unice.jugementday.button.MenuButtons;
import fr.unice.jugementday.service.UrlReader;

public class AlljudgmentActivity extends AppCompatActivity {

    private ArrayAdapter<String> myAdapter;
    private TextView CritiqueText;
    private ImageView Image;
    private Intent intent;
    private String title;
    private ArrayList<String> items = new ArrayList<>();
    private List<String> oeuvresList = new ArrayList<>();
    private List<String> randomOeuvresList = new ArrayList<>();
    private List<Integer> selectedIndices = new ArrayList<>(); // Liste des indices déjà sélectionnés
    int note = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alljudgment);

        CritiqueText = findViewById(R.id.critiqueText);
        intent = getIntent();
        title= getIntent().getStringExtra("title");
        String critique = getString(R.string.critiqueText);
        String newTitle = critique.replace("oeuvre", title);
        CritiqueText.setText(newTitle);
        Image = findViewById(R.id.workImagePlaceholderPicture);
        Image.setImageResource(intent.getIntExtra("photo", 0));

        myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        ListView searchList = findViewById(R.id.allJudgementList);
        searchList.setAdapter(myAdapter);

        fetchDataFromUrl("http://10.3.122.146/getdata.php?table=Avis&fields=texteAvis,login,note&idOeuvre=" + title);

        ImageButton profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> MenuButtons.profileClick(this));

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> MenuButtons.homeClick(this));

        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> MenuButtons.searchClick(this));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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
                String texteAvis = jsonObject.getString("texteAvis");
                String pseudo = jsonObject.getString("login");
                String note = jsonObject.getString("note");


                // Ajouter l'œuvre sans image
                oeuvresList.add(pseudo + " (" + note + "/5) : " + texteAvis);
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
}