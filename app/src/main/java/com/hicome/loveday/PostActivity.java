package com.hicome.loveday;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hicome.loveday.Fragment4;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
public class PostActivity extends AppCompatActivity {
    private static final int PICK_FILE = 1;
    private static final int PICK_IMAGE = 1;
    private static final long MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private ImageView imageView;
    private ProgressBar progressBar;
    private Uri selectedImageUri;

    private EditText etdesc;
    private Button btnchoosefile, btnuploadfile;
    private String url, name;
    private StorageReference storageReference;
    private DatabaseReference db1, db2, db3;
    private MediaController mediaController;
    private String type;
    private Postmember postmember;
    private String senderuid;
    private FirebaseUser user;
    private String currentuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        user = FirebaseAuth.getInstance().getCurrentUser();
        currentuid = user.getUid();

        postmember = new Postmember();
        mediaController = new MediaController(this);

        progressBar = findViewById(R.id.pb_post);
        imageView = findViewById(R.id.iv_post);
        btnchoosefile = findViewById(R.id.btn_choosefile_post);
        btnuploadfile = findViewById(R.id.btn_uploadfile_post);
        etdesc = findViewById(R.id.et_desc_post);

        storageReference = FirebaseStorage.getInstance().getReference("User posts");

        db1 = FirebaseDatabase.getInstance().getReference("All images").child(currentuid);
        db2 = FirebaseDatabase.getInstance().getReference("All videos").child(currentuid);
        db3 = FirebaseDatabase.getInstance().getReference("All posts");



        etdesc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        btnuploadfile.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                Dopost();
            }
        });

        btnchoosefile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });
    }

    private void chooseImage() {
        // Intent.ACTION_GET_CONTENT kullanarak genel bir seçici başlatın
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false); // Tek bir resim seçilmesine izin ver
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            // Picasso kütüphanesi kullanılarak seçilen resmi ImageView'a yükleyin
            Picasso.get().load(selectedImageUri).into(imageView);
            imageView.setVisibility(View.VISIBLE);
            type = "iv"; // 'type' değişkeninizin amacına bağlı olarak
        } else {
            Toast.makeText(this, "No image chosen or operation cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExt(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection("user").document(currentuid);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        name = document.getString("name");
                        url = document.getString("url");
                    } else {
                        Toast.makeText(PostActivity.this, "Document does not exist", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PostActivity.this, "Error getting document: " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void Dopost() {
        final String desc = etdesc.getText().toString();

        if (!TextUtils.isEmpty(desc) && selectedImageUri != null) {
            progressBar.setVisibility(View.VISIBLE);
            final StorageReference reference = storageReference.child(System.currentTimeMillis() + "." + getFileExt(selectedImageUri));
            UploadTask uploadTask = reference.putFile(selectedImageUri);

            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    btnuploadfile.setText(String.format("Uploading... %d%%", (int) progress));
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

                        Calendar cdate = Calendar.getInstance();
                        SimpleDateFormat currentdate = new SimpleDateFormat("dd-MMMM-yyyy");
                        String savedate = currentdate.format(cdate.getTime());
                        Calendar ctime = Calendar.getInstance();
                        SimpleDateFormat currenttime = new SimpleDateFormat("HH:mm:ss");
                        String savetime = currenttime.format(ctime.getTime());
                        String time = savedate + ":" + savetime;

                        postmember.setDesc(desc);
                        postmember.setName(name);
                        postmember.setPostUri(downloadUri.toString());
                        postmember.setTime(time);
                        postmember.setUid(currentuid);
                        postmember.setUrl(url);
                        postmember.setType("iv");

                        DatabaseReference dbRef = db1;
                        String id = dbRef.push().getKey();
                        dbRef.child(id).setValue(postmember);

                        String id1 = db3.push().getKey();
                        db3.child(id1).setValue(postmember);

                        progressBar.setVisibility(View.INVISIBLE);
                        btnuploadfile.setText("Upload File");
                        Toast.makeText(PostActivity.this, "Post Uploaded", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(PostActivity.this, Fragment4.class));
                        finish();
                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        btnuploadfile.setText("Upload File");
                        Toast.makeText(PostActivity.this, "Upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "Please fill all Fields", Toast.LENGTH_SHORT).show();
        }
    }
}
