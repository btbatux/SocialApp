package com.hicome.loveday;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class TermsAndConditionsActivity extends AppCompatActivity {

    TextView tv_terms_content;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions);


        tv_terms_content = findViewById(R.id.tv_terms_content);


        tv_terms_content.setText(R.string.your_terms_and_conditions);

    }
}