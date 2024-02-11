package com.hicome.loveday;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class CreateProfile extends AppCompatActivity {

    EditText etname, etBio, etProfession, etEmail, etWeb;
    Button button;
    ImageView imageView;
    TextView termsLink;
    ProgressBar progressBar;
    Uri imageUri;
    UploadTask uploadTask;
    StorageReference storageReference;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference documentReference;
    private static final int PICK_IMAGE = 1;
    All_UserMmber member;
    String currentUserId;
    CheckBox checkBox_terms;

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5 MB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);


        member = new All_UserMmber();
        imageView = findViewById(R.id.iv_cp);
        etBio = findViewById(R.id.et_bio_cp);
        etEmail = findViewById(R.id.et_email_cp);
        etname = findViewById(R.id.et_name_cp);
        etProfession = findViewById(R.id.et_profession_cp);
        etWeb = findViewById(R.id.et_web_cp);
        button = findViewById(R.id.btn_cp);
        progressBar = findViewById(R.id.progressbar_cp);
        termsLink = findViewById(R.id.tv_terms_link);
        checkBox_terms = findViewById(R.id.checkBox_terms);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = user.getUid();

        documentReference = db.collection("user").document(currentUserId);
        storageReference = FirebaseStorage.getInstance().getReference("Profile images");
        databaseReference = database.getReference("All Users");


        checkBox_terms.setButtonTintList(ColorStateList.valueOf(Color.GREEN)); // CheckBox'ın rengini kırmızı yapar

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // CheckBox'ın durumunu kontrol edin
                if (!checkBox_terms.isChecked()) {
                    checkBox_terms.setButtonTintList(ColorStateList.valueOf(Color.RED)); // CheckBox'ın rengini kırmızı yapar
                    return; // Şartlar kabul edilmediyse kayıt işlemi engellendi
                }

                // Şartlar kabul edildiyse CheckBox'ın rengini normale döndürün
                checkBox_terms.setButtonTintList(ColorStateList.valueOf(Color.GREEN)); // CheckBox'ın rengini yeşil yapar

                // Verileri yükle
                uploadData();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PICK_IMAGE);
            }
        });

        termsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateProfile.this, TermsAndConditionsActivity.class);
                startActivity(intent);
            }
        });


    }

    // Fotoğraf seçimi sonucunda çağrılan onActivityResult metodu
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();

            // Dosya boyutunu kontrol et
            long fileSize = getFileSize(selectedImageUri);
            if (fileSize > MAX_IMAGE_SIZE) {
                // Dosya boyutu 5 MB'dan büyükse kullanıcıya uyarı ver
                Toast.makeText(this, "Please select an image smaller than 5 MB", Toast.LENGTH_SHORT).show();
                return;
            }

            // Dosya boyutu uygunsa resmi göster
            imageUri = selectedImageUri;
            Picasso.get().load(imageUri).into(imageView);
        }
    }


    // Uri'den dosya boyutunu almak için bir yardımcı metot
    private long getFileSize(Uri uri) {
        try {
            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor != null) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                cursor.moveToFirst();
                long fileSize = cursor.getLong(sizeIndex);
                cursor.close();
                return fileSize;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private String getFileExt(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType((contentResolver.getType(uri)));
    }

    private void uploadData() {
        final String name = etname.getText().toString();
        final String bio = etBio.getText().toString();
        final String web = etWeb.getText().toString();
        final String prof = etProfession.getText().toString();
        final String email = etEmail.getText().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(bio) || TextUtils.isEmpty(web) || TextUtils.isEmpty(prof)
                || TextUtils.isEmpty(email) || imageUri == null) {
            Toast.makeText(this, "Please fill all Fields and select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        final StorageReference reference = storageReference.child(System.currentTimeMillis() + "." + getFileExt(imageUri));

        reference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> reference.getDownloadUrl().addOnSuccessListener(uri -> {
            Uri downloadUri = uri;

            Map<String, String> profile = new HashMap<>();
            profile.put("name", name);
            profile.put("prof", prof);
            profile.put("url", downloadUri.toString());
            profile.put("email", email);
            profile.put("web", web);
            profile.put("bio", bio);
            profile.put("uid", currentUserId);
            profile.put("privacy", "Public");

            member.setName(name.toUpperCase());
            member.setProf(prof);
            member.setUid(currentUserId);
            member.setUrl(downloadUri.toString());

            databaseReference.child(currentUserId).setValue(member);

            documentReference.set(profile)
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(CreateProfile.this, "Profile Created", Toast.LENGTH_SHORT).show();
                        Handler handler = new Handler();
                        handler.postDelayed(() -> {
                            Intent intent = new Intent(CreateProfile.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }, 2500);
                    });
        })).addOnFailureListener(e -> {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(CreateProfile.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
