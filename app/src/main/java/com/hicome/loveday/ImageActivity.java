package com.hicome.loveday;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class ImageActivity extends AppCompatActivity {

    ImageView imageView;
    TextView textView;
    Button btnEdit,btnDel;
    DocumentReference reference;
    String url;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    String senderuid;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String currentuid = user.getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);


        btnDel = findViewById(R.id.btn_del_iv);
        btnEdit = findViewById(R.id.btn_edit_iv);
        imageView = findViewById(R.id.iv_expand);
        textView = findViewById(R.id.tv_name_image);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentid = user.getUid();

        reference = db.collection("user").document(currentid);

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ImageActivity.this,UpdatePhoto.class);
                startActivity(intent);
            }
        });
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Firebase Storage'dan fotoğrafı sil
                StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(url);
                photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Fotoğraf başarıyla silindiğinde kullanıcıya bildir
                        Toast.makeText(ImageActivity.this, "Photo deleted successfully", Toast.LENGTH_SHORT).show();

                        // ImageView'deki resmi temizle
                        imageView.setImageResource(0); // ImageView'i temizle

                        // Firestore'daki kullanıcı profil bilgisini sil veya güncelle
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("url", FieldValue.delete()); // Fotoğrafın URL'sini sil
                        // Eğer başka alanlar varsa ve silmek (veya güncellemek) istiyorsanız, burada belirtebilirsiniz.

                        reference.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Bilgi başarıyla silindiğinde veya güncellendiğinde kullanıcıya bildir
                                Toast.makeText(ImageActivity.this, "User info updated successfully", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ImageActivity.this, "Error updating user info", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ImageActivity.this, "Error deleting photo", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        reference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.getResult().exists()){
                    String name = task.getResult().getString("name");
                    url = task.getResult().getString("url");

                    Picasso.get().load(url).into(imageView);
                    textView.setText(name);
                }else {
                    Toast.makeText(ImageActivity.this, "No profile", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}