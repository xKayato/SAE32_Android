package fr.unice.jugementday;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import fr.unice.jugementday.service.UrlSend;

public class Create_Account extends AppCompatActivity {

    private Button registerButton;
    private EditText loginField;
    private EditText pseudoField;
    private EditText passwordField;
    private EditText passwordFieldConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        registerButton = findViewById(R.id.registerButton);
        loginField = findViewById(R.id.loginFieldButton);
        pseudoField = findViewById(R.id.pseudoFieldButton);
        passwordField = findViewById(R.id.passwordFieldButton);
        passwordFieldConfirm = findViewById(R.id.passwordFieldConfirmButton);

        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String loginText = loginField.getText().toString().trim();
        String nicknameText = pseudoField.getText().toString().trim();
        String passwordText = passwordField.getText().toString().trim();
        String confirmPasswordText = passwordFieldConfirm.getText().toString().trim();

        if (!loginText.isEmpty() && !nicknameText.isEmpty() && !passwordText.isEmpty() && !confirmPasswordText.isEmpty()) {
            if (passwordText.equals(confirmPasswordText)) {
                // Hachage du mot de passe en MD5
                UrlSend urlSend = new UrlSend();
                String hashedPassword = urlSend.encryptToMD5(passwordText);

                if (hashedPassword != null) {
                    // Construire les options
                    String baseUrl = "http://10.3.122.146/importdata.php";
                    String table = "User"; // Exemple : la table cible
                    String[] options = {
                            "login=" + loginText,
                            "pseudo=" + nicknameText,
                            "mdp=" + hashedPassword,
                            "acces=1"
                    };

                    // Envoyer les données
                    new Thread(() -> {
                        String response = urlSend.sendData(baseUrl, table, options);

                        runOnUiThread(() -> {
                            if (response.startsWith("Erreur")) {
                                Toast.makeText(this, response, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "Enregistrement réussi : " + response, Toast.LENGTH_LONG).show();
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
