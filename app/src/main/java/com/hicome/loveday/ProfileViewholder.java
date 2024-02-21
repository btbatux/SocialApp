package com.hicome.loveday;

import static android.app.PendingIntent.getActivity;
import static android.provider.Settings.System.getString;

import android.app.Application;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.checkerframework.checker.units.qual.A;

public class ProfileViewholder extends RecyclerView.ViewHolder {
    TextView textViewName, textViewProfession, sendmessagebtn;
    TextView namell, vp_ll, namefollower, vpfollower, professionFollower;
    ImageButton viewUser_delete,viewUserprofile;
    DatabaseReference blockref;
    ImageView imageView, iv_ll, iv_follower;
    CardView cardView;
    LinearLayout llprofile;
    Postmember postmember;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    // Resources nesnesini alın
    Resources resources = itemView.getResources();
    // strings.xml içindeki e-posta adreslerini alın
    String email1 = resources.getString(R.string.admin_email_1);
    String email2 = resources.getString(R.string.admin_email_2);
    String email3 = resources.getString(R.string.admin_email_3);
    DocumentReference documentReference;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ProfileViewholder(@NonNull View itemView) {
        super(itemView);
    }

    public void setProfile(FragmentActivity fragmentActivity, String name, String uid, String prof,
                           String url) {

        cardView = itemView.findViewById(R.id.cardview_profile);
        textViewName = itemView.findViewById(R.id.tv_name_profile);
        textViewProfession = itemView.findViewById(R.id.tv_profession_profile);
        viewUserprofile = itemView.findViewById(R.id.viewUser_profile);
        imageView = itemView.findViewById(R.id.profile_imageview);
        llprofile = itemView.findViewById(R.id.ll_profile);
        viewUser_delete = itemView.findViewById(R.id.viewUser_delete); //delete user tv
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentuid = user.getUid();
        String email = user.getEmail();


        // E-posta adreslerini kontrol edin
        if (email1.equals(email) || email2.equals(email) || email3.equals(email)) {
            viewUser_delete.setVisibility(View.VISIBLE); // Admin için görünür yap DELETE USER BUTONU
        } else {
            viewUser_delete.setVisibility(View.GONE); // Diğer kullanıcılar için gizle
        }

        // Admin kontrolü
        viewUser_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteUser(uid, fragmentActivity);
                deleteImages(uid);
                deletepost(uid);
            }
        });

        blockref = database.getReference("Block users").child(currentuid);
        blockref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.hasChild(uid)) {

                    textViewProfession.setText("");
                    textViewName.setText("App user");
                    viewUserprofile.setVisibility(View.GONE);

                } else
                {
                    documentReference = db.collection("user").document(uid);
                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            String nameResult = task.getResult().getString("name");
                            String profResult = task.getResult().getString("prof");
                            String url = task.getResult().getString("url");

                            Picasso.get().load(url).into(imageView);
                            textViewName.setText(nameResult);
                            textViewProfession.setText(profResult);


                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void deleteUser(String userId, FragmentActivity fragmentActivity) {
        // Firebase Realtime Database'den kullanıcıyı silme
        DatabaseReference realtimeRef = FirebaseDatabase.getInstance().getReference("All Users").child(userId);
        realtimeRef.removeValue();
        // Firestore'dan kullanıcıyı silme (Eğer Firestore kullanıyorsanız)
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference firestoreRef = firestore.collection("user").document(userId);
        firestoreRef.delete();

        // Kullanıcı silindiğinde bir geri bildirim mesajı göster
        Toast.makeText(fragmentActivity, "realtime + firestore deleted", Toast.LENGTH_SHORT).show();


    }

    private void deleteImages(String uid) {
        DatabaseReference realtimeRef2 = FirebaseDatabase.getInstance().getReference("All images").child(uid);
        realtimeRef2.removeValue();

    }

    private void deletepost(String uid) {
        DatabaseReference realtimeRef3 = FirebaseDatabase.getInstance().getReference("All posts").child(uid);
        realtimeRef3.removeValue();
    }


    public void setProfileInchat(Application fragmentActivity, String name, String uid, String prof,
                                 String url) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userid = user.getUid();

        ImageView imageView = itemView.findViewById(R.id.iv_ch_item);
        TextView nametv = itemView.findViewById(R.id.name_ch_item_tv);
        TextView proftv = itemView.findViewById(R.id.ch_itemprof_tv);
        sendmessagebtn = itemView.findViewById(R.id.send_messagech_item_btn);
        CardView cv_chat = itemView.findViewById(R.id.cv_chat_profile);
        LinearLayout linearLayout = itemView.findViewById(R.id.ll_chat_profile);

        blockref = database.getReference("Block users").child(userid);
        blockref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.hasChild(uid)) {
                    cv_chat.setVisibility(View.GONE);
                    linearLayout.setVisibility(View.GONE);
                } else {

                    if (userid.equals(uid)) {
                        //Picasso.get().load(url).into(imageView);
                        //nametv.setText(name);
                        //proftv.setText(prof);
                        //sendmessagebtn.setVisibility(View.INVISIBLE);
                        cv_chat.setVisibility(View.GONE);
                        linearLayout.setVisibility(View.GONE);
                    } else {
                        Picasso.get().load(url).into(imageView);
                        nametv.setText(name);
                        proftv.setText(prof);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    public void setLikeduser(Application fragmentActivity, String name, String uid, String prof,
                             String url) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userid = user.getUid();


        vp_ll = itemView.findViewById(R.id.vp_ll);
        namell = itemView.findViewById(R.id.name_ll);
        iv_ll = itemView.findViewById(R.id.iv_ll);

        Picasso.get().load(url).into(iv_ll);
        namell.setText(name);


    }

    public void setFollower(Application application, String name, String url,
                            String profession, String bio, String privacy, String email, String followers, String website) {

        iv_follower = itemView.findViewById(R.id.iv_follower);
        professionFollower = itemView.findViewById(R.id.profession_follower);
        namefollower = itemView.findViewById(R.id.name_follower);
        vpfollower = itemView.findViewById(R.id.vp_follower);

        Picasso.get().load(url).into(iv_follower);
        namefollower.setText(name);
        professionFollower.setText(profession);


    }


}
