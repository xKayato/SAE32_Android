package fr.unice.jugementday;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import fr.unice.jugementday.service.ImageService;
import fr.unice.jugementday.service.MenuButtons;
import fr.unice.jugementday.service.UrlSend;

public class AddWorkActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText title;
    private EditText author;
    private EditText genre;
    private EditText Date;
    private Button publish;
    private ImageButton selectedImage;
    private Bitmap imageBitmap;
    private String encodedImage;
    private UrlSend urlSend;
    private ImageService imageService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_work);

        title = findViewById(R.id.workTitleFieldButton);
        author = findViewById(R.id.workAuthorFieldButton);
        genre = findViewById(R.id.workGenreFieldButton);
        Date = findViewById(R.id.releaseDateTextFieldButton);
        publish = findViewById(R.id.publishButton);
        publish.setOnClickListener(this::publishWork);
        selectedImage = findViewById(R.id.selectedImageButton);
        urlSend = new UrlSend();
        imageService = new ImageService();

        ImageButton profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> MenuButtons.profileClick(this));

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> MenuButtons.homeClick(this));

        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> MenuButtons.searchClick(this));


        selectedImage.setOnClickListener(v -> openImageChooser());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Sélectionnez une image"), PICK_IMAGE_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                selectedImage.setImageBitmap(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void publishWork(View view) {

        String title = this.title.getText().toString();
        String author = this.author.getText().toString();
        String genre = this.genre.getText().toString();
        String date = this.Date.getText().toString();

        // Vérifier que les champs sont remplis
        if (title.isEmpty() || author.isEmpty() || genre.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs et sélectionner une image", Toast.LENGTH_LONG).show();
            return;
        }

        // Convertir l'image en base64
        String imageBase64 = imageService.convertImageToBase64(imageBitmap);
        try {
            encodedImage = URLEncoder.encode(imageBase64, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Toast.makeText(this, "Encoding error", Toast.LENGTH_LONG).show();
            return;
        }
        String table = "Oeuvre";
        String[] options = {
                "nomOeuvre=" + title,
                "auteur_studio=" + author,
                "genre=" + genre,
                "tags=non",
                "dateSortie=" + date,
                "actif=1",
                "photo=" + encodedImage
        };

        System.out.println("Base64 Image: " + encodedImage);
        // Envoyer les données avec l'image
        new Thread(() -> {
            String response = urlSend.sendData(table, options);

            runOnUiThread(() -> {
                if (response.startsWith("Erreur")) {
                    Toast.makeText(this, encodedImage, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Données envoyées : " + response, Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }
}
