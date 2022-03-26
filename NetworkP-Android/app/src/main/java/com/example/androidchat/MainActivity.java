package com.example.androidchat;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.Layout;
import android.text.Selection;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import android.graphics.Color;
import android.view.View;

import com.example.androidchat.databinding.ActivityLoginBinding;
import com.example.androidchat.databinding.ActivityMainBinding;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {
    public Button button;
    public Button button2;
    public ActivityMainBinding activityMainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());

        button=activityMainBinding.button;
        button2=activityMainBinding.button2;

        button.setOnClickListener(new BtnClickListener());
        button2.setOnClickListener(new BtnClickListener2());

    }

    class BtnClickListener implements View.OnClickListener{
        public void onClick(View v){
            Intent intent=new Intent(MainActivity.this, LoginActivity.class);
            startActivityForResult(intent, 0);
        }
    }

    class BtnClickListener2 implements View.OnClickListener{
        public void onClick(View v){
            Intent intent=new Intent(MainActivity.this, GameActivity.class);
            intent.putExtra("username", activityMainBinding.username.getText().toString());
            startActivity(intent);
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode==0&&resultCode==RESULT_OK){
            String userName=data.getStringExtra("username");
            activityMainBinding.username.setText(userName);

        }
        super.onActivityResult(requestCode, resultCode, data);
    }



}