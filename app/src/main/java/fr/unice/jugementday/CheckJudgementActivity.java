package fr.unice.jugementday;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.text.HtmlCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import fr.unice.jugementday.service.JsonStock;
import fr.unice.jugementday.service.MenuButtons;
import fr.unice.jugementday.service.UrlReader;
import fr.unice.jugementday.service.UserSessionManager;

public class CheckJudgementActivity extends AppCompatActivity {

    private String title;
    private int id;
    private TextView JudgementField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_judgement);
        EdgeToEdge.enable(this);

        ImageButton profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> MenuButtons.profileClick(this));

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> MenuButtons.homeClick(this));

        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> MenuButtons.searchClick(this));

        UserSessionManager sessionManager = new UserSessionManager(this);
        JsonStock jsonStock = new JsonStock(this);
        String works = jsonStock.getWorks();

        ImageButton community = findViewById(R.id.communityButton);
        community.setOnClickListener(this::onClickAllJudgement);

        TextView typeText = findViewById(R.id.typeText);
        TextView auteurText = findViewById(R.id.auteurText);
        TextView dateText = findViewById(R.id.dateText);
        JudgementField = findViewById(R.id.JudgementField);



        // Récupère les données de l'intent
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        String userLogin = intent.getStringExtra("login");
        id = intent.getIntExtra("idOeuvre", 0);

        TextView titleText = findViewById(R.id.titleText);
        titleText.setText(title);

        TextView pseudo = findViewById(R.id.JudgementOfText);
        String formattedText = getString(R.string.critiqueOfText, userLogin);
        pseudo.setText(formattedText);

        fetchDataFromUrl("&table=Avis&idOeuvre=" + String.valueOf(id) + "&login=" + userLogin);

        try{
            JSONArray jsonArray = new JSONArray(works);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int idOeuvre = jsonObject.getInt("idOeuvre");
                if (idOeuvre == id) {
                    typeText.setText(String.format("Genre : %s", capitalizeFirstLetter(jsonObject.getString("type"))));
                    auteurText.setText(String.format("Auteur/Studio : %s", capitalizeFirstLetter(jsonObject.getString("auteur_studio"))));
                    dateText.setText(String.format("Date : %s", jsonObject.getString("dateSortie")));
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public void onClickAllJudgement(View view) {
        Intent intent2 = new Intent(this, AlljudgmentActivity.class);
        intent2.putExtra("title", title);
        intent2.putExtra("idOeuvre", id);

        startActivity(intent2);

    }

    // Méthode pour récupérer les données depuis l'URL et mettre à jour la liste
    private void fetchDataFromUrl(String options) {
        new Thread(() -> {
            // Ici, on appelle la méthode pour récupérer les données de l'URL
            String result = new UrlReader().fetchData(options);

            // Mise à jour de l'interface (doit être effectué sur le thread principal)
            runOnUiThread(() -> {
                if (result.startsWith("Erreur")) {
                    Toast.makeText(this, R.string.errorText, Toast.LENGTH_LONG).show();
                } else {
                    parseAndUpdateData(result);
                }
            });
        }).start();
    }

    // Méthode pour analyser les données JSON et mettre à jour la liste
    private void parseAndUpdateData(String jsonData) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            String texteAvis = HtmlCompat.fromHtml(jsonObject.getString("texteAvis"), HtmlCompat.FROM_HTML_MODE_LEGACY).toString(); // Pour contrer le htmlspecialchars
            String note = jsonObject.getString("note");
            JudgementField.setText(texteAvis);
            TextView noteText = findViewById(R.id.noteText);
            noteText.setText(String.format("%s/5", note));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}