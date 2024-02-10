package com.hicome.loveday;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {

    private Button darkModeButton;
    private TextView showBlockedUsersTv, changePasswordTv, blocktv;
    private boolean isDarkModeOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeViews();
        setupListeners();
        SharedPreferences sharedPreferences = getSharedPreferences("SharedPrefs",MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        final boolean isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn",false);

        if (isDarkModeOn){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            darkModeButton.setText("Disable Dark Mode");
        }else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            darkModeButton.setText("Enable Dark Mode");
        }
        darkModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isDarkModeOn){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editor.putBoolean("isDarkModeOn",false);
                    editor.apply();

                    darkModeButton.setText("Enable Dark Mode");
                }else {

                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    editor.putBoolean("isDarkModeOn",true);
                    editor.apply();

                    darkModeButton.setText("Disable Dark Mode");

                }


            }
        });

        blocktv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, ShowBlocked.class));
            }
        });


    }


    private void initializeViews() {
        darkModeButton = findViewById(R.id.darkmode);
        showBlockedUsersTv = findViewById(R.id.blocktv);
        changePasswordTv = findViewById(R.id.updatepassword);
        blocktv = findViewById(R.id.blocktv);
    }


    private void setupListeners() {
        changePasswordTv.setOnClickListener(view -> showChangePasswordDialog());
    }


    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText currentPasswordInput = new EditText(this);
        currentPasswordInput.setHint("Current password");
        currentPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(currentPasswordInput);

        final EditText newPasswordInput = new EditText(this);
        newPasswordInput.setHint("New password");
        newPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(newPasswordInput);

        builder.setView(layout);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String currentPassword = currentPasswordInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();
            reauthenticateAndUpdatePassword(currentPassword, newPassword);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void reauthenticateAndUpdatePassword(String currentPassword, String newPassword) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Mevcut e-posta ve şifre ile kimlik bilgilerini oluştur
            AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(user.getEmail()), currentPassword);

            // Kullanıcıyı yeniden kimlik doğrulama
            user.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Yeniden kimlik doğrulama başarılı, şimdi şifreyi güncelle
                            user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                                if (updateTask.isSuccessful()) {
                                    Toast.makeText(SettingsActivity.this, "Password updated successfully.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(SettingsActivity.this, "Failed to update password.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(SettingsActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
