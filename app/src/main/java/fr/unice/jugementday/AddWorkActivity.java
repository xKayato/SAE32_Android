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
import fr.unice.jugementday.service.UserSessionManager;

public class AddWorkActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText title;
    private EditText author;
    private EditText date;
    private Spinner typeSpinner;
    private ImageButton selectedImage;
    private Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_work);

        checkUserSession(); // Vérifie si l'utilisateur est connecté
        setupUI();          // Configure les éléments de l'interface utilisateur
        setupButtons();     // Configure les boutons
        setupListeners();   // Ajoute les listeners aux éléments
    }

    /**
     * Vérifie si l'utilisateur est connecté. Redirige vers la page de connexion si nécessaire.
     */
    private void checkUserSession() {
        UserSessionManager sessionManager = new UserSessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    private void showToast(int messageId) {
        Toast.makeText(this, getString(messageId), Toast.LENGTH_SHORT).show();
    }

    /**
     * Configure les éléments de l'interface utilisateur.
     */
    private void setupUI() {
        title = findViewById(R.id.workTitleFieldButton);
        author = findViewById(R.id.workAuthorFieldButton);
        date = findViewById(R.id.releaseDateTextFieldButton);
        typeSpinner = findViewById(R.id.SpinnerType);
        selectedImage = findViewById(R.id.selectedImageButton);

        fillTypeSpinner(); // Remplit le Spinner de types d'œuvres

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Configure les boutons et leur comportement.
     */
    private void setupButtons() {
        Button publish = findViewById(R.id.publishButton);
        publish.setOnClickListener(this::publishWork);

        ImageButton profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> MenuButtons.profileClick(this));

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> MenuButtons.homeClick(this));

        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> MenuButtons.searchClick(this));
    }

    /**
     * Ajoute les listeners aux éléments interactifs.
     */
    private void setupListeners() {
        selectedImage.setOnClickListener(v -> openImageChooser());
    }

    /**
     * Remplit le Spinner avec les types d'œuvres obtenus dynamiquement.
     */
    private void fillTypeSpinner() {
        new Thread(() -> {
            UrlReader urlReader = new UrlReader();
            String result = urlReader.fetchData("&table=Type");

            runOnUiThread(() -> {
                if (!result.contains("Erreur") && !result.contains("Aucune")) {
                    try {
                        JSONArray jsonArray = new JSONArray(result);
                        List<String> types = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            types.add(jsonObject.getString("nomType"));
                        }

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

    /**
     * Ouvre la galerie pour choisir une image.
     */
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
                imageBitmap = resizeImage(MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri), selectedImage.getWidth(), selectedImage.getHeight());
                selectedImage.setImageBitmap(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Publie une œuvre en envoyant ses données au serveur.
     */
    private void publishWork(View view) {
        String title = this.title.getText().toString();
        String author = this.author.getText().toString();
        String type = this.typeSpinner.getSelectedItem().toString();
        String date = this.date.getText().toString();

        if (title.isEmpty() || author.isEmpty() || type.isEmpty() || date.isEmpty() || imageBitmap == null) {
            showToast(R.string.fill_fields_error);
            return;
        }

        if (title.length() > 30 || author.length() > 30) {
            showToast(R.string.maxCharAdd);
            return;
        }

        uploadImageToServer(imageBitmap);
    }

    private Bitmap resizeImage(Bitmap bitmap, int newWidth, int newHeight) {
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    private void uploadImageToServer(Bitmap bitmap) {
        new Thread(() -> {
            try {
                Bitmap resizedBitmap = resizeImage(bitmap, 200, 300);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                String encodedImage = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);

                JSONObject jsonData = new JSONObject();
                jsonData.put("image", encodedImage);
                jsonData.put("filename", this.title.getText().toString().replace(" ", "_") + ".jpg");

                HttpURLConnection connection = (HttpURLConnection) new URL(Address.getImportPhotoPage()).openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setDoOutput(true);

                OutputStream os = connection.getOutputStream();
                os.write(jsonData.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
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
