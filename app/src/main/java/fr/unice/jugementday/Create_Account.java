package fr.unice.jugementday;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import fr.unice.jugementday.service.UrlSend;
import fr.unice.jugementday.service.UserSessionManager;

public class Create_Account extends AppCompatActivity {

    private Button registerButton;
    private EditText loginField;
    private EditText passwordField;
    private EditText passwordFieldConfirm;
    private UserSessionManager sessionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        sessionManager = new UserSessionManager(this);

        registerButton = findViewById(R.id.registerButton);
        loginField = findViewById(R.id.loginFieldButton);
        passwordField = findViewById(R.id.passwordFieldButton);
        passwordFieldConfirm = findViewById(R.id.passwordFieldConfirmButton);

        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String loginText = loginField.getText().toString().trim();
        String passwordText = passwordField.getText().toString().trim();
        String confirmPasswordText = passwordFieldConfirm.getText().toString().trim();

        if (!loginText.isEmpty() && !passwordText.isEmpty() && !confirmPasswordText.isEmpty()) {
            if (passwordText.equals(confirmPasswordText)) {
                // Hachage du mot de passe en MD5
                UrlSend urlSend = new UrlSend();
                String hashedPassword = urlSend.encryptToMD5(passwordText);

                if (hashedPassword != null) {
                    // Construire les options
                    String table = "User"; // Exemple : la table cible
                    String[] options = {
                            "login=" + loginText,
                            "mdp=" + hashedPassword,
                            "acces=1"
                    };

                    // Envoyer les données
                    new Thread(() -> {
                        String response = urlSend.sendData(table, options);

                        runOnUiThread(() -> {
                            if (response.startsWith("Erreur")) {
                                Toast.makeText(this, response, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "Enregistrement réussi : " + response, Toast.LENGTH_LONG).show();
                                sessionManager.setLogin(loginText);
                                Intent intent = new Intent(this, HomeActivity.class);
                                startActivity(intent);
                            }
                        });
                    }).start();
                } else {
                    Toast.makeText(this, "Erreur de hachage du mot de passe", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
        }
    }
}
