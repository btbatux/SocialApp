package com.hicome.loveday;

import static android.service.controls.ControlsProviderService.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CommentsActivity extends AppCompatActivity {

    ImageView usernameImageview;
    TextView usernameTextview;
    Button commentsBtn;
    EditText commentsEdittext;
    String url, name, post_key, userid, bundleuid;
    DatabaseReference Commentref, userCommentref, likesref, ntref;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    String name_result, age_result, Url, uid, bio_result, web_result, email_result, usertoken;
    RecyclerView recyclerView;
    Boolean likeChecker = false;
    DatabaseReference checkVideocallRef;
    String senderuid;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String currentuid = user.getUid();
    NewMember newMember;
    CommentsMember commentsMember;
    public String fcmKey;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        commentsMember = new CommentsMember();

        newMember = new NewMember();
        recyclerView = findViewById(R.id.recycler_view_comments);

        recyclerView.setHasFixedSize(true);
        //   MediaController mediaController;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        commentsBtn = findViewById(R.id.btn_comments);
        usernameImageview = findViewById(R.id.imageviewUser_comment);
        usernameTextview = findViewById(R.id.name_comments_tv);
        commentsEdittext = findViewById(R.id.et_comments);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            url = extras.getString("url");
            name = extras.getString("name");
            post_key = extras.getString("postkey");
            bundleuid = extras.getString("uid");
        } else {

        }


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userid = user.getUid();
        Commentref = database.getReference("All Posts").child(post_key).child("Comments");

        likesref = database.getReference("comment likes");
        userCommentref = database.getReference("User Posts").child(userid);

        ntref = database.getReference("notification").child(bundleuid);

        commentsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                comment();
            }
        });

        //MOBİL ADS REKLAM
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });

        //Reklam isteğini oluştur Geçişli
        AdRequest adRequest = new AdRequest.Builder().build();
        // Ara reklamı yükle  Geçişli
        InterstitialAd.load(this, "ca-app-pub-8648170927904071/1746279598", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        Log.i("AdMob", "onAdLoaded");

                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Reklam kapatıldığında yapılacak işlemler
                                Log.d("AdMob", "Ad was dismissed.");
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Reklam gösterimi başarısız olduğunda yapılacak işlemler
                                Log.d("AdMob", "Ad failed to show.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Reklam gösterildiğinde yapılacak işlemler
                                Log.d("AdMob", "Ad showed fullscreen content.");
                                mInterstitialAd = null;
                            }
                        });

                        if (mInterstitialAd != null) {
                            mInterstitialAd.show(CommentsActivity.this);
                        }
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.d("AdMob", "Ad failed to load.");
                        mInterstitialAd = null;
                    }
                });



    }

    @Override
    protected void onStart() {
        super.onStart();

        Picasso.get().load(url).into(usernameImageview);
        usernameTextview.setText(name);


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection("user").document(userid);

        documentReference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.getResult().exists()) {
                            name_result = task.getResult().getString("name");
                            age_result = task.getResult().getString("age");
                            bio_result = task.getResult().getString("bio");
                            email_result = task.getResult().getString("email");
                            web_result = task.getResult().getString("website");
                            Url = task.getResult().getString("url");
                            uid = task.getResult().getString("uid");
                        }
                    }
                });


        FirebaseRecyclerOptions<CommentsMember> options =
                new FirebaseRecyclerOptions.Builder<CommentsMember>()
                        .setQuery(Commentref, CommentsMember.class)
                        .build();

        FirebaseRecyclerAdapter<CommentsMember, CommentsViewholder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<CommentsMember, CommentsViewholder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull CommentsViewholder holder, int position, @NonNull CommentsMember model) {

                        // Yorumun sahibi olan kullanıcının ID'sini al
                        String commentUserId = model.getUid();

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String currentUserId = user.getUid();
                        final String postkey = getRef(position).getKey();
                        String time = getItem(position).getTime();

                        holder.setComment(getApplication(), model.getComment(), model.getTime(), model.getUrl(), model.getUsername(), model.getUid());

                        holder.LikeChecker(postkey);

                        holder.delete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Query query = Commentref.orderByChild("time").equalTo(time);
                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                                            dataSnapshot1.getRef().removeValue();

                                            Toast.makeText(CommentsActivity.this, "deleted", Toast.LENGTH_SHORT).show();
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });


                            }
                        });
                        holder.likebutton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                likeChecker = true;

                                likesref.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        if (likeChecker.equals(true)) {
                                            if (snapshot.child(postkey).hasChild(currentUserId)) {
                                                likesref.child(postkey).child(currentUserId).removeValue();
                                                likeChecker = false;

                                            } else {
                                                likesref.child(postkey).child(currentUserId).setValue(true);
                                                likeChecker = false;
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        });

                        // Şu anki kullanıcının ID'si ile yorum yapanın ID'sini karşılaştır
                        if (userid.equals(commentUserId)) {
                            // Eğer ID'ler eşleşiyorsa, delete butonunu görünür yap
                            holder.delete.setVisibility(View.VISIBLE);
                        } else {
                            // Eğer ID'ler eşleşmiyorsa, delete butonunu gizle
                            holder.delete.setVisibility(View.GONE);
                        }


                    }

                    @NonNull
                    @Override
                    public CommentsViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.comments_item, parent, false);

                        return new CommentsViewholder(view);
                    }
                };

        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    private void comment() {

        Calendar callfordate = Calendar.getInstance();
        SimpleDateFormat currentdate = new
                SimpleDateFormat("dd-MMMM-yyyy");
        final String savedate = currentdate.format(callfordate.getTime());
        Calendar callfortime = Calendar.getInstance();
        SimpleDateFormat currenttime = new
                SimpleDateFormat("HH:mm:ss");

        final String savetime = currenttime.format(callfortime.getTime());
        String time = savedate + ":" + savetime;

        String comment = commentsEdittext.getText().toString().trim();
        if (!TextUtils.isEmpty(comment))
        {
                commentsMember.setComment(comment);
                commentsMember.setUsername(name_result);
                commentsMember.setUid(uid);
                commentsMember.setTime(time);
                commentsMember.setUrl(Url);

                String pushkey = Commentref.push().getKey();
                Commentref.child(pushkey).setValue(commentsMember);

                commentsEdittext.setText("");


                newMember.setName(name);
                newMember.setUid(userid);
                newMember.setUrl(Url);
                newMember.setSeen("no");
                newMember.setText("Commented on your post: " + comment);

                String key = ntref.push().getKey();
                ntref.child(key).setValue(newMember);
                sendNotification(bundleuid, name_result, comment);

                Toast.makeText(this, "Commented", Toast.LENGTH_SHORT).show();

            }
         else
        {
           commentsEdittext.setError("Comment is empty");
        }
    }

    private void sendNotification(String bundleuid, String name_result, String comment) {

        FirebaseDatabase.getInstance().getReference("Token").child(bundleuid).child("token")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        usertoken = snapshot.getValue(String.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        db.collection("FcmKey")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                fcmKey = document.getString("FcmKey");
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {



                FcmNotificationsSender notificationsSender =
                        new FcmNotificationsSender(usertoken, "new comment", name_result + " Commented on your post: " + comment,
                                getApplicationContext(), CommentsActivity.this);

                notificationsSender.SendNotifications(fcmKey);

            }
        }, 3000);

    }
}