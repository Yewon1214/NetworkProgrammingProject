package com.example.androidchat;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.CountDownTimer;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.Layout;
import android.text.Selection;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
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
import java.util.ArrayList;

import android.graphics.Color;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.example.androidchat.databinding.ActivityGameBinding;

import petrov.kristiyan.colorpicker.ColorPicker;

import static java.sql.DriverManager.println;


public class GameActivity extends AppCompatActivity {

    public EditText txtInput;
    public TextView txtUserName;
    public ScrollView scrollView;
    public TextView txtView;
    public Button btnEnter, btnSend, btnStart;
    public Socket socket;
    public ObjectInputStream ois;
    public ObjectOutputStream oos;
    public boolean ServerStatus = false;
    public boolean ReadyStatus=false;
    public ImageView imgView;
    private MyView myView;
    public ActivityGameBinding gameBinding;
    public Button sendbtn;
    public Button erasebtn;
    public TextView count_view;
    public Button colorbtn;
    public TextView myscore, yourscore;

    public Button skipbtn, hintbtn;
    private static final long TIMER_DURATION = 10000L;
    private static final long TIMER_INTERVAL = 1000L;
    private long mTimeRemaining;

    private CountDownTimer mCountDownTimer;

    String UserName;
    final String ip_addr = "10.0.2.2"; // Emulator PC의 127.0.0.1
    //final String ip_addr = "172.30.1.54"; // 실제 Phone으로 테스트 할 때는 이렇게 설정한다.

    final int port_no = 30000;

    @Override
    protected void onPause() {
        count_view.setText("");
        //count_view.setVisibility(View.INVISIBLE);
        super.onPause();
        mCountDownTimer.cancel();
        mCountDownTimer = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameBinding=ActivityGameBinding.inflate(getLayoutInflater());
        setContentView(gameBinding.getRoot());
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        ImageView imageView2 = (ImageView) findViewById(R.id.imageView2);
        GlideDrawableImageViewTarget gifImage = new GlideDrawableImageViewTarget(imageView);
        GlideDrawableImageViewTarget gifImage2 = new GlideDrawableImageViewTarget(imageView2);
        Glide.with(this).load(R.drawable.buggigiff).into(gifImage);
        Glide.with(this).load(R.drawable.buggigiff).into(gifImage2);

        btnStart=(Button) findViewById(R.id.btnStart);
        btnEnter = (Button) findViewById(R.id.btnEnter);
        btnSend = (Button) findViewById(R.id.btnSend);
        txtUserName = (TextView) findViewById(R.id.txtUserName);
        txtInput = (EditText) findViewById(R.id.txtInput);
        txtView = (TextView) findViewById(R.id.txtView);
        myView = (MyView)findViewById(R.id.myView);
        sendbtn=(Button)findViewById(R.id.imgsend) ;
        imgView=(ImageView)findViewById(R.id.imgView);
        erasebtn=(Button)findViewById(R.id.erasebtn);
        count_view=(TextView)findViewById(R.id.timer);
        colorbtn=(Button)findViewById(R.id.color_change);
        myscore=(TextView)findViewById(R.id.myscore);
        yourscore=(TextView)findViewById(R.id.yourscore);
        skipbtn=(Button)findViewById(R.id.skipbtn);
        CountDownTimer timer;
        hintbtn=(Button)findViewById(R.id.hintbtn);

        txtView.setMovementMethod(new ScrollingMovementMethod());
        btnStart.setOnClickListener(new GameActivity.BtnStartClickListener());
        btnEnter.setOnClickListener( new GameActivity.BtnEnterClickListener());
        txtInput.setOnEditorActionListener(new GameActivity.TxtInputAction());
        btnSend.setOnClickListener(new GameActivity.BtnSendClickListener());

        sendbtn.setOnClickListener(new ImgSendClickListener());
        erasebtn.setOnClickListener(new EraseClickListener());
        colorbtn.setOnClickListener(new ColorChangeClickListener());
        skipbtn.setOnClickListener(new SkipClickListener());
        hintbtn.setOnClickListener(new HintClickListener());

        Intent data=getIntent();
        String str=data.getStringExtra("username");
        txtUserName.setText(str);


        mCountDownTimer = new CountDownTimer(TIMER_DURATION, TIMER_INTERVAL) {

            @SuppressLint("ResourceAsColor")
            @Override
            public void onTick(long millisUntilFinished) { // 총 시간과 주기
                count_view.setText(String.format(Locale.getDefault(), "%d 초", millisUntilFinished / 1000L));
                mTimeRemaining = millisUntilFinished;


            }

            @Override
            public void onFinish() {
                count_view.setText("!TIME OVER!");
                ChatMsg cm = new ChatMsg(UserName, "600", "TimeOver");
                SendChatMsg(cm);

            }
        };
    }



