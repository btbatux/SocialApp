package com.hicome.loveday;
import static android.app.PendingIntent.getActivity;
import static android.service.controls.ControlsProviderService.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardedAd;
public class MessageActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ImageView imageView;
    ImageButton sendbtn, cambtn;
    TextView username, typingtv;
    EditText messageEt;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference rootref1, rootref2, typingref, cancelRef;
    MessageMember messageMember;
    Boolean typingchecker = false;
    String receiver_name, receiver_uid, sender_uid, url, usertoken;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    Uri uri;
    private static final int PICK_IMAGE = 1;
    private RewardedAd rewardedAd;
    private final String TAG = "MainActivity";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public String fcmKey;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_message);
        super.onCreate(savedInstanceState);



        try {

            SharedPreferences sharedPreferences = getSharedPreferences("SharedPrefs", MODE_PRIVATE);

            final boolean isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", false);

            if (isDarkModeOn) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

        }


        cancelRef = database.getInstance().getReference("cancel");
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            url = bundle.getString("u");
            receiver_name = bundle.getString("n");
            receiver_uid = bundle.getString("uid");
        } else {
            Toast.makeText(this, "user missing", Toast.LENGTH_SHORT).show();
        }

        cancelRef.removeValue();
        messageMember = new MessageMember();

        recyclerView = findViewById(R.id.rv_message);

        cambtn = findViewById(R.id.cam_sendmessage);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MessageActivity.this));
        imageView = findViewById(R.id.iv_message);
        messageEt = findViewById(R.id.messageet);
        sendbtn = findViewById(R.id.imageButtonsend);
        username = findViewById(R.id.username_messageTv);
        typingtv = findViewById(R.id.typingstatus);

        Picasso.get().load(url).into(imageView);
        username.setText(receiver_name);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        sender_uid = user.getUid();

        rootref1 = database.getReference("Message").child(sender_uid).child(receiver_uid);
        rootref2 = database.getReference("Message").child(receiver_uid).child(sender_uid);
        typingref = database.getReference("typing");


        sendbtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });

        cambtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PICK_IMAGE);
            }
        });


        typingref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child(sender_uid).hasChild(receiver_uid)) {
                    typingtv.setVisibility(View.VISIBLE);

                } else {
                    typingtv.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                Typing();
            }

            @Override
            public void afterTextChanged(Editable editable) {

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
                            Activity activityContext = MessageActivity.this;
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





    private void Typing() {

        typingchecker = true;

        typingref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (typingchecker.equals(true)) {
                    if (snapshot.child(receiver_uid).hasChild(sender_uid)) {
                        typingchecker = false;
                    } else {
                        typingref.child(receiver_uid).child(sender_uid).setValue(true);
                        typingchecker = false;
                    }

                } else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        // İlk olarak requestCode ve resultCode'un doğru olduğunu kontrol edin
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            // Daha sonra data'nın ve data.getData()'nın null olmadığını kontrol edin
            if (data != null && data.getData() != null) {
                uri = data.getData();

                String url = uri.toString();
                Intent intent = new Intent(MessageActivity.this, SendImage.class);
                intent.putExtra("u", url);
                intent.putExtra("n", receiver_name);
                intent.putExtra("ruid", receiver_uid);
                intent.putExtra("suid", sender_uid);
                startActivity(intent);
            } else {
                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<MessageMember> options1 =
                new FirebaseRecyclerOptions.Builder<MessageMember>()
                        .setQuery(rootref1, MessageMember.class)
                        .build();

        FirebaseRecyclerAdapter<MessageMember, MessageViewHolder> firebaseRecyclerAdapter1 =
                new FirebaseRecyclerAdapter<MessageMember, MessageViewHolder>(options1) {
                    @Override
                    protected void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull MessageMember model) {

                        holder.Setmessage(getApplication(), model.getMessage(), model.getTime(), model.getDate(), model.getType(),
                                model.getSenderuid(), model.getReceiveruid(), model.getSendername(), model.getAudio(), model.getImage());

                        long delete = getItem(position).getDelete();
                        String type = getItem(position).getType();
                        String imageuri = getItem(position).getImage();
                        String date = getItem(position).getDate();
                        String time = getItem(position).getTime();
                        String sendername = getItem(position).getSendername();


                        holder.sendertv.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                createMessageDialog(delete, type, imageuri, date, time, sendername);

                                return false;
                            }
                        });
                        holder.iv_sender.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                createMessageDialog(delete, type, imageuri, date, time, sendername);

                                return false;
                            }
                        });

                        holder.receivertv.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                createMessageDialog(delete, type, imageuri, date, time, sendername);

                                return false;
                            }
                        });
                        holder.iv_receiver.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                createMessageDialog(delete, type, imageuri, date, time, sendername);

                                return false;
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.message_layout, parent, false);

                        return new MessageViewHolder(view);
                    }
                };

        firebaseRecyclerAdapter1.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter1);

    }

    private void createMessageDialog(long delete, String type, String imageuri, String date, String time, String sendername) {

        final Dialog dialog = new Dialog(MessageActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.message_options);

        TextView unsend = dialog.findViewById(R.id.unsend_id);
        TextView details = dialog.findViewById(R.id.details_id);
        TextView download = dialog.findViewById(R.id.option1_id);
        TextView datetv = dialog.findViewById(R.id.date_mo);
        TextView timetv = dialog.findViewById(R.id.time_mo);


        if (type.equals("t")) {
            download.setVisibility(View.GONE);
        } else {
            download.setVisibility(View.VISIBLE);
        }

        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                datetv.setVisibility(View.VISIBLE);
                timetv.setVisibility(View.VISIBLE);
                datetv.setText("Date :" + date);
                timetv.setText("Time :" + time);

            }
        });

        unsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (type.equals("t")) {
                    Query rootref = rootref1.orderByChild("delete").equalTo(delete);
                    rootref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                                dataSnapshot1.getRef().removeValue();

                                Toast.makeText(MessageActivity.this, "deleted", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    Query rootrefdel = rootref2.orderByChild("delete").equalTo(delete);
                    rootrefdel.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                                dataSnapshot1.getRef().removeValue();

                                Toast.makeText(MessageActivity.this, "deleted", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    dialog.dismiss();


                } else if (type.equals("i")) {


                    Query rootref = rootref1.orderByChild("delete").equalTo(delete);
                    rootref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                                dataSnapshot1.getRef().removeValue();

                                Toast.makeText(MessageActivity.this, "deleted", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    Query rootrefdel = rootref2.orderByChild("delete").equalTo(delete);
                    rootrefdel.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                                dataSnapshot1.getRef().removeValue();

                                Toast.makeText(MessageActivity.this, "deleted", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                    StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(imageuri);
                    reference.delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    Toast.makeText(MessageActivity.this, "deleted", Toast.LENGTH_SHORT).show();


                                    dialog.dismiss();
                                }
                            });
                } else if (type.equals("a")) {


                    Query rootref = rootref1.orderByChild("delete").equalTo(delete);
                    rootref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                                dataSnapshot1.getRef().removeValue();

                                Toast.makeText(MessageActivity.this, "deleted", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    Query rootrefdel = rootref2.orderByChild("delete").equalTo(delete);
                    rootrefdel.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                                dataSnapshot1.getRef().removeValue();

                                Toast.makeText(MessageActivity.this, "deleted", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });


        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (!Environment.isExternalStorageManager()) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    } else {
                        startDownloadProcess(imageuri);
                    }
                }else {
                    // TedPermission ile izin isteme işlemi
                    TedPermission.with(getApplicationContext())
                            .setPermissionListener(new PermissionListener() {
                                @Override
                                public void onPermissionGranted() {
                                    // İzinler verildiğinde indirme işlemi başlatılacak
                                    startDownloadProcess(imageuri);
                                }

                                @Override
                                public void onPermissionDenied(List<String> deniedPermissions) {
                                    // İzinler reddedildiğinde kullanıcıya bilgi verilecek
                                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                            .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                            .check();
                }

                        }


        });


        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.Bottomanim;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }


    private void startDownloadProcess(String url) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle("MessagePicDownload");
        String appNameDownload = getResources().getString(R.string.app_name);

        request.setDescription(appNameDownload);
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        String fileExtension = ".jpg";
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,  + System.currentTimeMillis() + fileExtension);
        DownloadManager manager = (DownloadManager) getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
        Toast.makeText(getApplicationContext(), "Downloading", Toast.LENGTH_SHORT).show();
    }

    String sender_name;

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void SendMessage() {
        String message = messageEt.getText().toString().trim();

        Calendar cdate = Calendar.getInstance();
        SimpleDateFormat currentdate = new SimpleDateFormat("dd-MMMM-yyyy");
        final String savedate = currentdate.format(cdate.getTime());

        Calendar ctime = Calendar.getInstance();
        SimpleDateFormat currenttime = new SimpleDateFormat("HH:mm:ss");
        final String savetime = currenttime.format(ctime.getTime());

        String time = savedate + ":" + savetime;

        if (message.isEmpty()) {
            Toast.makeText(this, "Cannot send empty message", Toast.LENGTH_SHORT).show();
            return;
        }
        if (message.length() > 80) {
            messageEt.setError("max 80 character");
        } else {
            // Firebase Database referansını alın
            DatabaseReference allUsersRef = FirebaseDatabase.getInstance().getReference("All Users");
            // Kullanıcı ID'sini belirleyin (örnek olarak "userId")
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            // Kullanıcı adını sorgulayın
            allUsersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        sender_name = dataSnapshot.child("name").getValue(String.class);
                        long deletetime = System.currentTimeMillis();
                        messageMember.setSendername(sender_name);
                        messageMember.setDate(savedate);
                        messageMember.setTime(savetime);
                        messageMember.setMessage(message);
                        messageMember.setReceiveruid(receiver_uid);
                        messageMember.setSenderuid(sender_uid);
                        messageMember.setType("t");
                        messageMember.setDelete(deletetime);

                        String id = rootref1.push().getKey();
                        rootref1.child(id).setValue(messageMember);

                        String id1 = rootref2.push().getKey();
                        rootref2.child(id1).setValue(messageMember);

                        sendNotification(receiver_uid, messageMember.getSendername(), message);
                        messageEt.setText("");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Hata işleme
                }
            });
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        typingref.child(receiver_uid).child(sender_uid).removeValue();
    }


    private void sendNotification(String receiver_uid, String sender_name, String message) {

        FirebaseDatabase.getInstance().getReference("Token").child(receiver_uid).child("token")
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
                        new FcmNotificationsSender(usertoken, "new message", sender_name + ": " + message,
                                getApplicationContext(), MessageActivity.this);

                notificationsSender.SendNotifications(fcmKey);

            }
        }, 3000);

    }

}