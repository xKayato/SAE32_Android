package fr.unice.jugementday;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import fr.unice.jugementday.service.JsonStock;
import fr.unice.jugementday.service.MenuButtons;
import fr.unice.jugementday.service.UrlReader;
import fr.unice.jugementday.service.UserSessionManager;

public class CheckJudgementActivity extends AppCompatActivity {

    private String userLogin;
    private UserSessionManager sessionManager;
    private String title;
    private int id;
    private JsonStock jsonStock;
    private String Works;
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

        sessionManager = new UserSessionManager(this);
        jsonStock = new JsonStock(this);
        Works = jsonStock.getWorks();



        TextView typeText = findViewById(R.id.typeText);
        TextView auteurText = findViewById(R.id.auteurText);
        TextView dateText = findViewById(R.id.dateText);
        JudgementField = findViewById(R.id.JudgementField);

        // Récupère les données de l'intent
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        userLogin = intent.getStringExtra("login");
        id = intent.getIntExtra("idOeuvre", 0);

        Toast.makeText(this, userLogin, Toast.LENGTH_SHORT).show();

        TextView titleText = findViewById(R.id.titleText);
        titleText.setText(title);

        TextView pseudo = findViewById(R.id.JudgementOfText);
        String formattedText = getString(R.string.critiqueOfText, userLogin);
        pseudo.setText(formattedText);

        fetchDataFromUrl(UrlReader.address + "?table=Avis&idOeuvre=" + String.valueOf(id) + "&login=" + userLogin);

        try{
            JSONArray jsonArray = new JSONArray(Works);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int idOeuvre = jsonObject.getInt("idOeuvre");
                if (idOeuvre == id) {
                    typeText.setText("Genre : " + jsonObject.getString("type").substring(0, 1).toUpperCase() + jsonObject.getString("type").substring(1));
                    auteurText.setText("Auteur/Studio : " + jsonObject.getString("auteur_studio").substring(0, 1).toUpperCase() + jsonObject.getString("auteur_studio").substring(1));
                    dateText.setText("Date : " + jsonObject.getString("dateSortie"));
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

    // Méthode pour récupérer les données depuis l'URL et mettre à jour la liste
    private void fetchDataFromUrl(String url) {
        new Thread(() -> {
            // Ici, on appelle la méthode pour récupérer les données de l'URL
            String result = new UrlReader().fetchData(url);

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
            noteText.setText(note + "/5");
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "" + e, Toast.LENGTH_LONG).show();
        }
    }

}