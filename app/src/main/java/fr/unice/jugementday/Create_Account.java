package fr.unice.jugementday;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Create_Account extends AppCompatActivity {

    private Button registerButton;
    private EditText loginField;
    private EditText pseudoField;
    private EditText passwordField;
    private EditText passwordFieldConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_account);

        registerButton = findViewById(R.id.registerButton);
        loginField = findViewById(R.id.loginFieldButton);
        pseudoField = findViewById(R.id.pseudoFieldButton);
        passwordField = findViewById(R.id.passwordFieldButton);
        passwordFieldConfirm = findViewById(R.id.passwordFieldConfirmButton);
        registerUser();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    private void registerUser(){
        registerButton.setOnClickListener(v -> {
            String loginText = loginField.getText().toString().trim();
            String nicknameText = pseudoField.getText().toString().trim();
            String passwordText = passwordField.getText().toString().trim();
            String confirmPasswordText = passwordFieldConfirm.getText().toString().trim();
            if(!loginText.isEmpty() && !nicknameText.isEmpty() && !passwordText.isEmpty() && !confirmPasswordText.isEmpty()){
                if(passwordText.equals(confirmPasswordText)){
                    
                } else {
                    Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
            }
        });
    }
}