package fr.unice.jugementday;

import android.content.Intent;
import android.os.Bundle;
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
    private UserSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new UserSessionManager(this);

        if(sessionManager.isLoggedIn()){
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }


        loginField = findViewById(R.id.loginFieldButton);
        passwordField = findViewById(R.id.passwordFieldButton);
        Button loginButton = findViewById(R.id.loginButton);
        TextView signupLink = findViewById(R.id.createAccountButton);

        loginButton.setOnClickListener(v -> handleLogin());
        signupLink.setOnClickListener(v -> {
            // Rediriger vers l'activité de création de compte
            startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
        });

    }

    /**
     * Affiche un message toast
     * @param messageId
     */
    private void showToast(int messageId) {
        Toast.makeText(this, getString(messageId), Toast.LENGTH_SHORT).show();
    }

    /**
     * Gestion de la connexion
     */
    private void handleLogin() {
        String loginText = loginField.getText().toString().trim();
        String passwordText = passwordField.getText().toString().trim();

        if (!loginText.isEmpty() && !passwordText.isEmpty()) {
            // Hachage du mot de passe en MD5
            String hashedPassword = encryptToMD5(passwordText);

            // Construire l'URL pour envoyer les données au serveur
            String options = "&table=User&login=" + loginText + "&mdp=" + hashedPassword;


            // Envoyer les données avec UrlReader
            new Thread(() -> {
                UrlReader urlReader = new UrlReader();
                String response = urlReader.fetchData(options);

                runOnUiThread(() -> {
                    if (response.contains("Erreur") || response.contains("Aucune")) {
                        showToast(R.string.errorText);
                    } else {
                        // Redirection vers l'écran d'accueil
                        sessionManager.setLogin(loginText);
                        sessionManager.setPassword(hashedPassword);
                        Intent intent = new Intent(LoginActivity.this, LoadingActivity.class);
                        startActivity(intent);
                    }
                });
            }).start();
        } else {
            showToast(R.string.fill_fields_error);
        }
    }


    /**
     * Hachage du mot de passe en MD5
     * @param password le mot de passe à hacher
     * @return le mot de passe haché
     */
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
