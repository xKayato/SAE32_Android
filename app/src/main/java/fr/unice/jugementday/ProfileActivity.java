package fr.unice.jugementday;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import fr.unice.jugementday.button.MenuButtons;
import fr.unice.jugementday.service.UserSessionManager;

public class ProfileActivity extends AppCompatActivity {

    private UserSessionManager sessionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        sessionManager = new UserSessionManager(this);

        ImageButton profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> MenuButtons.profileClick(this));

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> MenuButtons.homeClick(this));

        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> MenuButtons.searchClick(this));

        // Vérifier si l'utilisateur est connecté
        if (sessionManager.isLoggedIn()) {
            String userLogin = sessionManager.getLogin();
            // Utilisez le login comme vous voulez
            TextView usernameTextView = findViewById(R.id.modificationStatusText);
            usernameTextView.setText("Bonjour, " + userLogin);
        } else {
            // Si l'utilisateur n'est pas connecté, rediriger vers la page de connexion
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void onSettingClick(View v) {
        Intent i = new Intent(this, activitySettingsAccount.class);
        i.putExtra("title", "Settings");
        startActivity(i);
    }
}