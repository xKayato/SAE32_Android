package fr.unice.jugementday;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import fr.unice.jugementday.service.MenuButtons;
import fr.unice.jugementday.service.UrlUpdate;
import fr.unice.jugementday.service.UserSessionManager;

public class activitySettingsAccount extends AppCompatActivity {

    private ImageButton disconnectButton;
    private UserSessionManager sessionManager;
    private Button changeLoginButton;
    private EditText newLogin;
    private UrlUpdate urlUpdate;
    private String login;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings_account);
        sessionManager = new UserSessionManager(this);

        urlUpdate = new UrlUpdate();

        login = sessionManager.getLogin();

        disconnectButton = findViewById(R.id.disconnectButton);
        disconnectButton.setOnClickListener(v -> { disconnectUser();});

        newLogin = findViewById(R.id.usernameFieldButton);
        changeLoginButton = findViewById(R.id.changeUsernameButton);

        changeLoginButton.setOnClickListener(v -> { changeLogin();});

        // Bouton pour aller sur la page profile
        ImageButton profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> MenuButtons.profileClick(this));
        // Bouton pour aller sur la page Accueil
        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> MenuButtons.homeClick(this));
        // Bouton pour aller sur la page Rechercher
        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> MenuButtons.searchClick(this));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void changeLogin() {

        String newLoginText = newLogin.getText().toString();

        String table = "User";
        String[] options = {
                "login=" + login,
                "newlogin=" + newLoginText
        };


        new Thread(() -> {
            String response = urlUpdate.updateData(table, options);
            runOnUiThread(() -> {
                if (response.startsWith("Erreur")) {
                    Toast.makeText(this, response, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Données update : " + response, Toast.LENGTH_LONG).show();
                    sessionManager.setLogin(newLoginText);
                    Intent intent = new Intent(activitySettingsAccount.this, StartingActivity.class);
                    startActivity(intent);

                }
            });
        }).start();

    }

    // Fonction pour déconnecter l'utilisateur
    private void disconnectUser() {
        // Supprimer les données de session
        sessionManager.logout();
        // Rediriger vers la page de connexion
        Intent intent = new Intent(activitySettingsAccount.this, LoginActivity.class);
        startActivity(intent);
    }
}