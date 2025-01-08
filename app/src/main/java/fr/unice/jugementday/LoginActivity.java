package fr.unice.jugementday;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import fr.unice.jugementday.service.UrlReader;
import fr.unice.jugementday.service.UserSessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText loginField;
    private EditText passwordField;
    private Button loginButton;
    private TextView signupLink;
    private UserSessionManager sessionManager;

    private final String passid = "SalutJeSuisUnMotDePassePourGet";  // Passid fixe

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new UserSessionManager(this);

        if(sessionManager.isLoggedIn()){
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
        }


        loginField = findViewById(R.id.loginFieldButton);
        passwordField = findViewById(R.id.passwordFieldButton);
        loginButton = findViewById(R.id.loginButton);
        signupLink = findViewById(R.id.createAccountButton);

        loginButton.setOnClickListener(v -> handleLogin());
        signupLink.setOnClickListener(v -> {
            // Rediriger vers l'activité de création de compte
            startActivity(new Intent(LoginActivity.this, Create_Account.class));
        });
    }

    private void handleLogin() {
        String loginText = loginField.getText().toString().trim();
        String passwordText = passwordField.getText().toString().trim();

        if (!loginText.isEmpty() && !passwordText.isEmpty()) {
            // Hachage du mot de passe en MD5
            String hashedPassword = encryptToMD5(passwordText);

            // Construire l'URL pour envoyer les données au serveur
            String baseUrl = UrlReader.address;
            String table = "User";
            String[] options = {
                    "login=" + loginText,
                    "pseudo=" + loginText,
                    "mdp=" + hashedPassword,
                    "passid=" + passid
            };

            // Envoyer les données avec UrlReader
            new Thread(() -> {
                UrlReader urlReader = new UrlReader();
                String response = urlReader.fetchData(urlReader.buildUrl(baseUrl, table, options));

                runOnUiThread(() -> {
                    if (response.contains("Erreur") || response.contains("Aucune")) {
                        Toast.makeText(this, response, Toast.LENGTH_LONG).show();
                    } else {
                        // Réponse réussie
                        Toast.makeText(this, "Connexion réussie : " + response, Toast.LENGTH_LONG).show();
                        // Redirection vers l'écran d'accueil
                        sessionManager.setLogin(loginText);
                        sessionManager.setPassword(hashedPassword);
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                    }
                });
            }).start();
        } else {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
        }
    }


    // Méthode pour hacher le mot de passe en MD5
    private String encryptToMD5(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
