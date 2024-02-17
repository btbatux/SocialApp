package com.hicome.loveday;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hicome.loveday.LoginActivity;
import com.hicome.loveday.MainActivity;


public class Splashscreen extends AppCompatActivity {

    ImageView imageView;
    TextView nameTv;
    long animTime = 2100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splashscreen);

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

        imageView = findViewById(R.id.iv_logo_splash);
        //name2Tv = findViewById(R.id.tv_splash_name2);
        nameTv = findViewById(R.id.tv_splash_name);

        ObjectAnimator animatorY = ObjectAnimator.ofFloat(imageView,"y",800f);
        //ObjectAnimator animatorname = ObjectAnimator.ofFloat(nameTv,"x",320f);
        animatorY.setDuration(animTime);
      //  animatorname.setDuration(animTime);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animatorY);
        animatorSet.start();
    }

    @Override
    protected void onStart() {
        super.onStart();

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (user != null){
                    Intent intent = new Intent(Splashscreen.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    Intent intent = new Intent(Splashscreen.this, LoginActivity.class);
                    startActivity(intent);
                }
            }
        },2500);
    }
}
