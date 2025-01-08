package fr.unice.jugementday;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import fr.unice.jugementday.service.JsonStock;
import fr.unice.jugementday.service.UrlReader;
import fr.unice.jugementday.service.UserSessionManager;

public class StartingActivity extends AppCompatActivity {

    private JsonStock jsonStock;
    private String userLogin;
    private UserSessionManager sessionManager;
    private UrlReader urlReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        jsonStock = new JsonStock(this);
        sessionManager = new UserSessionManager(this);
        urlReader = new UrlReader();

        // Vérifier si l'utilisateur est connecté
        if (sessionManager.isLoggedIn()) {
            userLogin = sessionManager.getLogin();
        } else {
            // Si l'utilisateur n'est pas connecté, rediriger vers la page de connexion
            Intent intent = new Intent(StartingActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        List<String> urls = new ArrayList<>();
        urls.add(UrlReader.address + "?table=Oeuvre");
        urls.add(UrlReader.address + "?table=User&fields=login,mdp,acces");
        urls.add(UrlReader.address + "?table=User&fields=acces&login=" + userLogin);

        urls.add(UrlReader.address + "?table=Avis&fields=idOeuvre,nomOeuvre,type&login=" + userLogin);


        CountDownLatch latch = new CountDownLatch(urls.size());


        // Faire un temps de chargement et attendre que tout soit téléchargé avant de lancer l'activité
        // Synchroniser les threads
        for (String url : urls) {
            new Thread(() -> {
                String result = urlReader.fetchData(url);
                runOnUiThread(() -> {
                    if (result.startsWith("Erreur")) {
                        Toast.makeText(this, R.string.errorText, Toast.LENGTH_LONG).show();
                    } else {
                        if (url.contains("table=Oeuvre")) {
                            if(!result.contains("Aucune")){
                                jsonStock.setWorks(result);
                            } else {
                                jsonStock.setWorks("[]");
                            }
                        }
                        if (url.contains("table=User")) {
                            if(!result.contains("Aucune")) {
                                if (url.contains("&login=" + userLogin)) {

                                    try {
                                        // Convertir la réponse en JSONArray
                                        JSONArray jsonArray = new JSONArray(result);
                                        // Accéder au premier élément du tableau (ici, index 0)
                                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                                        // Extraire la valeur de "acces" et la stocker dans la session
                                        sessionManager.setAccess(jsonObject.getString("acces"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();  // Log de l'exception
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    jsonStock.setPeople(result);
                                }
                            } else {
                                jsonStock.setPeople("[]");
                            }

                    }
                        if (url.contains("table=Avis")) {
                            if(!result.contains("Aucune")) {
                                jsonStock.setJudged(result);
                            } else {
                                jsonStock.setJudged("[]");
                            }
                        }
                    }
                    latch.countDown();
                });
            }).start();
        }

        new Thread(() -> {
            try {
                latch.await();
                runOnUiThread(() -> {
                    Intent intent = new Intent(StartingActivity.this, HomeActivity.class);
                    startActivity(intent);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();



        setContentView(R.layout.activity_starting);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}