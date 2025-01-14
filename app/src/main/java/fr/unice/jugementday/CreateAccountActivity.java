package fr.unice.jugementday;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import fr.unice.jugementday.service.JsonStock;
import fr.unice.jugementday.service.UrlSend;
import fr.unice.jugementday.service.UserSessionManager;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText loginField;
    private EditText passwordField;
    private EditText passwordFieldConfirm;
    private UserSessionManager sessionManager;
    private String People;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // Initialisation des composants et vérification de la session utilisateur
        sessionManager = new UserSessionManager(this);
        checkUserSession();

        // Récupération des données utilisateurs (personnes)
        JsonStock jsonStock = new JsonStock(this);
        People = jsonStock.getPeople();

        // Configuration de l'interface utilisateur
        setupUI();
    }

    /**
     * Vérifie si un utilisateur est déjà connecté.
     * Si l'utilisateur est connecté, il est redirigé vers l'écran d'accueil.
     */
    private void checkUserSession() {
        if (sessionManager.isLoggedIn()) {
            Intent intent = new Intent(CreateAccountActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    /**
     * Configure les éléments de l'interface utilisateur : champs de saisie et bouton.
     */
    private void setupUI() {
        Button registerButton = findViewById(R.id.registerButton);
        loginField = findViewById(R.id.loginFieldButton);
        passwordField = findViewById(R.id.passwordFieldButton);
        passwordFieldConfirm = findViewById(R.id.passwordFieldConfirmButton);

        // Gestion de l'événement pour le bouton d'inscription
        registerButton.setOnClickListener(v -> registerUser());
    }

    /**
     * Enregistre un nouvel utilisateur en vérifiant la validité des informations saisies.
     * Si les données sont valides, l'utilisateur est inscrit et les informations sont envoyées au serveur.
     */
    private void registerUser() {
        String loginText = loginField.getText().toString().trim();
        String passwordText = passwordField.getText().toString().trim();
        String confirmPasswordText = passwordFieldConfirm.getText().toString().trim();
        boolean exist = false;

        try {
            // Vérification si le login existe déjà
            exist = checkIfLoginExists(loginText);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Si le login n'existe pas
        if (!exist) {
            // Vérification des champs et des conditions de validité
            if (validateFields(loginText, passwordText, confirmPasswordText)) {
                // Vérification que les mots de passe correspondent
                if (passwordText.equals(confirmPasswordText)) {
                    // Chiffrement du mot de passe et envoi au serveur
                    registerOnServer(loginText, passwordText);
                } else {
                    showToast(R.string.password_mismatch_error);
                }
            }
        }
    }

    private void showToast(int messageId) {
        Toast.makeText(this, getString(messageId), Toast.LENGTH_SHORT).show();
    }


    /**
     * Vérifie si le login existe déjà parmi les utilisateurs existants.
     * @param loginText Le login à vérifier.
     * @return true si le login existe déjà, false sinon.
     */
    private boolean checkIfLoginExists(String loginText) {
        boolean exists = false;
        try {
            JSONArray jsonArray = new JSONArray(People);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String loginPeople = jsonObject.getString("login");
                if (loginPeople.equalsIgnoreCase(loginText)) {
                    showToast(R.string.login_already_exists_error);
                    clearFields();  // Vider les champs en cas d'erreur
                    exists = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exists;
    }

    /**
     * Vide les champs de saisie de l'utilisateur après une erreur.
     */
    private void clearFields() {
        loginField.setText("");
        passwordField.setText("");
        passwordFieldConfirm.setText("");
    }

    /**
     * Valide les champs du formulaire : login, mot de passe et confirmation.
     * @param loginText Le login de l'utilisateur.
     * @param passwordText Le mot de passe de l'utilisateur.
     * @param confirmPasswordText La confirmation du mot de passe.
     * @return true si les champs sont valides, false sinon.
     */
    private boolean validateFields(String loginText, String passwordText, String confirmPasswordText) {
        // Vérification des règles de validité pour le login et le mot de passe
        if (!loginText.matches("^[a-zA-Z0-9]+$")) {
            showToast(R.string.invalid_login_error);
            return false;
        }

        if (!passwordText.matches("^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]+$")) {
            showToast(R.string.invalid_password_error);
            return false;
        }

        if (loginText.length() >= 20) {
            showToast(R.string.login_too_long_error);
            return false;
        }

        if (passwordText.length() >= 20) {
            showToast(R.string.password_too_long_error);
            return false;
        }

        // Vérification que les champs ne sont pas vides
        if (loginText.isEmpty() || passwordText.isEmpty() || confirmPasswordText.isEmpty()) {
            showToast(R.string.fill_fields_error);
            return false;
        }

        return true;
    }

    /**
     * Enregistre l'utilisateur sur le serveur après chiffrement du mot de passe.
     * @param loginText Le login de l'utilisateur.
     * @param passwordText Le mot de passe de l'utilisateur.
     */
    private void registerOnServer(String loginText, String passwordText) {
        UrlSend urlSend = new UrlSend();
        // Chiffrement du mot de passe en MD5
        String hashedPassword = urlSend.encryptToMD5(passwordText);

        if (hashedPassword != null) {
            String table = "User";
            String[] options = {
                    "login=" + loginText,
                    "mdp=" + hashedPassword,
                    "acces=0"
            };

            // Envoi des données au serveur dans un thread séparé
            new Thread(() -> {
                String response = urlSend.sendData(table, options);

                runOnUiThread(() -> {
                    if (response.startsWith("Erreur")) {
                        showToast(R.string.errorText);
                    } else {
                        // Enregistrement de la session utilisateur et redirection vers l'écran de chargement
                        sessionManager.setLogin(loginText);
                        sessionManager.setPassword(hashedPassword);
                        Intent intent = new Intent(this, LoadingActivity.class);
                        startActivity(intent);
                    }
                });
            }).start();
        } else {
            showToast(R.string.password_hash_error);
        }
    }
}
