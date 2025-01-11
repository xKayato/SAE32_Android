package fr.unice.jugementday;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.CountDownLatch;

import fr.unice.jugementday.service.Address;
import fr.unice.jugementday.service.JsonStock;
import fr.unice.jugementday.service.UrlReader;
import fr.unice.jugementday.service.UserSessionManager;

public class LoadingActivity extends AppCompatActivity {

    private JsonStock jsonStock;
    private String userLogin;
    private UserSessionManager sessionManager;
    private UrlReader urlReader;
    private TextView loadingStatus; // Déclaration du TextView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_loading);

        jsonStock = new JsonStock(this);
        sessionManager = new UserSessionManager(this);
        urlReader = new UrlReader();
        ProgressBar progressBar = findViewById(R.id.progressBar);
        loadingStatus = findViewById(R.id.loadingStatus); // Initialisation du TextView

        if (sessionManager.isLoggedIn()) {
            userLogin = sessionManager.getLogin();
        } else {
            Intent intent = new Intent(LoadingActivity.this, LoginActivity.class);
            startActivity(intent);
            return;
        }

        // Afficher la barre de chargement
        progressBar.setVisibility(View.VISIBLE);
        loadingStatus.setText("Téléchargement des données...");

        String[] urls = {
                "&table=Oeuvre",
                "&table=User&fields=login,acces",
                "&table=User&fields=acces&login=" + userLogin,
                "&table=Avis&fields=idOeuvre,nomOeuvre,type&login=" + userLogin
        };

        CountDownLatch latch = new CountDownLatch(urls.length);

        for (String url : urls) {
            new Thread(() -> {
                String result = urlReader.fetchData(url);
                runOnUiThread(() -> {
                    if (result.startsWith("Erreur")) {
                        Toast.makeText(this, R.string.errorText, Toast.LENGTH_LONG).show();
                    } else {
                        processResult(url, result);
                    }
                    latch.countDown();
                });
            }).start();
        }

        new Thread(() -> {
            try {
                latch.await();
                // Mettre à jour l'état du chargement
                runOnUiThread(() -> loadingStatus.setText("Vérification des images..."));

                downloadImages();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Une fois les données récupérées, les traiter et les stocker dans JsonStock (Si vide, stocker "[]")
    private void processResult(String url, String result) {
        try {
            if (url.contains("table=Oeuvre")) {
                jsonStock.setWorks(result.contains("Aucune") ? "[]" : result);
            } else if (url.contains("table=User")) {
                if (url.contains("&login=" + userLogin)) {
                    JSONArray jsonArray = new JSONArray(result);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    sessionManager.setAccess(jsonObject.getString("acces"));
                } else {
                    jsonStock.setPeople(result.contains("Aucune") ? "[]" : result);
                }
            } else if (url.contains("table=Avis")) {
                jsonStock.setJudged(result.contains("Aucune") ? "[]" : result);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Télécharger les images des œuvres
    private void downloadImages() {
        new Thread(() -> {
            try {
                JSONArray worksArray = new JSONArray(jsonStock.getWorks());

                // Dossier de cache pour les images
                File cacheDir = new File(getCacheDir(), "images");
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs(); // créer le cache s'il n'existe pas
                }


                for (int i = 0; i < worksArray.length(); i++) {
                    JSONObject work = worksArray.getJSONObject(i);
                    int idOeuvre = work.getInt("idOeuvre");

                    // Vérifier si l'image est déjà en cache
                    File imageFile = new File(cacheDir, idOeuvre + ".png");
                    String nomOeuvre = work.getString("nomOeuvre");
                    if (!imageFile.exists()) {
                        // Télécharger l'image
                        String imageUrl = Address.getGetPhotoPage() + "&title=" + URLEncoder.encode(nomOeuvre.replace(" ", "_"), "UTF-8");
                        try (InputStream input = new URL(imageUrl).openStream()) {
                            Bitmap bitmap = BitmapFactory.decodeStream(input);
                            if (bitmap != null) {
                                runOnUiThread(() -> loadingStatus.setText("Téléchargement des images...\n" + nomOeuvre));
                                try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else{
                        // Mettre à jour l'état du chargement (image déjà téléchargée dans le cache)
                        runOnUiThread(() -> loadingStatus.setText("Vérification des images...\n" + nomOeuvre));
                    }

                }
                Intent intent = new Intent(LoadingActivity.this, HomeActivity.class);
                startActivity(intent);


            } catch (JSONException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
