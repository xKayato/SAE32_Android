package fr.unice.jugementday;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;

import fr.unice.jugementday.service.JsonStock;
import fr.unice.jugementday.service.MenuButtons;
import fr.unice.jugementday.service.UrlDelete;
import fr.unice.jugementday.service.UrlUpdate;
import fr.unice.jugementday.service.UserSessionManager;

public class activitySettingsAccount extends AppCompatActivity {

    private UserSessionManager sessionManager;
    private EditText newLogin;
    private UrlUpdate urlUpdate;
    private UrlDelete urlDelete;
    private String login;
    private EditText oldPassword;
    private EditText newPassword;
    private String peoplesJson;
    private Button deleteAccountButton;
    private Button confirmDeleteButton;
    private Button cancelDeleteButton;
    private TextView deleteAccountConfirmText;
    private View deleteAccountView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings_account);
        sessionManager = new UserSessionManager(this);

        urlUpdate = new UrlUpdate();
        urlDelete = new UrlDelete();

        login = sessionManager.getLogin();

        ImageButton disconnectButton = findViewById(R.id.disconnectButton);
        disconnectButton.setOnClickListener(v -> { disconnectUser();});

        newLogin = findViewById(R.id.usernameFieldButton);
        Button changeLoginButton = findViewById(R.id.changeUsernameButton);
        changeLoginButton.setOnClickListener(v -> { changeLogin();});

        oldPassword = findViewById(R.id.oldPasswordFieldButton);

        newPassword = findViewById(R.id.newPasswordFieldButton);


        Button changePasswordButton = findViewById(R.id.changePasswordButton);

        changePasswordButton.setOnClickListener(v -> { changePassword();});

        confirmDeleteButton = findViewById(R.id.confirmDeleteAccountButton);
        cancelDeleteButton = findViewById(R.id.cancelDeleteAccountButton);
        deleteAccountConfirmText = findViewById(R.id.confirmDeleteAccountText);
        deleteAccountView = findViewById(R.id.deleteAccountView);

        deleteAccountButton = findViewById(R.id.deleteAccountButton);

        deleteAccountButton.setOnClickListener(v -> { askDeleteAccount();});

        JsonStock jsonStock = new JsonStock(this);
        peoplesJson = jsonStock.getPeople();



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

        if (!newLoginText.matches("^[a-zA-Z0-9]+$")) {
            Toast.makeText(this, getString(R.string.invalid_login_error), Toast.LENGTH_SHORT).show();
            return;
        }

        if (newLoginText.length() >= 20) {
            Toast.makeText(this, getString(R.string.login_too_long_error), Toast.LENGTH_SHORT).show();
            return;
        }

        try{
            JSONArray jsonArray = new JSONArray(peoplesJson);

            // Vérifier si le login existe déjà
            for (int i = 0; i < jsonArray.length(); i++) {
                String login = jsonArray.getJSONObject(i).getString("login");
                if (login.equalsIgnoreCase(newLoginText)) {
                    Toast.makeText(this, "Ce login est déjà utilisé", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        } catch (Exception e){
            Toast.makeText(this, "Erreur lors de la modification du login", Toast.LENGTH_LONG).show();
        }



        String table = "User";
        String[] options = {
                "login=" + login,
                "newlogin=" + newLoginText
        };


        new Thread(() -> {
            String response = urlUpdate.updateData(table, options);
            runOnUiThread(() -> {
                if (response.startsWith("Erreur")) {
                } else {
                    sessionManager.setLogin(newLoginText);
                    Intent intent = new Intent(activitySettingsAccount.this, StartingActivity.class);
                    startActivity(intent);

                }
            });
        }).start();

        String table2 = "Avis";
        String[] options2 = {
                "login=" + login,
                "newlogin=" + newLoginText
        };

        new Thread(() -> {
            String response = urlUpdate.updateData(table2, options2);
            runOnUiThread(() -> {
                if (response.startsWith("Erreur")) {
                    Toast.makeText(this, R.string.errorText, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, R.string.changePseudoSuccessText, Toast.LENGTH_LONG).show();
                    sessionManager.setLogin(newLoginText);
                    Intent intent = new Intent(activitySettingsAccount.this, StartingActivity.class);
                    startActivity(intent);

                }
            });
        }).start();

    }

    private void changePassword(){
        String oldPasswordText = oldPassword.getText().toString();
        String newPasswordText = newPassword.getText().toString();

        if (!newPasswordText.matches("^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]+$")) {
            Toast.makeText(this, getString(R.string.invalid_password_error), Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPasswordText.length() >= 20) {
            Toast.makeText(this, getString(R.string.password_too_long_error), Toast.LENGTH_SHORT).show();
            return;
        }

        String newPasswordTextEncrypted = urlUpdate.encryptToMD5(newPasswordText);
        String oldPasswordTextEncrypted = urlUpdate.encryptToMD5(oldPasswordText);

        String checkPasswordText = sessionManager.getPassword();





        if(checkPasswordText.equals(oldPasswordTextEncrypted)){
                String table = "User";
                String[] options = {
                        "login=" + login,
                        "newmdp=" + newPasswordTextEncrypted
                };

                new Thread(() -> {
                    String response = urlUpdate.updateData(table, options);
                    runOnUiThread(() -> {
                        if (response.startsWith("Erreur")) {
                            Toast.makeText(this, R.string.errorText, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, R.string.changePasswordSuccessText, Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(activitySettingsAccount.this, StartingActivity.class);
                            startActivity(intent);

                        }
                    });
                }).start();
            } else {
                Toast.makeText(this, "Mauvais mot de passe.", Toast.LENGTH_LONG).show();

            }


    }

    public void askDeleteAccount(){
        confirmDeleteButton.setVisibility(View.VISIBLE);
        cancelDeleteButton.setVisibility(View.VISIBLE);
        deleteAccountConfirmText.setVisibility(View.VISIBLE);
        deleteAccountView.setVisibility(View.VISIBLE);

        confirmDeleteButton.setOnClickListener(v -> { deleteAccount();});
        cancelDeleteButton.setOnClickListener(v -> { cancelDeleteAccount();});

    }

    public void cancelDeleteAccount(){
        confirmDeleteButton.setVisibility(View.GONE);
        cancelDeleteButton.setVisibility(View.GONE);
        deleteAccountConfirmText.setVisibility(View.GONE);
        deleteAccountView.setVisibility(View.GONE);
    }

    public void deleteAccount(){
        String table = "User";
        String[] options = {
                "login=" + login,
        };

        new Thread(() -> {
            String response = urlDelete.deleteData(table, options);
            runOnUiThread(() -> {
                if (response.startsWith("Erreur")) {
                    Toast.makeText(this, R.string.deleteAccountFailText, Toast.LENGTH_LONG).show();
                } else {
                    sessionManager.logout();
                    Toast.makeText(this, R.string.deleteAccountSuccessText, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(activitySettingsAccount.this, LoginActivity.class);
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