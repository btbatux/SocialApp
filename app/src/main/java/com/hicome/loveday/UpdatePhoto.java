package com.hicome.loveday;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class UpdatePhoto extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    StorageReference storageReference;
    ImageView imageView;
    UploadTask uploadTask;
    ProgressBar progressBar;
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    String currentuid;
    Button updateImage;
    private final static int PICK_IMAGE = 1;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    Uri imageuri, url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_photo);

        imageView = findViewById(R.id.iv_updatephoto);
        updateImage = findViewById(R.id.btn_updatephoto);
        progressBar = findViewById(R.id.pv_updatephoto);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentuid = user.getUid();

        storageReference = FirebaseStorage.getInstance().getReference("Profile images");
        updateImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE);
    }

    private String getFileExt(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
                imageuri = data.getData();

                // Resmin boyutunu kontrol et ve sıkıştır
                if (isImageSizeValid(imageuri, 5 * 1024 * 1024)) {
                    Picasso.get().load(imageuri).into(imageView);
                    uploadImage();
                } else {
                    // Resmi sıkıştır ve yükle
                    compressAndUploadImage(imageuri);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isImageSizeValid(Uri imageUri, long maxSizeBytes) {
        try {
            // Resmi boyutunu al
            ContentResolver contentResolver = getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(imageUri);
            if (inputStream != null) {
                long imageSizeBytes = inputStream.available();
                inputStream.close();

                // Boyut kontrolü
                return imageSizeBytes <= maxSizeBytes;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void uploadImage() {
        if (imageuri != null) {
            // Mevcut profil resmi referansını al
            final DocumentReference userDocRef = db.collection("user").document(currentuid);
            userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        String currentImageUrl = documentSnapshot.getString("url");
                        // Mevcut resmi sil
                        if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                            StorageReference oldImageRef = firebaseStorage.getReferenceFromUrl(currentImageUrl);
                            oldImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Mevcut resim başarıyla silindi, yeni resmi yükle
                                    proceedWithUploadingNewImage(imageuri, userDocRef);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Silme işlemi başarısız oldu, yine de yeni resmi yükle
                                    proceedWithUploadingNewImage(imageuri, userDocRef);
                                }
                            });
                        } else {
                            // Mevcut resim yok, direkt yeni resmi yükle
                            proceedWithUploadingNewImage(imageuri, userDocRef);
                        }
                    }
                }
            });
        }
    }

    private void proceedWithUploadingNewImage(Uri imageUri, DocumentReference userDocRef) {
        final StorageReference newReference = storageReference.child(System.currentTimeMillis() + "." + getFileExt(imageUri));
        uploadTask = newReference.putFile(imageUri);

        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progressPercentage = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                updateImage.setText(String.format("Upload: %s%%", (int) progressPercentage));
                progressBar.setProgress((int) progressPercentage);
            }
        });

        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return newReference.getDownloadUrl();
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    userDocRef.update("url", downloadUri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(UpdatePhoto.this, "Profile image updated successfully.", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                            updateImage.setText("Update Image");
                        }
                    });
                } else {
                    Toast.makeText(UpdatePhoto.this, "Upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    updateImage.setText("Update Image");
                }
            }
        });
    }


    private Bitmap compressImage(InputStream imageStream) {
        try {
            Bitmap originalBitmap = BitmapFactory.decodeStream(imageStream);

            // Resmi hedef boyuta sıkıştır
            int targetWidth = 1024; // Örnek bir hedef genişlik
            int targetHeight = 768; // Örnek bir hedef yükseklik
            return Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void compressAndUploadImage(final Uri imageUri) {
        try {
            InputStream imageStream = getContentResolver().openInputStream(imageUri);
            final Bitmap compressedBitmap = compressImage(imageStream);

            if (compressedBitmap != null) {
                // Bitmap'i byte dizisine dönüştür
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                final byte[] data = baos.toByteArray();

                // Resmi sıkıştırılmış halini yükle
                final StorageReference reference = storageReference.child(System.currentTimeMillis() + "." + getFileExt(imageUri));
                uploadTask = reference.putBytes(data);

                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        progressBar.setProgress((int) progress);
                        updateImage.setText((int) progress + "% Yükleme");
                    }
                });

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return reference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();

                            final DocumentReference sDoc = db.collection("user").document(currentuid);
                            db.runTransaction(new Transaction.Function<Void>() {
                                        @Override
                                        public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                                            DocumentSnapshot snapshot = transaction.get(sDoc);
                                            transaction.update(sDoc, "url", downloadUri.toString());

                                            // Başarılı
                                            return null;
                                        }
                                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(UpdatePhoto.this, "Güncelleniyor", Toast.LENGTH_SHORT).show();

                                            DatabaseReference db1 = database.getReference("All posts");
                                            Query query = db1.orderByChild("uid").equalTo(currentuid);
                                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                        Map<String, Object> profile = new HashMap<>();
                                                        profile.put("url", downloadUri.toString());
                                                        dataSnapshot.getRef().updateChildren(profile)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Toast.makeText(UpdatePhoto.this, "Tamamlandı", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(UpdatePhoto.this, "Başarısız", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
