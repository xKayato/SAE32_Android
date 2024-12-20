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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import fr.unice.jugementday.button.MenuButtons;
import fr.unice.jugementday.service.UrlSend;
import fr.unice.jugementday.service.UserSessionManager;

public class JudgementActivity extends AppCompatActivity {

    private TextView CritiqueText;
    private ImageView CritiqueImage;
    private Button publishButton;
    private Intent intent;
    private String title;
    private EditText JugementField;
    int note = 0;
    private UserSessionManager sessionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_judgement);

        sessionManager = new UserSessionManager(this);

        JugementField = findViewById(R.id.JudgementField);

        ImageButton profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> MenuButtons.profileClick(this));

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> MenuButtons.homeClick(this));

        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> MenuButtons.searchClick(this));

        CritiqueText = findViewById(R.id.CritiqueText);
        intent = getIntent();
        title= getIntent().getStringExtra("title");
        String critique = getString(R.string.critiqueText);
        String newTitle = critique.replace("oeuvre", title);
        CritiqueText.setText(newTitle);
        CritiqueImage = findViewById(R.id.workImagePlaceholderPicture);
        CritiqueImage.setImageResource(intent.getIntExtra("photo", 0));


        publishButton = findViewById(R.id.publishButton);

        publishButton.setOnClickListener(v -> onClickPublish(v));


        onClickStarJudge();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void onClickPublish(View view) {
        UrlSend urlSend = new UrlSend();
        String baseUrl = "http://10.3.122.146/importdata.php";
        String table = "Avis";
        String[] options = {
                "idOeuvre=" + title,
                "texteAvis=" + JugementField.getText().toString(),
                "note=" + note,
                "date=" + new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()),
                "login=" + sessionManager.getLogin()
        };

        // Envoyer les données
        new Thread(() -> {
            String response = urlSend.sendData(baseUrl, table, options);

            runOnUiThread(() -> {
                if (response.startsWith("Erreur")) {
                    Toast.makeText(this, response, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Données envoyées : " + response, Toast.LENGTH_LONG).show();
                }
            });
        }).start();
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