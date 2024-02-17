package com.hicome.loveday;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
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
import com.squareup.picasso.Picasso;

public class ReplyActivity extends AppCompatActivity {
    String uid, question, post_key, key, name_result;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference reference, reference2;
    TextView nametv, questiontv, tvreply;
    RecyclerView recyclerView;
    ImageView imageViewQue, imageViewUser;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference votesref, Allquetions;
    Boolean votechecker = false;
    DatabaseReference checkVideocallRef;
    String senderuid;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String currentuid = user.getUid();
    private RewardedAd rewardedAd;
    private final String TAG = "ReplyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);

        nametv = findViewById(R.id.name_reply_tv);
        questiontv = findViewById(R.id.que_reply_tv);
        imageViewQue = findViewById(R.id.iv_que_user);
        imageViewUser = findViewById(R.id.iv_reply_user);
        tvreply = findViewById(R.id.answer_tv);

        recyclerView = findViewById(R.id.rv_ans);
        recyclerView.setLayoutManager(new LinearLayoutManager(ReplyActivity.this));


        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            uid = extra.getString("uid");
            post_key = extra.getString("postkey");
            question = extra.getString("q");
            // key = extra.getString("key");
        } else {
            Toast.makeText(this, "opps", Toast.LENGTH_SHORT).show();
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentuid = user.getUid();

        Allquetions = database.getReference("AllQuestions").child(post_key).child("Answer");
        votesref = database.getReference("votes");

        reference = db.collection("user").document(uid);
        reference2 = db.collection("user").document(currentuid);


        tvreply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                        AnswerBottomSheetFragment bottomSheetFragment = AnswerBottomSheetFragment.newInstance(uid, post_key);
                        bottomSheetFragment.show(getSupportFragmentManager(), "AnswerBottomSheet");

            }
        });

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, "ca-app-pub-8648170927904071/4567662286",
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d(TAG, loadAdError.toString());
                        rewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        rewardedAd = ad;
                        Log.d(TAG, "Ad was loaded.");
                        if (rewardedAd != null) {
                            Activity activityContext = ReplyActivity.this;
                            rewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                                @Override
                                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                    // Handle the reward.
                                    Log.d(TAG, "The user earned the reward.");
                                    int rewardAmount = rewardItem.getAmount();
                                    String rewardType = rewardItem.getType();
                                }
                            });
                        } else {
                            Log.d(TAG, "The rewarded ad wasn't ready yet.");
                        }
                    }
                });

    }

    private void notification() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel nc = new NotificationChannel("n", "n", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(nc);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "n")
                    .setContentText("Social media")
                    .setSmallIcon(R.drawable.ic_baseline_category_24)
                    .setAutoCancel(true)
                    .setContentText(name_result + " Replied to your Question");

            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            managerCompat.notify(999, builder.build());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
       // question user refernce
        reference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.getResult().exists()){
                            String url = task.getResult().getString("url");
                             name_result = task.getResult().getString("name");
                            Picasso.get().load(url).into(imageViewQue);
                            questiontv.setText(question);
                            nametv.setText(name_result);
                        }else {
                            Toast.makeText(ReplyActivity.this, "error", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
      // refernce for replying user
        reference2.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.getResult().exists()){
                            String url = task.getResult().getString("url");
                            Picasso.get().load(url).into(imageViewUser);

                        }else {
                            Toast.makeText(ReplyActivity.this, "error", Toast.LENGTH_SHORT).show();
                        }

                    }
                });





        FirebaseRecyclerOptions<AnswerMember> options =
                new FirebaseRecyclerOptions.Builder<AnswerMember>()
                        .setQuery(Allquetions,AnswerMember.class)
                        .build();

        FirebaseRecyclerAdapter<AnswerMember,AnsViewholder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<AnswerMember, AnsViewholder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull AnsViewholder holder, int position, @NonNull final AnswerMember model) {

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        final String currentUserid = user.getUid();

                        final String postkey = getRef(position).getKey();

                        holder.setAnswer(getApplication(),model.getName(),model.getAnswer()
                                ,model.getUid(),model.getTime(),model.getUrl());

                        holder.upvoteChecker(postkey);
                        holder.upvoteTv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                votechecker = true;
                                votesref.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        if (votechecker.equals(true)){
                                            if (snapshot.child(postkey).hasChild(currentUserid)){
                                                votesref.child(postkey).child(currentUserid).removeValue();

                                                votechecker = false;
                                            }else {
                                                votesref.child(postkey).child(currentUserid).setValue(true);

                                                votechecker = false;
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public AnsViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.ans_layout,parent,false);

                        return new AnsViewholder(view);



                    }
                };
        firebaseRecyclerAdapter.startListening();

        recyclerView.setAdapter(firebaseRecyclerAdapter);


    }

}