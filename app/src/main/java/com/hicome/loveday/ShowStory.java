package com.hicome.loveday;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class ShowStory extends AppCompatActivity implements StoriesProgressView.StoriesListener {


    int counter = 0;
    ImageView imageViewSHowStory, imageViewUrl;
    TextView textView, tv_view, tvcap;
    ImageButton deletebtn;
    List<String> posturi;
    List<String> url;
    List<String> username;
    List<String> caption;
    List<Long> timeEnd;
    DatabaseReference checkVideocallRef;
    String senderuid;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String currentuid = user.getUid();
    String userid;
    String currentUid;
    StoriesProgressView storiesProgressView;
    DatabaseReference reference;
    private String adminEmail1;
    private String adminEmail2;
    private String adminEmail3;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_story);

        imageViewSHowStory = findViewById(R.id.iv_storyview);
        imageViewUrl = findViewById(R.id.iv_ss);
        textView = findViewById(R.id.tv_uname_ss);
        storiesProgressView = findViewById(R.id.stories);

        tvcap = findViewById(R.id.tv_cap_st);
        deletebtn = findViewById(R.id.btn_delstory);
        tv_view = findViewById(R.id.tv_views);

        View reverse = findViewById(R.id.view_prev);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storiesProgressView.reverse();
            }
        });
        reverse.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                storiesProgressView.pause();
                return false;
            }
        });
        reverse.setOnTouchListener(onTouchListener);

        View next = findViewById(R.id.view_next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storiesProgressView.skip();
            }
        });
        next.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                storiesProgressView.pause();
                return false;
            }
        });
        next.setOnTouchListener(onTouchListener);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            userid = bundle.getString("u");
        } else {
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
        }
        reference = database.getReference("story").child(userid);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentUid = user.getUid();
        String uemail = user.getEmail();

        // String değerlerine erişim ve atama
        adminEmail1 = getResources().getString(R.string.admin_email_1);
        adminEmail2 = getResources().getString(R.string.admin_email_2);
        adminEmail3 = getResources().getString(R.string.admin_email_3);

        if (currentUid.equals(userid) || uemail.equals(adminEmail1) || uemail.equals(adminEmail2) || uemail.equals(adminEmail3)) {
            tv_view.setVisibility(View.VISIBLE);
            deletebtn.setVisibility(View.VISIBLE);
        } else {

            tv_view.setVisibility(View.INVISIBLE);
            deletebtn.setVisibility(View.INVISIBLE);
        }


        deletebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {

                    Query query = reference.orderByChild("timeEnd").equalTo(timeEnd.get(counter));
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                                dataSnapshot1.getRef().removeValue();

                                StorageReference storage = FirebaseStorage.getInstance().getReferenceFromUrl(posturi.get(counter));
                                storage.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        Toast.makeText(ShowStory.this, "deleted", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } catch (Exception e) {

                }


            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        getStories(userid);
    }

    private void getStories(String userid) {

        posturi = new ArrayList<>();
        username = new ArrayList<>();
        url = new ArrayList<>();
        timeEnd = new ArrayList<>();
        caption = new ArrayList<>();

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                posturi.clear();
                url.clear();
                username.clear();
                timeEnd.clear();
                caption.clear();

                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    StoryMember member = snapshot1.getValue(StoryMember.class);

                    posturi.add(member.getPostUri());
                    url.add(member.getUrl());
                    username.add(member.getName());
                    timeEnd.add(member.getTimeEnd());
                    caption.add(member.getCaption());


                }

                storiesProgressView.setStoriesCount(posturi.size());
                storiesProgressView.setStoryDuration(5000L);
                storiesProgressView.setStoriesListener(ShowStory.this);
                storiesProgressView.startStories(counter);

                Picasso.get().load(posturi.get(counter)).into(imageViewSHowStory);
                Picasso.get().load(url.get(counter)).into(imageViewUrl);
                textView.setText(username.get(counter));
                tvcap.setText(caption.get(counter));

                view(currentUid);
                ViewCount();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onNext() {

        Picasso.get().load(posturi.get(++counter)).into(imageViewSHowStory);
        tvcap.setText(caption.get(counter));


        view(currentUid);
        ViewCount();

    }

    @Override
    public void onPrev() {

        if ((counter - 1) < 0) return;
        Picasso.get().load(posturi.get(--counter)).into(imageViewSHowStory);
        tvcap.setText(caption.get(counter));

        ViewCount();

    }

    @Override
    public void onComplete() {

        finish();
    }

    @Override
    protected void onDestroy() {
        storiesProgressView.destroy();
        super.onDestroy();


    }

    @Override
    protected void onPause() {
        storiesProgressView.pause();
        super.onPause();


    }

    private void view(String currentUid) {
        DatabaseReference viewRef = database.getReference("views");
        viewRef.child(userid).child(currentUid).setValue(true);

    }

    private void ViewCount() {
        DatabaseReference viewRef = database.getReference("views").child(currentUid);
        viewRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int viewCount = (int) snapshot.getChildrenCount();
                tv_view.setText(viewCount + " Views");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}