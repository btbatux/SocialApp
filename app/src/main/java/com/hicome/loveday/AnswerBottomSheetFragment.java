package com.hicome.loveday;

import static android.service.controls.ControlsProviderService.TAG;

import android.app.Activity;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AnswerBottomSheetFragment extends BottomSheetDialogFragment {

    String uid, postkey; // Gerekli verileri tutacak değişkenler
    Button btn_answer_submit;
    EditText answer_edt;
    TextView maxLengthTv;
    NewMember newMember;
    AnswerMember member;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference Allquestions,ntref;
    String senderuid,userid,time,name,url,usertoken;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public String fcmKey;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Bottom Sheet'in layout'unu inflate et
        View view = inflater.inflate(R.layout.activity_answer, container, false);

        newMember = new NewMember();
        member = new AnswerMember();

        // UI component'lerini initialize et ve olay dinleyicilerini ayarla
        // Örneğin: EditText, Button vb.
        btn_answer_submit = view.findViewById(R.id.btn_answer_submit);
        answer_edt = view.findViewById(R.id.answer_edt);
         maxLengthTv = view.findViewById(R.id.maxlenght);


        // Bundle'dan gerekli verileri al (eğer varsa)
        if (getArguments() != null) {
            uid = getArguments().getString("u");
            postkey = getArguments().getString("p");
        }

        Allquestions = database.getReference("AllQuestions").child(postkey).child("Answer");
        ntref = database.getReference("notification").child(uid);

        btn_answer_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitAnswer();

            }
        });

        // TextWatcher ile EditText'i izle
        answer_edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Bu metodun içeriği boş bırakılabilir
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int inputLength = s.length();
                maxLengthTv.setText(inputLength + "/75");
                if (inputLength > 75) {
                    maxLengthTv.setTextColor(getResources().getColor(R.color.red)); // R.color.red, colors.xml dosyanızda tanımlı olmalıdır.
                    maxLengthTv.setText("Limit exceeded! " + inputLength + "/75");
                    btn_answer_submit.setEnabled(false);
                } else {
                    maxLengthTv.setTextColor(getResources().getColor(R.color.black)); // R.color.black, colors.xml dosyanızda tanımlı olmalıdır.
                    btn_answer_submit.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Bu metodun içeriği boş bırakılabilir
            }
        });

        return view;

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void submitAnswer() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userid = user.getUid();
        String answer = answer_edt.getText().toString().trim();


        if (!answer.isEmpty()) {

            Calendar cdate = Calendar.getInstance();
            SimpleDateFormat currentdate = new SimpleDateFormat("dd-MMMM-yyyy");
            final  String savedate = currentdate.format(cdate.getTime());

            Calendar ctime = Calendar.getInstance();
            SimpleDateFormat currenttime = new SimpleDateFormat("HH:mm:ss");
            final String savetime = currenttime.format(ctime.getTime());

            time = savedate +":"+ savetime;
            member.setAnswer(answer);
            member.setTime(time);
            member.setName(name);
            member.setUid(userid);
            member.setUrl(url);

            String id = Allquestions.push().getKey();
            Allquestions.child(id).setValue(member);

            newMember.setName(name);
            newMember.setText("Replied To your Question: " + answer);
            newMember.setSeen("no");
            newMember.setUid(userid);
            newMember.setUrl(url);

            String key = ntref.push().getKey();
            ntref.child(key).setValue(newMember);

            sendNotification(uid,name,answer);


            // Veritabanına kaydetme işlemi
            DatabaseReference answerRef = FirebaseDatabase.getInstance().getReference("Answers").child(postkey);
            String answerId = answerRef.push().getKey(); // Benzersiz bir ID oluştur
            Map<String, Object> answerMap = new HashMap<>();
            answerMap.put("answer", answer);
            answerMap.put("uid", uid); // Yanıtı gönderen kullanıcının UID'si
            // Diğer gerekli bilgileri de ekleyebilirsiniz

            answerRef.child(answerId).setValue(answerMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Answer submitted successfully", Toast.LENGTH_SHORT).show();
                        dismiss(); // BottomSheet'i kapat
                    } else {
                        Toast.makeText(getContext(), "Failed to submit answer", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(getContext(), "Answer cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userid = user.getUid();
        FirebaseFirestore d = FirebaseFirestore.getInstance();
        DocumentReference reference;
        reference = d.collection("user").document(userid);

        reference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.getResult().exists()){
                            url = task.getResult().getString("url");
                            name = task.getResult().getString("name");

                        }else {

                        }

                    }
                });

    }

    public static AnswerBottomSheetFragment newInstance(String uid, String postKey) {
        AnswerBottomSheetFragment fragment = new AnswerBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString("u", uid);
        args.putString("p", postKey);
        fragment.setArguments(args);
        return fragment;
    }

    private void sendNotification(String uid, String name, String answer){
        FirebaseDatabase.getInstance().getReference().child("Tokens").child(uid).child("token")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        usertoken = snapshot.getValue(String.class);
                        if (usertoken != null && getActivity() != null) { // getActivity() null kontrolü

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


                                    // getActivity() null kontrolü yapılıyor
                                    Activity activity = getActivity();
                                    if(activity != null){
                                        FcmNotificationsSender notificationsSender =
                                                new FcmNotificationsSender(usertoken, "Social Media", name + " Commented on your post: " + answer,
                                                        getContext(), activity); // getActivity() kullanımı

                                        notificationsSender.SendNotifications(fcmKey);
                                    }
                                }
                            },3000);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}
