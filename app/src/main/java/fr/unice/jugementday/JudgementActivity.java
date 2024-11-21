package fr.unice.jugementday;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import fr.unice.jugementday.button.MenuButtons;

public class JudgementActivity extends AppCompatActivity {

    private TextView CritiqueText;
    private ImageView CritiqueImage;
    private Intent intent;
    int note = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_judgement);

        ImageButton profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> MenuButtons.profileClick(this));

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> MenuButtons.homeClick(this));

        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> MenuButtons.searchClick(this));

        CritiqueText = findViewById(R.id.CritiqueText);
        intent = getIntent();
        String title= getIntent().getStringExtra("title");
        String critique = getString(R.string.critiqueText);
        String newTitle = critique.replace("oeuvre", title);
        CritiqueText.setText(newTitle);
        CritiqueImage = findViewById(R.id.workImagePlaceholderPicture);
        CritiqueImage.setImageResource(intent.getIntExtra("photo", 0));

        onClickStarJudge();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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
            note = i;
            stars[i].setOnClickListener(v1 -> {
                // Parcourir toutes les étoiles
                for (int j = 0; j < stars.length; j++) {
                    if (j <= index) {
                        // Les étoiles sélectionnées ou en dessous : couleur secondaire
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