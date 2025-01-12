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

public class AccountSettingsActivity extends AppCompatActivity {

    private UserSessionManager sessionManager;
    private EditText newLogin;
    private UrlUpdate urlUpdate;
    private UrlDelete urlDelete;
    private String login;
    private EditText oldPassword;
    private EditText newPassword;
    private String peoplesJson;
    private Button confirmDeleteButton;
    private Button cancelDeleteButton;
    private TextView deleteAccountConfirmText;
    private View deleteAccountView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings_account);

        initializeServices();
        checkUserLogin();

        setupUIComponents();
        configureNavigationButtons();
        configureWindowInsets();
    }

    private void initializeServices() {
        urlUpdate = new UrlUpdate();
        urlDelete = new UrlDelete();
        sessionManager = new UserSessionManager(this);
        login = sessionManager.getLogin();
        JsonStock jsonStock = new JsonStock(this);
        peoplesJson = jsonStock.getPeople();
    }

    private void checkUserLogin() {
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    private void setupUIComponents() {
        newLogin = findViewById(R.id.usernameFieldButton);
        oldPassword = findViewById(R.id.oldPasswordFieldButton);
        newPassword = findViewById(R.id.newPasswordFieldButton);

        setupChangeLoginButton();
        setupChangePasswordButton();
        setupDeleteAccountButtons();
        setupDisconnectButton();
    }

    private void setupChangeLoginButton() {
        Button changeLoginButton = findViewById(R.id.changeUsernameButton);
        changeLoginButton.setOnClickListener(v -> changeLogin());
    }

    private void setupChangePasswordButton() {
        Button changePasswordButton = findViewById(R.id.changePasswordButton);
        changePasswordButton.setOnClickListener(v -> changePassword());
    }

    private void setupDeleteAccountButtons() {
        confirmDeleteButton = findViewById(R.id.confirmDeleteAccountButton);
        cancelDeleteButton = findViewById(R.id.cancelDeleteAccountButton);
        deleteAccountConfirmText = findViewById(R.id.confirmDeleteAccountText);
        deleteAccountView = findViewById(R.id.deleteAccountView);

        Button deleteAccountButton = findViewById(R.id.deleteAccountButton);
        deleteAccountButton.setOnClickListener(v -> askDeleteAccount());
    }

    private void setupDisconnectButton() {
        ImageButton disconnectButton = findViewById(R.id.disconnectButton);
        disconnectButton.setOnClickListener(v -> disconnectUser());
    }

    private void configureNavigationButtons() {
        ImageButton profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> MenuButtons.profileClick(this));

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> MenuButtons.homeClick(this));

        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> MenuButtons.searchClick(this));
    }

    private void configureWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void changeLogin() {
        String newLoginText = newLogin.getText().toString();
        if (!isValidLogin(newLoginText)) return;

        if (isLoginAlreadyExists(newLoginText)) return;

        updateLogin(newLoginText);
    }

    private boolean isValidLogin(String newLoginText) {
        if (!newLoginText.matches("^[a-zA-Z0-9]+$")) {
            showToast(R.string.invalid_login_error);
            return false;
        }

        if (newLoginText.length() >= 20) {
            showToast(R.string.login_too_long_error);
            return false;
        }
        return true;
    }

    private boolean isLoginAlreadyExists(String newLoginText) {
        try {
            JSONArray jsonArray = new JSONArray(peoplesJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                String existingLogin = jsonArray.getJSONObject(i).getString("login");
                if (existingLogin.equalsIgnoreCase(newLoginText)) {
                    showToast(R.string.login_already_exists_error);
                    return true;
                }
            }
        } catch (Exception e) {
            showToast(R.string.loginError);
        }
        return false;
    }

    private void updateLogin(String newLoginText) {
        String table = "User";
        String[] options = {"login=" + login, "newlogin=" + newLoginText};

        new Thread(() -> {
            String response = urlUpdate.updateData(table, options);
            runOnUiThread(() -> {
                if (response.startsWith("Erreur")) {
                    showToast(R.string.errorText);
                } else {
                    sessionManager.setLogin(newLoginText);
                    showToast(R.string.changePseudoSuccessText);
                }
            });
        }).start();

        updateLoginInReviews(newLoginText);
    }

    private void updateLoginInReviews(String newLoginText) {
        String table = "Avis";
        String[] options = {"login=" + login, "newlogin=" + newLoginText};

        new Thread(() -> {
            String response = urlUpdate.updateData(table, options);
            runOnUiThread(() -> {
                if (!response.startsWith("Erreur")) {
                    Intent intent = new Intent(AccountSettingsActivity.this, LoadingActivity.class);
                    startActivity(intent);
                }
            });
        }).start();
    }

    private void changePassword() {
        String oldPasswordText = oldPassword.getText().toString();
        String newPasswordText = newPassword.getText().toString();

        if (!isValidPassword(newPasswordText)) return;

        String oldPasswordEncrypted = urlUpdate.encryptToMD5(oldPasswordText);
        if (!oldPasswordEncrypted.equals(sessionManager.getPassword())) {
            showToast(R.string.passwordError);
            return;
        }

        updatePassword(newPasswordText);
    }

    private boolean isValidPassword(String password) {
        if (!password.matches("^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]+$")) {
            showToast(R.string.invalid_password_error);
            return false;
        }

        if (password.length() >= 20) {
            showToast(R.string.password_too_long_error);
            return false;
        }
        return true;
    }

    private void updatePassword(String newPassword) {
        String newPasswordEncrypted = urlUpdate.encryptToMD5(newPassword);
        String table = "User";
        String[] options = {"login=" + login, "newmdp=" + newPasswordEncrypted};

        new Thread(() -> {
            String response = urlUpdate.updateData(table, options);
            runOnUiThread(() -> {
                if (response.startsWith("Erreur")) {
                    showToast(R.string.errorText);
                } else {
                    showToast(R.string.changePasswordSuccessText);
                    startActivity(new Intent(this, LoadingActivity.class));
                }
            });
        }).start();
    }

    public void askDeleteAccount() {
        setDeleteAccountViewVisibility(View.VISIBLE);
        confirmDeleteButton.setOnClickListener(v -> deleteAccount());
        cancelDeleteButton.setOnClickListener(v -> cancelDeleteAccount());
    }

    public void cancelDeleteAccount() {
        setDeleteAccountViewVisibility(View.GONE);
    }

    private void setDeleteAccountViewVisibility(int visibility) {
        confirmDeleteButton.setVisibility(visibility);
        cancelDeleteButton.setVisibility(visibility);
        deleteAccountConfirmText.setVisibility(visibility);
        deleteAccountView.setVisibility(visibility);
    }

    public void deleteAccount() {
        String table = "User";
        String[] options = {"login=" + login};

        new Thread(() -> {
            String response = urlDelete.deleteData(table, options);
            runOnUiThread(() -> {
                if (response.startsWith("Erreur")) {
                    showToast(R.string.deleteAccountFailText);
                } else {
                    sessionManager.logout();
                    showToast(R.string.deleteAccountSuccessText);
                    startActivity(new Intent(this, LoginActivity.class));
                }
            });
        }).start();
    }

    private void disconnectUser() {
        sessionManager.logout();
        startActivity(new Intent(this, LoginActivity.class));
    }

    private void showToast(int messageId) {
        Toast.makeText(this, getString(messageId), Toast.LENGTH_SHORT).show();
    }
}
