package com.example.androidchat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.example.androidchat.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    public ActivityLoginBinding activityLoginBinding;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityLoginBinding=ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(activityLoginBinding.getRoot());
        ImageView buggigiff = (ImageView) findViewById(R.id.gifView);
        GlideDrawableImageViewTarget gifImage = new GlideDrawableImageViewTarget(buggigiff);
        Glide.with(this).load(R.drawable.buggigiff).into(gifImage);

        activityLoginBinding.btnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent data=new Intent();
                data.putExtra("username", activityLoginBinding.txtUserName.getText().toString());
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }

}