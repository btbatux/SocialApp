package com.hicome.loveday;

import android.app.Application;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class StoryViewHolder  extends RecyclerView.ViewHolder {

    ImageView imageView ;
    TextView textView;
    public StoryViewHolder(@NonNull View itemView) {
        super(itemView);
    }
    public void setStory(FragmentActivity activity, String postUri, String name, long timeEnd, String timeUpload, String type, String caption,String url, String uid) {


        imageView = itemView.findViewById(R.id.iv_story_f4);
        textView = itemView.findViewById(R.id.tv_unamestory);

        // Firestore'dan kullanıcının profil resmi URL'sini al
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("user").document(uid); // "Users" koleksiyonunuzun adı ve kullanıcının ID'si
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        // Kullanıcının profil resmi URL'sini al
                        String profileImageUrl = document.getString("url"); // "profileImageUrl" alan adınız
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            // Picasso ile resmi ImageView'e yükle
                            Picasso.get().load(profileImageUrl).into(imageView);
                        }
                    } else {
                        // Doküman yoksa veya başka bir hata oluştuysa
                        // Varsayılan bir resim kullanabilirsiniz veya hata yönetimi yapabilirsiniz
                        imageView.setImageResource(R.drawable.usericon); // Varsayılan avatar resmi
                    }
                } else {
                    // Sorgu başarısız olduysa
                    Log.d("Firestore", "Error getting documents: ", task.getException());
                }
            }
        });

        textView.setText(name);
    }
}

