package fr.unice.jugementday;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import fr.unice.jugementday.button.MenuButtons;
import fr.unice.jugementday.service.UrlSend;

public class AddWorkActivity extends AppCompatActivity {

    EditText title;
    EditText author;
    EditText genre;
    EditText Tags;
    EditText Date;
    Button publish;

    UrlSend urlSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_work);

        title = findViewById(R.id.workTitleFieldButton);
        author = findViewById(R.id.workAuthorFieldButton);
        genre = findViewById(R.id.workGenreFieldButton);
        Tags = findViewById(R.id.workTagsFieldButton);
        Date = findViewById(R.id.releaseDateTextFieldButton);
        publish = findViewById(R.id.publishButton);
        urlSend = new UrlSend();

        ImageButton profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> MenuButtons.profileClick(this));

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> MenuButtons.homeClick(this));

        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> MenuButtons.searchClick(this));


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void publishWork(View view) {

        String title = this.title.getText().toString();
        String author = this.author.getText().toString();
        String genre = this.genre.getText().toString();
        String tags = this.Tags.getText().toString();
        String date = this.Date.getText().toString();

        // Vérifier que les champs sont remplis
        if (title.isEmpty() || author.isEmpty() || genre.isEmpty() || tags.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_LONG).show();
            return;
        }
        String table = "Oeuvre";
        String[] options = {
                "nomOeuvre=" + title,
                "auteur_studio=" + author,
                "genre=" + genre,
                "tags=" + tags,
                "dateSortie=" + date,
                "actif=1",
                "photo=1"
        };

        // Envoyer les données
        new Thread(() -> {
            String response = urlSend.sendData(table, options);

            runOnUiThread(() -> {
                if (response.startsWith("Erreur")) {
                    Toast.makeText(this, response, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Données envoyées : " + response, Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }
}