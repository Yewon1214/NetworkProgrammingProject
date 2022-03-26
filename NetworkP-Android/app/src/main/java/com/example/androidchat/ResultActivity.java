package com.example.androidchat;

import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidchat.databinding.ActivityResultBinding;

public class ResultActivity extends AppCompatActivity {

    public ActivityResultBinding activityResultBinding;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityResultBinding = ActivityResultBinding.inflate(getLayoutInflater());
        setContentView(activityResultBinding.getRoot());
        activityResultBinding.gotomain.setOnClickListener(new retrybtnClickListener());

        Intent intent = getIntent();
        activityResultBinding.myid.setText(intent.getStringExtra("my id")+" ");
        activityResultBinding.myscore.setText(intent.getStringExtra("my score"));
        activityResultBinding.yourid.setText(intent.getStringExtra("your id")+" ");
        activityResultBinding.yourscore.setText( intent.getStringExtra("your score"));


    }


    class retrybtnClickListener implements View.OnClickListener{
        public void onClick(View v){
            Intent intent=new Intent(ResultActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

}