    // 문자열을 추가하고 자동 스크롤
    public synchronized void AppendText(String str) {
        String msg = str.trim();
        if(txtView != null){
            txtView.append(msg + "\n");
            final Layout layout = txtView.getLayout();
            if(layout != null){
                int scrollDelta = layout.getLineBottom(txtView.getLineCount() - 1)
                        - txtView.getScrollY() - txtView.getHeight();
                if(scrollDelta > 0)
                    txtView.scrollBy(0, scrollDelta);
            }
        }
    }

    // keyboard 사라지게 한다.
    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(
                this.getCurrentFocus().getWindowToken(), 0);
    }





    public class ColorChangeClickListener extends Activity implements View.OnClickListener {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onClick(View v) {
            openColorPicker();
        }
    }

    private void openColorPicker() {
        final ColorPicker colorPicker = new ColorPicker(this);
        ArrayList<String> colors = new ArrayList<>();

        colors.add("#ffab91");
        colors.add("#F48FB1");
        colors.add("#ce93d8");
        colors.add("#b39ddb");
        colors.add("#9fa8da");
        colors.add("#90caf9");
        colors.add("#81d4fa");
        colors.add("#80deea");
        colors.add("#80cbc4");
        colors.add("#c5e1a5");
        colors.add("#e6ee9c");
        colors.add("#fff59d");
        colors.add("#ffe082");
        colors.add("#ffcc80");
        colors.add("#bdaaa4");

        colorPicker.setColors(colors)
                .setColumns(5)
                .setRoundColorButton(true)
                .setOnChooseColorListener(new ColorPicker.OnChooseColorListener(){
                    @Override
                    public void onChooseColor(int position, int color) {
                        myView.paint.setColor(color);
                    }

                    @Override
                    public void onCancel() {

                    }}).show();


    }

    // Enter button 처리
    public class BtnEnterClickListener implements View.OnClickListener {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onClick(View v) {
            if (ServerStatus == false) {
                btnEnter.setText("나가기");
                Enter();
            }
            else {
                btnEnter.setText("입장하기");
                Exit();
            }
        }
    }

    public class ImgSendClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {

            String filename=UserName;
            ByteArrayOutputStream bytearray=new ByteArrayOutputStream();
            Bitmap bm=myView.getMbitmap();
            bm.compress(Bitmap.CompressFormat.JPEG, 100, bytearray);
            byte[] imgbytes=bytearray.toByteArray();

            ChatMsg cm=new ChatMsg(UserName, "700", filename);
            cm.imgbytes=imgbytes;
            SendChatMsg(cm);
            gameBinding.timer.setVisibility(View.INVISIBLE);

        }
    }



    //Start button 처리
    public class BtnStartClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (ServerStatus == true && ReadyStatus==false) {
                ReadyStatus=true;
                btnStart.setText("준비");
                ChatMsg obj = new ChatMsg(UserName,"200", "R"); //Ready
                SendChatMsg(obj);
            }
            else if(ServerStatus==true && ReadyStatus==true){
                ReadyStatus=false;
                btnStart.setText("준비해제");
                ChatMsg obj = new ChatMsg(UserName,"200", "N");  //Not Ready
                SendChatMsg(obj);
            }

        }

    }


    public synchronized void Enter() {
        new Thread() {
            public void run() {
                try {
                    //AppendText("Login " + ip_addr + " " + port_no  + "\n");
                    socket = new Socket(ip_addr, port_no);
                    oos = new ObjectOutputStream(socket.getOutputStream());
                    //oos.flush();
                    ois = new ObjectInputStream(socket.getInputStream());
                    UserName = txtUserName.getText().toString();
                    ChatMsg obj = new ChatMsg(UserName,"100", "Hello");
                    SendChatMsg(obj);
                    ServerStatus = true;
                    DoReceive(); // Server에서 읽는 Thread 실행
                } catch (IOException e) {
                    e.printStackTrace();
                    AppendText("Socket() failed");
                }
            }
        }.start();
    }

    public void ViewImageBytes(byte[] bytes){
        Bitmap bm = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
        imgView.post(new Runnable()
        {
            public void run()
            {   imgView.setVisibility(View.VISIBLE);
                imgView.setImageBitmap(bm);
            }
        });
    }

    // Server Message 수신
    public void DoReceive() {

        new Thread() {
            public void run() {

                while (true && ServerStatus) {
                    ChatMsg cm=new ChatMsg("","","");

                    try {

                        cm.code = (String) ois.readObject();
                        cm.UserName = (String) ois.readObject();
                        cm.data = (String) ois.readObject();
                        if (cm.code.matches("700")) {

                            cm.imgbytes = (byte[]) ois.readObject();
                            ViewImageBytes(cm.imgbytes);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    mCountDownTimer.start();
                                    gameBinding.timer.setVisibility((View.VISIBLE));
                                }
                            });

                        }

                        else if(cm.code.matches("250")){
                            runOnUiThread(new Runnable(){
                                @Override
                                public void run() {

                                    gameBinding.word.setText(cm.data);
                                    imgView.setVisibility(View.GONE);
                                    gameBinding.imgsend.setVisibility(View.VISIBLE);
                                    gameBinding.timer.setVisibility(View.INVISIBLE);
                                }
                            });

                        }
                        else if(cm.code.matches("500")){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(cm.UserName.matches(txtUserName.getText().toString()))
                                        gameBinding.hint.setText(cm.data);

                                }
                            });
                        }
                        else if(cm.code.matches("600")){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    gameBinding.word.setText("");
                                    myView.erase();
                                    imgView.setVisibility(View.GONE);
                                    gameBinding.imgsend.setVisibility(View.INVISIBLE);
                                    gameBinding.hint.setText("");
                                }
                            });
                        }
                        else if(cm.code.matches("800")){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(cm.UserName.matches(txtUserName.getText().toString())) {  //myid
                                        gameBinding.myid.setText(cm.UserName);
                                        gameBinding.myscore.setText(cm.data);

                                    }
                                    else{ //yourid
                                        gameBinding.yourid.setText(cm.UserName);
                                        gameBinding.yourscore.setText(cm.data);
                                    }
                                }
                            });
                        }
                        else if(cm.code.matches("900")){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(cm.UserName.matches(txtUserName.getText().toString())) {  //myid
                                        Intent intent=new Intent(GameActivity.this, ResultActivity.class);
                                        intent.putExtra("my id",gameBinding.myid.getText().toString());
                                        intent.putExtra("my score",gameBinding.myscore.getText().toString());
                                        intent.putExtra("your id",gameBinding.yourid.getText().toString());
                                        intent.putExtra("your score",gameBinding.yourscore.getText().toString());
                                        startActivity(intent);

                                    }
                                    else{
                                        Intent intent=new Intent(GameActivity.this, LoseActivity.class);
                                        intent.putExtra("my id",gameBinding.myid.getText().toString());
                                        intent.putExtra("my score",gameBinding.myscore.getText().toString());
                                        intent.putExtra("your id",gameBinding.yourid.getText().toString());
                                        intent.putExtra("your score",gameBinding.yourscore.getText().toString());
                                        startActivity(intent);

                                    }
                                }
                            });
                        }
                        else{
                            if(cm.data.matches("정답입니다.")){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        gameBinding.word.setText("");
                                        myView.erase();
                                        imgView.setVisibility(View.GONE);
                                        gameBinding.imgsend.setVisibility(View.INVISIBLE);
                                        gameBinding.hint.setText("");
                                        mCountDownTimer.cancel();
                                        gameBinding.timer.setVisibility((View.INVISIBLE));
                                    }
                                });
                            }
                            AppendText(cm.UserName + cm.data);
                        }

                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }.start();
    }


    public synchronized void Exit() {
        new Thread() {
            public void run() {
                try {
                    //AppendText("Logout"  + "\n");
                    ChatMsg obj = new ChatMsg(UserName,"400", "Bye");
                    SendChatMsg(obj);
                    oos.close();
                    ois.close();
                    socket.close();
                    ServerStatus = false;
                } catch (IOException e) {
                    e.printStackTrace();
                    AppendText("Socket() connect failed");
                }
            }
        }.start();
        finish();
    }


    // Text 입력중 Enter key 처리
    class TxtInputAction implements EditText.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            String msg;
            msg = txtInput.getText().toString();
            if (msg.length()==0)
                return false;
            hideKeyboard(); // 호출하면 Software 키가 내려간다.
            ChatMsg cb = new ChatMsg(UserName, "300", msg);
            SendChatMsg(cb);
            txtInput.setText("");
            return false;
        }
    }

    // Text Send button 처리
    class BtnSendClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            String msg;
            msg = txtInput.getText().toString();
            if (msg.length()==0)
                return;
            hideKeyboard();
            ChatMsg cb = new ChatMsg(UserName, "300", msg);
            SendChatMsg(cb);
            txtInput.setText("");

        }
    }


    // SendChatMsg()
    public synchronized void SendChatMsg(ChatMsg cm)  {
        new Thread() {
            public void run() {
                // Java 호환성을 위해 각각의 Field를 따로따로 보낸다.
                try {
                    oos.writeObject(cm.code);
                    oos.writeObject(cm.UserName);
                    oos.writeObject(cm.data);
                    if(cm.code.equals("700"))
                        oos.writeObject(cm.imgbytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    public class EraseClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            myView.erase();
            imgView.setVisibility(View.GONE);

        }
    }
    public class SkipClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            ChatMsg cm=new ChatMsg(UserName, "600", "포기했습니다.");
            SendChatMsg(cm);
            gameBinding.word.setText("");
            myView.erase();
            imgView.setVisibility(View.GONE);
            gameBinding.imgsend.setVisibility(View.INVISIBLE);
            mCountDownTimer.cancel();
            gameBinding.timer.setVisibility((View.INVISIBLE));
        }
    }

    private class HintClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            ChatMsg cm=new ChatMsg(UserName, "500", "힌트요청");
            SendChatMsg(cm);
        }
    }
}