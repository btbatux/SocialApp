package com.hicome.loveday;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.security.Permission;
import java.security.Permissions;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static android.service.controls.ControlsProviderService.TAG;


public class Fragment4 extends Fragment implements View.OnClickListener {
    /**
     * @author R10 btbatu - 10.Sub.2024
     */
    ImageButton createpost_f4;
    RecyclerView recyclerView;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference reference, likeref, storyRef, likelist, referenceDel, ntref;
    Boolean likechecker = false;
    DatabaseReference db1, db2, db3;
    String senderuid;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String currentuid = user.getUid();

    ReportClass reportClass;
    DocumentReference documentReference;
    NewMember newMember;
    LinearLayoutManager linearLayoutManager;
    Uri imageUri;
    private static final int PICK_IMAGE = 1;
    RecyclerView recyclerViewstory;
    String name_result, url_result, uid_result, usertoken;
    All_UserMmber userMmber;
    long lastClickTime;
    long debounceTime;

    private String adminEmail1;
    private String adminEmail2;
    private String adminEmail3;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public String fcmKey;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment4, container, false);
        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        createpost_f4 = getActivity().findViewById(R.id.createpost_f4);
        reference = database.getReference("All posts");
        likeref = database.getReference("post likes");


        reportClass = new ReportClass();
        storyRef = database.getReference("All story");
        referenceDel = database.getReference("story");


        recyclerView = getActivity().findViewById(R.id.rv_posts);
        recyclerView.setHasFixedSize(true);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentuid = user.getUid();

        documentReference = db.collection("user").document(currentuid);


        newMember = new NewMember();


        db1 = database.getReference("All images").child(currentuid);
        db2 = database.getReference("All videos").child(currentuid);
        db3 = database.getReference("All posts");
        db3.keepSynced(true);

        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);


        recyclerViewstory = getActivity().findViewById(R.id.rv_storyf4);
        recyclerViewstory.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewstory.setLayoutManager(linearLayoutManager);
        recyclerViewstory.setItemAnimator(new DefaultItemAnimator());

        createpost_f4.setOnClickListener(this);

        userMmber = new All_UserMmber();

        checkStory(currentuid);

        // String değerlerine erişim ve atama
        adminEmail1 = getResources().getString(R.string.admin_email_1);
        adminEmail2 = getResources().getString(R.string.admin_email_2);
        adminEmail3 = getResources().getString(R.string.admin_email_3);

    }


    @Override
    public void onClick(View view) {

        int id = view.getId();
        if (R.id.createpost_f4 == id) {
            showBottomsheet();
        }

    }

    private void checkStory(String currentuid) {
        referenceDel.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.hasChild(currentuid)) {

                } else {
                    Query query3 = storyRef.orderByChild("uid").equalTo(currentuid);
                    query3.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                                dataSnapshot1.getRef().removeValue();

                               // Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showBottomsheet() {

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.f4_bottomsheet);

        TextView tvcp = dialog.findViewById(R.id.tv_cpf4);
        TextView tvcs = dialog.findViewById(R.id.tv_csf4);


        tvcp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PostActivity.class);
                startActivity(intent);
            }
        });

        tvcs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentstory = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intentstory.setType("image/*");
                startActivityForResult(intentstory, PICK_IMAGE);
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.Bottomanim;
        dialog.getWindow().setGravity(Gravity.BOTTOM);


    }

    @Override
    public void onStart() {
        super.onStart();


        documentReference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.getResult().exists()) {
                            name_result = task.getResult().getString("name");
                            url_result = task.getResult().getString("url");
                            uid_result = task.getResult().getString("uid");

                        } else {

                        }

                    }
                });

        FirebaseRecyclerOptions<Postmember> options =
                new FirebaseRecyclerOptions.Builder<Postmember>()
                        .setQuery(reference, Postmember.class)
                        .build();

        FirebaseRecyclerAdapter<Postmember, PostViewholder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Postmember, PostViewholder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull PostViewholder holder, int position, @NonNull final Postmember model) {

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        final String currentUserid = user.getUid();


                        final String postkey = getRef(position).getKey();

                        holder.SetPost(getActivity(), model.getName(), model.getUrl(), model.getPostUri(), model.getTime()
                                , model.getUid(), model.getType(), model.getDesc());


                        final String url = getItem(position).getPostUri();
                        final String name = getItem(position).getName();
                        final String useruri = getItem(position).getUrl();
                        final String time = getItem(position).getTime();
                        final String type = getItem(position).getType();
                        final String userid = getItem(position).getUid();


                        holder.commentchecker(postkey);


                        holder.menuoptions.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showDialog(name, url, time, userid, type, postkey, useruri);
                            }
                        });

                        holder.tv_nameprofile.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (currentUserid.equals(userid)) {
                                    Intent intent = new Intent(getActivity(), MainActivity.class);
                                    startActivity(intent);

                                } else {
                                    Intent intent = new Intent(getActivity(), ShowUser.class);
                                    intent.putExtra("n", name);
                                    intent.putExtra("u", url);
                                    intent.putExtra("uid", userid);
                                    startActivity(intent);
                                }


                            }
                        });

                        holder.likeschecker(postkey);

                        holder.likebtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Debouncing için son tıklama zamanını tutan bir değişken.
                                lastClickTime = 0;
                                // Tıklamalar arası minimum bekleme süresi (örneğin, 500ms).
                                debounceTime = 10000;

                                // Eğer son tıklamadan bu yana yeterli zaman geçtiyse işlemi gerçekleştir.
                                if (System.currentTimeMillis() - lastClickTime > debounceTime) {
                                    lastClickTime = System.currentTimeMillis(); // Son tıklama zamanını güncelle

                                    ntref = database.getReference("notification").child(userid);
                                    likechecker = true;

                                    likeref.addListenerForSingleValueEvent(new ValueEventListener() { // addValueEventListener yerine addListenerForSingleValueEvent kullan
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                            if (likechecker.equals(true)) {
                                                if (snapshot.child(postkey).hasChild(currentUserid)) {
                                                    likeref.child(postkey).child(currentUserid).removeValue();
                                                    likelist = database.getReference("like list").child(postkey).child(currentUserid);
                                                    likelist.removeValue();
                                                    ntref.child(currentUserid + "l").removeValue();
                                                } else {

                                                    likeref.child(postkey).child(currentUserid).setValue(true);
                                                    likelist = database.getReference("like list").child(postkey);
                                                    userMmber.setName(name_result);
                                                    userMmber.setUid(currentUserid);
                                                    userMmber.setUrl(url_result);
                                                    likelist.child(currentUserid).setValue(userMmber);

                                                    newMember.setName(name_result);
                                                    newMember.setUid(currentUserid);
                                                    newMember.setUrl(url_result);
                                                    newMember.setSeen("no");
                                                    newMember.setText("Liked Your Post ");
                                                    ntref.child(currentUserid + "l").setValue(newMember);
                                                    sendNotification(userid, name_result);
                                                }
                                                likechecker = false; // Bu satırı döngünün sonuna taşıdık.
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                        }
                                    });
                                }
                            }
                        });


                        holder.tv_likes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(getActivity(), ShowLikedUser.class);
                                intent.putExtra("p", postkey);
                                startActivity(intent);
                            }
                        });

                        holder.commentbtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(getActivity(), CommentsActivity.class);
                                intent.putExtra("postkey", postkey);
                                intent.putExtra("name", name);
                                intent.putExtra("url", url);
                                intent.putExtra("uid", userid);
                                startActivity(intent);
                            }
                        });

                    }


                    @NonNull
                    @Override
                    public PostViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.post_layout, parent, false);

                        return new PostViewholder(view);


                    }


                };
        firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);


        // story firebase adapter
        FirebaseRecyclerOptions<StoryMember> options1 =
                new FirebaseRecyclerOptions.Builder<StoryMember>()
                        .setQuery(storyRef, StoryMember.class)
                        .build();

        FirebaseRecyclerAdapter<StoryMember, StoryViewHolder> firebaseRecyclerAdapterstory =
                new FirebaseRecyclerAdapter<StoryMember, StoryViewHolder>(options1) {
                    @Override
                    protected void onBindViewHolder(@NonNull StoryViewHolder holder, int position, @NonNull final StoryMember model) {

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        final String currentUserid = user.getUid();


                        holder.setStory(getActivity(), model.getPostUri(), model.getName(), model.getTimeEnd(), model.getTimeUpload()
                                , model.getType(), model.getCaption(), model.getUrl(), model.getUid());

                        String userid = getItem(position).getUid();
                        holder.imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(getActivity(), ShowStory.class);
                                intent.putExtra("u", userid);
                                startActivity(intent);

                            }
                        });


                    }

                    @NonNull
                    @Override
                    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.story_layout, parent, false);

                        return new StoryViewHolder(view);


                    }
                };
        firebaseRecyclerAdapterstory.startListening();

        recyclerViewstory.setAdapter(firebaseRecyclerAdapterstory);
    }

    void showDialog(String name, String url, String time, String userid, String type, String postkey, String useruri) {

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.post_options);

        //TextView download = dialog.findViewById(R.id.download_tv_post);
        TextView share = dialog.findViewById(R.id.share_tv_post);
        TextView delete = dialog.findViewById(R.id.delete_tv_post);
        TextView copyurl = dialog.findViewById(R.id.copyurl_tv_post);
        TextView edit = dialog.findViewById(R.id.edit_post);
        TextView reporttv = dialog.findViewById(R.id.report_tv_post);
        EditText captionEt = dialog.findViewById(R.id.et_caption);
        Button button = dialog.findViewById(R.id.btn_edit_caption);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserid = user.getUid();
        String email = user.getEmail();

        //MENU OPTİONS POST DELETE BUTONU
        if (userid.equals(currentUserid)) {
            delete.setVisibility(View.VISIBLE);
            edit.setVisibility(View.VISIBLE);
        } else if (email.equals(adminEmail1) || email.equals(adminEmail2) || email.equals(adminEmail3)) {
            delete.setVisibility(View.VISIBLE);
            edit.setVisibility(View.VISIBLE);
        } else {
            delete.setVisibility(View.GONE);
            edit.setVisibility(View.GONE);
        }


        reporttv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showReportsheet(name, url, useruri, userid, type, postkey, time);
            }
        });
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                captionEt.setVisibility(View.VISIBLE);
                button.setVisibility(View.VISIBLE);

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        Map<String, Object> map = new HashMap<>();
                        map.put("desc", captionEt.getText().toString());

                        FirebaseDatabase.getInstance().getReference()
                                .child("All posts")
                                .child(postkey)
                                .updateChildren(map)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                });
                    }
                });
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Query query = db1.orderByChild("time").equalTo(time);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                            dataSnapshot1.getRef().removeValue();

                            Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                Query query2 = db2.orderByChild("time").equalTo(time);
                query2.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                            dataSnapshot1.getRef().removeValue();

                            Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                Query query3 = db3.orderByChild("time").equalTo(time);
                query3.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                            dataSnapshot1.getRef().removeValue();

                            Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(url);
                reference.delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_SHORT).show();
                            }
                        });

                dialog.dismiss();

            }
        });

        //Post download
        /*download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (!Environment.isExternalStorageManager()) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    } else {
                        startDownloadProcess(type, url, name);
                    }
                } else {
                    // TedPermission ile izin isteme işlemi
                    TedPermission.with(getActivity())
                            .setPermissionListener(new PermissionListener() {
                                @Override
                                public void onPermissionGranted() {
                                    // İzinler verildiğinde indirme işlemi başlatılacak
                                    startDownloadProcess(type, url, name);
                                }

                                @Override
                                public void onPermissionDenied(List<String> deniedPermissions) {
                                    // İzinler reddedildiğinde kullanıcıya bilgi verilecek
                                    Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                            .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                            .check();
                }

                dialog.dismiss();
            }
        });*/

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String sharetext = name + "\n" + "\n" + url;
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Intent.EXTRA_TEXT, sharetext);
                intent.setType("text/plain");
                startActivity(intent.createChooser(intent, "share via"));

                dialog.dismiss();

            }
        });
        copyurl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ClipboardManager cp = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("String", url);
                cp.setPrimaryClip(clip);
                clip.getDescription();
                Toast.makeText(getActivity(), "Copy", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.Bottomanim;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    private void startDownloadProcess(String type, String url, String name) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle("Download");
        request.setDescription(type.equals("iv") ? "Downloading image...." : "Downloading video....");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        String fileExtension = type.equals("iv") ? ".jpg" : ".mp4";
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name + System.currentTimeMillis() + fileExtension);
        DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
        Toast.makeText(getActivity(), "Downloading", Toast.LENGTH_SHORT).show();
    }


    private void showReportsheet(String name, String url, String useruri, String userid,
                                 String type, String postkey, String time) {


        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.report_file_post);

        Button cancel = dialog.findViewById(R.id.cancel_report_post);
        Button submitreport = dialog.findViewById(R.id.submit_report_post);

        RadioButton sexualrb = dialog.findViewById(R.id.sexualTv_report);
        RadioButton violentrb = dialog.findViewById(R.id.violenTv_report);
        RadioButton hatefulrb = dialog.findViewById(R.id.hatefulTv_report);
        RadioButton harassmentrb = dialog.findViewById(R.id.harassmentTv_report);
        RadioButton childrb = dialog.findViewById(R.id.childTv_report);
        RadioButton infringesrb = dialog.findViewById(R.id.infringesTv_report);
        RadioButton spamrb = dialog.findViewById(R.id.spamTv_report);
        RadioButton terrorismrb = dialog.findViewById(R.id.terrorismTv_report);

        DatabaseReference reportRefPost = database.getReference("Report Post");

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.dismiss();

            }
        });


        submitreport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (sexualrb.isChecked()) {

                    reportClass.setIssue("Sexual Content");
                    reportClass.setName(name);
                    reportClass.setTime(time);
                    reportClass.setType(type);
                    reportClass.setUid(userid);
                    reportClass.setUrl(url);
                    reportClass.setUseruri(useruri);

                    String key = reportRefPost.push().getKey();
                    reportRefPost.child(key).setValue(reportClass).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getActivity(), "Reported Successfully", Toast.LENGTH_SHORT).show();
                        }
                    });


                } else if (violentrb.isChecked()) {

                    reportClass.setIssue("Violent of repulsive ");
                    reportClass.setName(name);
                    reportClass.setTime(time);
                    reportClass.setType(type);
                    reportClass.setUid(userid);
                    reportClass.setUrl(url);
                    reportClass.setUseruri(useruri);

                    String key = reportRefPost.push().getKey();
                    reportRefPost.child(key).setValue(reportClass).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getActivity(), "Reported Successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (hatefulrb.isChecked()) {

                    reportClass.setIssue("Hateful of abusive content");
                    reportClass.setName(name);
                    reportClass.setTime(time);
                    reportClass.setType(type);
                    reportClass.setUid(userid);
                    reportClass.setUrl(url);
                    reportClass.setUseruri(useruri);

                    String key = reportRefPost.push().getKey();
                    reportRefPost.child(key).setValue(reportClass).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getActivity(), "Reported Successfully", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else if (childrb.isChecked()) {

                    reportClass.setIssue("Child Abuse");
                    reportClass.setName(name);
                    reportClass.setTime(time);
                    reportClass.setType(type);
                    reportClass.setUid(userid);
                    reportClass.setUrl(url);
                    reportClass.setUseruri(useruri);

                    String key = reportRefPost.push().getKey();
                    reportRefPost.child(key).setValue(reportClass).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getActivity(), "Reported Successfully", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else if (infringesrb.isChecked()) {

                    reportClass.setIssue("Infringes my rights");
                    reportClass.setName(name);
                    reportClass.setTime(time);
                    reportClass.setType(type);
                    reportClass.setUid(userid);
                    reportClass.setUrl(url);
                    reportClass.setUseruri(useruri);

                    String key = reportRefPost.push().getKey();
                    reportRefPost.child(key).setValue(reportClass).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getActivity(), "Reported Successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (terrorismrb.isChecked()) {

                    reportClass.setIssue(" Promotes Terrorism ");
                    reportClass.setName(name);
                    reportClass.setTime(time);
                    reportClass.setType(type);
                    reportClass.setUid(userid);
                    reportClass.setUrl(url);
                    reportClass.setUseruri(useruri);

                    String key = reportRefPost.push().getKey();
                    reportRefPost.child(key).setValue(reportClass).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getActivity(), "Reported Successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (spamrb.isChecked()) {

                    reportClass.setIssue("Spam or misleading");
                    reportClass.setName(name);
                    reportClass.setTime(time);
                    reportClass.setType(type);
                    reportClass.setUid(userid);
                    reportClass.setUrl(url);
                    reportClass.setUseruri(useruri);

                    String key = reportRefPost.push().getKey();
                    reportRefPost.child(key).setValue(reportClass).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getActivity(), "Reported Successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (harassmentrb.isChecked()) {

                    reportClass.setIssue("Harassment Content");
                    reportClass.setName(name);
                    reportClass.setTime(time);
                    reportClass.setType(type);
                    reportClass.setUid(userid);
                    reportClass.setUrl(url);
                    reportClass.setUseruri(useruri);

                    String key = reportRefPost.push().getKey();
                    reportRefPost.child(key).setValue(reportClass).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getActivity(), "Reported Successfully", Toast.LENGTH_SHORT).show();
                        }
                    });

                }


            }
        });


        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.Bottomanim;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {

            if (requestCode == PICK_IMAGE || resultCode == RESULT_OK ||
                    data != null || data.getData() != null) {
                imageUri = data.getData();

                String url = imageUri.toString();
                Intent intent = new Intent(getActivity(), StoryActivity.class);
                intent.putExtra("u", url);
                startActivity(intent);
            } else {
                Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {

            Toast.makeText(getActivity(), "error" + e, Toast.LENGTH_SHORT).show();
        }


    }

    private void sendNotification(String userid, String name_result) {

        FirebaseDatabase.getInstance().getReference("Token").child(userid).child("token")
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
                        new FcmNotificationsSender(usertoken, "new like", name_result + " Liked Your post ",
                                getContext(), getActivity());

                notificationsSender.SendNotifications(fcmKey);

            }
        }, 3500);

    }

}
