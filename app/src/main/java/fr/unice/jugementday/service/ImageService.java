package fr.unice.jugementday.service;// HomeActivity.java

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class ImageService {

    // Constructeur par défaut
    public ImageService() {
        // Initialisation ou configuration si nécessaire
    }

    public String convertImageToBase64(Bitmap bitmap) {
        try {
            // Redimensionner l'image à 200x300 pixels
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 200, 300, false);

            // Convertir en tableau de bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            byte[] byteArray = outputStream.toByteArray();

            // Encoder en Base64
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            return null;
        }
    }
}
