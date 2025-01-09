package fr.unice.jugementday;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import fr.unice.jugementday.HomeActivity;
import fr.unice.jugementday.R;
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

        if (!loginText.isEmpty() && !passwordText.isEmpty() && !confirmPasswordText.isEmpty()) {
            if (passwordText.equals(confirmPasswordText)) {
                UrlSend urlSend = new UrlSend();
                String hashedPassword = urlSend.encryptToMD5(passwordText);

                if (hashedPassword != null) {
                    String table = "User";
                    String[] options = {
                            "login=" + loginText,
                            "mdp=" + hashedPassword,
                            "acces=0"
                    };

                    new Thread(() -> {
                        String response = urlSend.sendData(table, options);

                        runOnUiThread(() -> {
                            if (response.startsWith("Erreur")) {
                                Toast.makeText(this, response, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, response, Toast.LENGTH_LONG).show();
                                sessionManager.setLogin(loginText);
                                sessionManager.setPassword(hashedPassword);
                                Intent intent = new Intent(this, HomeActivity.class);
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
