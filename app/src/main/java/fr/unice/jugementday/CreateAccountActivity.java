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

    private Button registerButton;
    private EditText loginField;
    private EditText passwordField;
    private EditText passwordFieldConfirm;
    private UserSessionManager sessionManager;
    private JsonStock jsonStock;
    private String People;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        sessionManager = new UserSessionManager(this);

        if(sessionManager.isLoggedIn()){
            Intent intent = new Intent(CreateAccountActivity.this, HomeActivity.class);
            startActivity(intent);
        }

        jsonStock = new JsonStock(this);
        People = jsonStock.getPeople();

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
        boolean exist = false;

        try{
            // Vérifier si le login existe déjà
            JSONArray jsonArray = new JSONArray(People);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String loginPeople = jsonObject.getString("login");
                if (loginPeople.toLowerCase().equals(loginText.toLowerCase())) {
                    Toast.makeText(this, getString(R.string.login_already_exists_error), Toast.LENGTH_SHORT).show();
                    // Vider les champs
                    loginField.setText("");
                    passwordField.setText("");
                    passwordFieldConfirm.setText("");
                    exist = true;
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Si le login n'existe pas
        if(!exist){
            // Vérifier que les champs sont valides
            if (!loginText.matches("^[a-zA-Z0-9]+$")) {
                Toast.makeText(this, getString(R.string.invalid_login_error), Toast.LENGTH_SHORT).show();
                return;
            }

            if (!passwordText.matches("^[a-zA-Z0-9]+$")) {
                Toast.makeText(this, getString(R.string.invalid_password_error), Toast.LENGTH_SHORT).show();
                return;
            }

            if (loginText.length() >= 20) {
                Toast.makeText(this, getString(R.string.login_too_long_error), Toast.LENGTH_SHORT).show();
                return;
            }

            if (passwordText.length() >= 20) {
                Toast.makeText(this, getString(R.string.password_too_long_error), Toast.LENGTH_SHORT).show();
                return;
            }



            // Vérifier que les champs sont remplis
            if (!loginText.isEmpty() && !passwordText.isEmpty() && !confirmPasswordText.isEmpty()) {
                // Vérifier que les mots de passe correspondent
                if (passwordText.equals(confirmPasswordText)) {
                    UrlSend urlSend = new UrlSend();
                    // Chiffrer le mot de passe en MD5
                    String hashedPassword = urlSend.encryptToMD5(passwordText);

                    if (hashedPassword != null) {
                        String table = "User";
                        String[] options = {
                                "login=" + loginText,
                                "mdp=" + hashedPassword,
                                "acces=0"
                        };

                        // Envoyer les données au serveur
                        new Thread(() -> {
                            String response = urlSend.sendData(table, options);

                            runOnUiThread(() -> {
                                if (response.startsWith("Erreur")) {
                                    Toast.makeText(this, R.string.errorText, Toast.LENGTH_LONG).show();
                                } else {
                                    sessionManager.setLogin(loginText);
                                    sessionManager.setPassword(hashedPassword);
                                    Intent intent = new Intent(this, StartingActivity.class);
                                    startActivity(intent);
                                }
                            });
                        }).start();
                    } else {
                        Toast.makeText(this, getString(R.string.password_hash_error), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.password_mismatch_error), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getString(R.string.fill_fields_error), Toast.LENGTH_SHORT).show();
            }
        }




    }
}
