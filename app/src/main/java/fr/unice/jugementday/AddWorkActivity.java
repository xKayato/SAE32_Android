package fr.unice.jugementday;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import fr.unice.jugementday.service.Address;
import fr.unice.jugementday.service.MenuButtons;
import fr.unice.jugementday.service.UrlReader;
import fr.unice.jugementday.service.UrlSend;

public class AddWorkActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText title;
    private EditText author;
    private EditText Date;
    private Spinner typeSpinner;
    private ImageButton selectedImage;
    private Bitmap imageBitmap;
    private UrlSend urlSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_work);


        title = findViewById(R.id.workTitleFieldButton);
        author = findViewById(R.id.workAuthorFieldButton);
        typeSpinner = findViewById(R.id.SpinnerType);
        insertIntoSpinner();
        Date = findViewById(R.id.releaseDateTextFieldButton);
        Button publish = findViewById(R.id.publishButton);
        publish.setOnClickListener(this::publishWork);
        selectedImage = findViewById(R.id.selectedImageButton);
        urlSend = new UrlSend();

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

    // Récupérer les types d'oeuvres pour les afficher dans le spinner (liste déroulante). Dynamique
    public void insertIntoSpinner() {
        new Thread(() -> {
            UrlReader urlReader = new UrlReader();
            String result = urlReader.fetchData("&table=Type");

            runOnUiThread(() -> {
                if (!result.contains("Erreur") || result.contains("Aucune")) {

                        try {
                            JSONArray jsonArray = new JSONArray(result);
                            List<String> types = new ArrayList<>();

                            // Parcours du JSONArray pour extraire les types
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                types.add(jsonObject.getString("nomType"));
                            }

                            // Création et attribution de l'adaptateur
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            typeSpinner.setAdapter(adapter);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        }


            });
        }).start();
    }

    // Ouvrir la galerie pour choisir une image
    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Sélectionnez une image"), PICK_IMAGE_REQUEST);
    }
    // Récupérer l'image sélectionnée
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                // On redimensionne l'image pour l'afficher dans l'ImageButton
                imageBitmap = resizeImage(MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri), selectedImage.getWidth(), selectedImage.getHeight());
                // On affiche l'image dans l'ImageButton
                selectedImage.setImageBitmap(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    // Publier une oeuvre
    public void publishWork(View view) {
        // Récupérer les données des champs
        String title = this.title.getText().toString();
        String author = this.author.getText().toString();
        String type = this.typeSpinner.getSelectedItem().toString();
        String date = this.Date.getText().toString();

        // Vérifier que les champs sont remplis
        if (title.isEmpty() || author.isEmpty() || type.isEmpty() || date.isEmpty() || imageBitmap == null) {
            Toast.makeText(this, R.string.fill_fields_error, Toast.LENGTH_LONG).show();
            return;
        }

        // Vérifier que les champs ne dépassent pas 30 caractères
        if(title.length() <= 30 && author.length() <= 30){

            // Envoyer l'image au serveur
            uploadImageToServer(imageBitmap);

            String table = "Oeuvre";
            String[] options = {
                    "nomOeuvre=" + title,
                    "auteur_studio=" + author,
                    "dateSortie=" + date,
                    "actif=1",
                    "type=" + type
            };

            // Envoyer les données au serveur
            new Thread(() -> {
                urlSend.sendData(table, options);

            }).start();
        } else {
            Toast.makeText(this, R.string.maxCharAdd, Toast.LENGTH_LONG).show();
        }


    }

    private Bitmap resizeImage(Bitmap bitmap, int newWidth, int newHeight) {
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }


    private void uploadImageToServer(Bitmap bitmap) {
        new Thread(() -> {
            try {
                // Réduire la taille de l'image à 200x150
                Bitmap resizedBitmap = resizeImage(bitmap, 200, 300);

                // Convertir l'image redimensionnée en Base64
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                String encodedImage = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);

                // Préparer les données JSON (image et nom du fichier)
                JSONObject jsonData = new JSONObject();
                jsonData.put("image", encodedImage);
                jsonData.put("filename", this.title.getText().toString().replace(" ", "_") + ".jpg");

                // Envoyer les données au serveur en POST
                HttpURLConnection connection = (HttpURLConnection) new URL(Address.getImportPhotoPage()).openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setDoOutput(true);

                OutputStream os = connection.getOutputStream();
                os.write(jsonData.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                // Vérifier la réponse du serveur
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Si l'image est envoyée avec succès, envoyer les autres données au serveur
                    String table = "Oeuvre";
                    String[] options = {
                            "nomOeuvre=" + title.getText().toString(),
                            "auteur_studio=" + author.getText().toString(),
                            "dateSortie=" + Date.getText().toString(),
                            "actif=1",
                            "type=" + typeSpinner.getSelectedItem().toString()
                    };

                    // Envoyer les autres données
                    urlSend.sendData(table, options);

                    // Si tout est envoyé avec succès, lancer StartingActivity
                    runOnUiThread(() -> {
                        Intent intent = new Intent(this, LoadingActivity.class);
                        startActivity(intent);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }







}