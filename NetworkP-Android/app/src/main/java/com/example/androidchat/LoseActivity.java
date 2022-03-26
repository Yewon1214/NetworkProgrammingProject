package com.example.androidchat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidchat.databinding.ActivityLoseBinding;

public class LoseActivity extends AppCompatActivity {

    public ActivityLoseBinding LoseBinding;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoseBinding = ActivityLoseBinding.inflate(getLayoutInflater());
        setContentView(LoseBinding.getRoot());
        LoseBinding.gotomain.setOnClickListener(new retrybtnClickListener());

        Intent intent = getIntent();
        LoseBinding.myid.setText(intent.getStringExtra("my id")+" ");
        LoseBinding.myscore.setText(intent.getStringExtra("my score"));
        LoseBinding.yourid.setText(intent.getStringExtra("your id")+" ");
        LoseBinding.yourscore.setText( intent.getStringExtra("your score"));

    }


    class retrybtnClickListener implements View.OnClickListener{
        public void onClick(View v){
            Intent intent=new Intent(LoseActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

}
