package com.hicome.loveday;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth;
    DatabaseReference checkVideocallRef;
    String senderuid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        try {

            SharedPreferences sharedPreferences = getSharedPreferences("SharedPrefs",MODE_PRIVATE);

            final boolean isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn",false);

            if (isDarkModeOn){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

            }else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

            }
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

        }

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                        //    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            Toast.makeText(MainActivity.this, "Token is missing", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        FirebaseDatabase.getInstance().getReference("Token").child(uid).child("token").setValue(token);

                    }
                });


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(onNav);

        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,
                new Fragment1()).commit();






    }

    private String currentFragmentTag = null; // Şu an gösterilen fragment'in tag'ını tutacak

    private BottomNavigationView.OnNavigationItemSelectedListener onNav = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            String tag = null; // Seçilen fragment için bir tag tanımlayın
            Fragment selected = null;

            switch (item.getItemId()){
                case R.id.profile_bottom:
                    tag = "FRAGMENT_1";
                    selected = new Fragment1();
                    break;

                case R.id.ask_bottom:
                    tag = "FRAGMENT_2";
                    selected = new Fragment2();
                    break;

                case R.id.queue_bottom:
                    tag = "FRAGMENT_3";
                    selected = new Fragment3();
                    break;

                case R.id.home_bottom:
                    tag = "FRAGMENT_4";
                    selected = new Fragment4();
                    break;
            }

            // Eğer şu anki fragment bu fragment ise, yeniden yükleme yapmayın
            if(tag.equals(currentFragmentTag)) {
                return true; // Bu, fragment'in yeniden yüklenmesini engeller
            }

            // Yeni seçilen fragment'i yükle ve şu anki fragment tag'ini güncelle
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, selected, tag).commit();
            currentFragmentTag = tag; // Şu anki fragment'in tag'ini güncelleyin

            return true;
        }
    };

}
