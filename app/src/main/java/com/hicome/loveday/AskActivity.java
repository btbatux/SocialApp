package com.hicome.loveday;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.annotation.SuppressLint;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class AskActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final int MAX_QUESTION_LENGTH = 75;
    String[] categories = {"Choose category", "Tech", "Health", "Education", "Food", "Sports", "News", "Fashion", "Beauty", "Lifestyle"};
    EditText questionEditText;
    TextView characterLimitAlert, categoryTextView;
    Button submitButton;
    Spinner categorySpinner;
    LinearLayout mainLayout;
    FirebaseDatabase database;
    DatabaseReference allQuestionsRef, userQuestionsRef;
    FirebaseFirestore firestore;
    DocumentReference userDocRef;
    String currentUserId, userName, userUrl, userPrivacy, selectedCategory;
    QuestionMember questionMember;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask);

        initializeFirebase();
        initializeUI();

        //MOBİL ADS REKLAM
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });

        //Reklam isteğini oluştur Geçişli
        AdRequest adRequest = new AdRequest.Builder().build();
        // Ara reklamı yükle  Geçişli
        InterstitialAd.load(this, "ca-app-pub-3940256099942544/1033173712", adRequest,
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
                            mInterstitialAd.show(AskActivity.this);
                        }
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.d("AdMob", "Ad failed to load.");
                        mInterstitialAd = null;
                    }
                });
    }


    private void initializeFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            firestore = FirebaseFirestore.getInstance();
            userDocRef = firestore.collection("user").document(currentUserId);
            database = FirebaseDatabase.getInstance();
            allQuestionsRef = database.getReference("AllQuestions");
            userQuestionsRef = database.getReference("UserQuestions").child(currentUserId);
            questionMember = new QuestionMember();
        }
    }

    private void initializeUI() {
        questionEditText = findViewById(R.id.ask_et_question);
        submitButton = findViewById(R.id.btn_submit);
        characterLimitAlert = findViewById(R.id.characterlimitalert);
        categoryTextView = findViewById(R.id.tv_cat);
        categorySpinner = findViewById(R.id.spinner_cat);
        mainLayout = findViewById(R.id.ll_ask);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(arrayAdapter);
        categorySpinner.setOnItemSelectedListener(this);

        submitButton.setOnClickListener(view -> submitQuestion());
        questionEditText.addTextChangedListener(new QuestionTextWatcher());
    }

    private void submitQuestion() {
        String question = questionEditText.getText().toString();
        if (validateQuestion(question)) {
            postQuestion(question);
        }
    }


    private boolean validateQuestion(String question) {
        if (question.trim().isEmpty()) {
            Toast.makeText(this, "Please ask a question", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (question.length() > MAX_QUESTION_LENGTH) {
            characterLimitAlert.setText("Character limit exceeded!");
            return false;
        }
        if (selectedCategory.equals("Choose category")) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!isNetworkConnected()) {
            Snackbar.make(mainLayout, "Not Connected", Snackbar.LENGTH_LONG)
                    .setAction("Turn On", view -> {
                    })
                    .setActionTextColor(getResources().getColor(R.color.retrycolor))
                    .show();
            return false;
        }
        return true;
    }

    private void postQuestion(String question) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMMM-yyyy");
        String date = dateFormat.format(Calendar.getInstance().getTime());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String time = timeFormat.format(Calendar.getInstance().getTime());

        questionMember.setQuestion(question);
        questionMember.setName(userName);
        questionMember.setPrivacy(userPrivacy);
        questionMember.setUrl(userUrl);
        questionMember.setUserid(currentUserId);
        questionMember.setTime(date + ":" + time);
        questionMember.setCategory(selectedCategory.toLowerCase());

        String questionId = userQuestionsRef.push().getKey();
        if (questionId != null) {
            userQuestionsRef.child(questionId).setValue(questionMember);
            allQuestionsRef.child(questionId).setValue(questionMember);
            Toast.makeText(AskActivity.this, "Question submitted", Toast.LENGTH_SHORT).show();
            questionEditText.setText("");
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    protected void onStart() {
        super.onStart();
        userDocRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot doc = task.getResult();
                        userName = doc.getString("name");
                        userUrl = doc.getString("url");
                        userPrivacy = doc.getString("privacy");
                    } else {
                        Toast.makeText(AskActivity.this, "Error: User data not found", Toast.LENGTH_SHORT).show();
                    }
                });




    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedCategory = parent.getItemAtPosition(position).toString();
        categoryTextView.setText(selectedCategory);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        selectedCategory = "Choose category";
    }

    private class QuestionTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            int length = s.length();
            characterLimitAlert.setText(String.format("%d/%d", length, MAX_QUESTION_LENGTH));
            if (length > MAX_QUESTION_LENGTH) {
                characterLimitAlert.setTextColor(getResources().getColor(R.color.red));
            } else {
                characterLimitAlert.setTextColor(getResources().getColor(R.color.black));
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }


}
