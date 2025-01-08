package fr.unice.jugementday;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import fr.unice.jugementday.service.MenuButtons;
import fr.unice.jugementday.service.JsonStock;
import fr.unice.jugementday.service.UrlReader;
import fr.unice.jugementday.service.UrlSend;
import fr.unice.jugementday.service.UserSessionManager;

public class JudgementActivity extends AppCompatActivity {

    private TextView CritiqueText;
    private ImageView CritiqueImage;
    private Button publishButton;
    private Intent intent;
    private String title;
    private EditText JugementField;
    private UrlReader urlReader;
    int note = 0;
    private UserSessionManager sessionManager;
    private JsonStock jsonStock;
    private int id;
    private String judgements;
    private TextView typeText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_judgement);

        sessionManager = new UserSessionManager(this);

        JugementField = findViewById(R.id.JudgementField);

        jsonStock = new JsonStock(this);
        String Works = jsonStock.getWorks();
        judgements = jsonStock.getJudged();


        ImageButton profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> MenuButtons.profileClick(this));

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> MenuButtons.homeClick(this));

        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> MenuButtons.searchClick(this));

        ImageButton community = findViewById(R.id.communityButton);
        community.setOnClickListener(this::onClickAllJudgement);

        CritiqueText = findViewById(R.id.CritiqueText);
        intent = getIntent();
        title= getIntent().getStringExtra("title");
        String critique = getString(R.string.critiqueText);
        String newTitle = critique.replace("oeuvre", title);
        CritiqueText.setText(newTitle);
        CritiqueImage = findViewById(R.id.selectedImageButton);
        CritiqueImage.setImageResource(intent.getIntExtra("photo", 0));
        id = intent.getIntExtra("idOeuvre",0);
        typeText = findViewById(R.id.TypeText);



        try{
            JSONArray jsonArray = new JSONArray(Works);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int idOeuvre = jsonObject.getInt("idOeuvre");
                if (idOeuvre == id) {
                    typeText.setText(jsonObject.getString("type").substring(0, 1).toUpperCase() + jsonObject.getString("type").substring(1));
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }





        publishButton = findViewById(R.id.publishButton);

        publishButton.setOnClickListener(this::onClickPublish);


        onClickStarJudge();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void onClickAllJudgement(View view) {
        Intent intent2 = new Intent(this, AlljudgmentActivity.class);
        intent2.putExtra("title", title);
        intent2.putExtra("idOeuvre", id);

        startActivity(intent2);

    }

    public void onClickPublish(View view) {
        try {
            /*
            JSONArray jsonArray = new JSONArray(judgements);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int idOeuvre = jsonObject.getInt("idOeuvre");
                if (idOeuvre == id) {
                    Toast.makeText(this, "Vous avez déjà donné votre avis sur cette oeuvre", Toast.LENGTH_LONG).show();
                    Intent intent3 = new Intent(this, HomeActivity.class);
                    startActivity(intent3);
                    return;
                }
            }

             */
            UrlSend urlSend = new UrlSend();
            String table = "Avis";
            String[] options = {
                    "idOeuvre=" + id,
                    "texteAvis=" + JugementField.getText().toString(),
                    "note=" + note,
                    "date=" + new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()),
                    "login=" + sessionManager.getLogin()
            };

            // Envoyer les données
            new Thread(() -> {
                String response = urlSend.sendData(table, options);

                runOnUiThread(() -> {
                    if (response.startsWith("Erreur")) {
                        Toast.makeText(this, R.string.errorText, Toast.LENGTH_LONG).show();
                    }
                });
            }).start();
            Intent intent3 = new Intent(this, StartingActivity.class);
            startActivity(intent3);
            Toast.makeText(this, "Jugement ajouté !", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, ""+e, Toast.LENGTH_LONG).show();
        }



    }

    public void onClickStarJudge() {
        // Récupérer les boutons
        ImageButton starButton1 = findViewById(R.id.ratingStar1Button);
        ImageButton starButton2 = findViewById(R.id.ratingStar2Button);
        ImageButton starButton3 = findViewById(R.id.ratingStar3Button);
        ImageButton starButton4 = findViewById(R.id.ratingStar4Button);
        ImageButton starButton5 = findViewById(R.id.ratingStar5Button);

        // Liste des boutons pour plus de flexibilité
        ImageButton[] stars = {starButton1, starButton2, starButton3, starButton4, starButton5};

        // Ajouter un clic à chaque étoile
        for (int i = 0; i < stars.length; i++) {
            int index = i; // Capturer l'index pour l'utiliser dans le Listener

            stars[i].setOnClickListener(v1 -> {
                // Parcourir toutes les étoiles
                for (int j = 0; j < stars.length; j++) {
                    if (j <= index) {
                        // Les étoiles sélectionnées ou en dessous : couleur secondaire
                        note = j+1;
                        stars[j].setColorFilter(getResources().getColor(R.color.secondary_button));
                    } else {
                        // Les étoiles non sélectionnées : couleur blanche
                        stars[j].setColorFilter(getResources().getColor(R.color.white));
                    }
                }
            });
        }
    }
}