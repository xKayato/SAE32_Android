package fr.unice.jugementday;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import fr.unice.jugementday.service.CustomArrayAdapter;
import fr.unice.jugementday.service.MenuButtons;
import fr.unice.jugementday.service.JsonStock;
import fr.unice.jugementday.service.UrlReader;
import fr.unice.jugementday.service.UrlSend;
import fr.unice.jugementday.service.UserSessionManager;

public class JudgementActivity extends AppCompatActivity {

    private String title;
    private EditText JugementField;
    int note = 0;
    private UserSessionManager sessionManager;
    private int id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_judgement);

        sessionManager = new UserSessionManager(this);
        String userLogin = sessionManager.getLogin();

        checkUserSession();


        JugementField = findViewById(R.id.JudgementField);

        JsonStock jsonStock = new JsonStock(this);
        String Works = jsonStock.getWorks();
        String judgements = jsonStock.getJudged();

        Button publishButton = findViewById(R.id.publishButton);

        publishButton.setOnClickListener(this::onClickPublish);

        setupMenuButtons();

        ImageButton community = findViewById(R.id.communityButton);
        community.setOnClickListener(this::onClickAllJudgement);

        TextView critiqueText = findViewById(R.id.JudgementOfText);
        Intent intent = getIntent();
        title= getIntent().getStringExtra("title");
        String critique = getString(R.string.critiqueText);
        String newTitle = critique.replace("oeuvre", title);
        critiqueText.setText(newTitle);
        id = intent.getIntExtra("idOeuvre",0);
        TextView typeText = findViewById(R.id.typeText);
        TextView auteurText = findViewById(R.id.auteurText);
        TextView dateText = findViewById(R.id.dateText);
        ImageView selectedImageButton = findViewById(R.id.selectedImageButton);
        selectedImageButton.setImageBitmap(getImageFromCache(id));

        try{
            JSONArray jsonArray = new JSONArray(judgements);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int idOeuvre = jsonObject.getInt("idOeuvre");
                if (idOeuvre == id) {
                    Intent intent3 = new Intent(this, CheckJudgementActivity.class);
                    intent3.putExtra("idOeuvre", idOeuvre);
                    intent3.putExtra("title", title);
                    intent3.putExtra("login", userLogin);

                    startActivity(intent3);
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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





        onClickStarJudge();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private Bitmap getImageFromCache(int idOeuvre) {
        // Accéder au répertoire images dans le cache
        File cacheDir = new File(getCacheDir(), "images");
        File imageFile = new File(cacheDir, idOeuvre + ".png");

        if (imageFile.exists()) {
            // Si le fichier d'image existe, charger l'image en tant que Bitmap
            return BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        }

        return null; // Retourner null si l'image n'existe pas
    }


    /**
     * Vérifie si l'utilisateur est connecté et le redirige si nécessaire.
     */
    private void checkUserSession() {
        UserSessionManager sessionManager = new UserSessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * Configure les boutons de navigation.
     */
    private void setupMenuButtons() {
        ImageButton profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> MenuButtons.profileClick(this));

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> MenuButtons.homeClick(this));

        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> MenuButtons.searchClick(this));
    }

    /**
     * Rediriger vers la page de tous les avis
     * @param view Vue actuelle
     */
    public void onClickAllJudgement(View view) {
        Intent intent2 = new Intent(this, AlljudgmentActivity.class);
        intent2.putExtra("title", title);
        intent2.putExtra("idOeuvre", id);

        startActivity(intent2);

    }

    /**
     * Afficher un message toast
     * @param messageId ID du message
     */
    private void showToast(int messageId) {
        Toast.makeText(this, getString(messageId), Toast.LENGTH_SHORT).show();
    }

    /**
     * Ajouter un avis sur une œuvre
     * @param view
     */
    public void onClickPublish(View view) {
        if(JugementField.getText().toString().length() <= 150) {
            try {
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
                            showToast(R.string.errorText);
                        }
                    });
                }).start();
                Intent intent3 = new Intent(this, LoadingActivity.class);
                intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent3);
                showToast(R.string.addedJudgementText);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showToast(R.string.maxCharJudgement);
        }
    }

    /**
     * Gestion des étoiles de notation
     */
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
                if (note == index + 1) {
                    // Si on clique sur la plus grande étoile sélectionnée, remettre à 0
                    note = 0;
                    for (ImageButton star : stars) {
                        star.clearColorFilter(); // Réinitialiser toutes les étoiles
                    }
                } else {
                    // Parcourir toutes les étoiles pour mettre à jour les couleurs
                    for (int j = 0; j < stars.length; j++) {
                        if (j <= index) {
                            // Les étoiles sélectionnées ou en dessous : couleur secondaire
                            stars[j].setColorFilter(getResources().getColor(R.color.secondary_button));
                        } else {
                            // Les étoiles non sélectionnées : reset de couleur
                            stars[j].clearColorFilter();
                        }
                    }
                    // Mettre à jour la note
                    note = index + 1;
                }
            });
        }
    }

}