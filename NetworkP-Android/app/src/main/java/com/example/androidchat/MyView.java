package com.example.androidchat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MyView extends View
{
    Paint paint = new Paint();
    Canvas canvas = new Canvas();
    Bitmap mbitmap;

    int oldX,oldY = -1;

    public MyView(Context context)
    {
        super(context);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10f);

        canvas = new Canvas();

        mbitmap = Bitmap.createBitmap(1200,700, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(mbitmap);
        canvas.drawColor(Color.WHITE);
    }

    public MyView(Context context, AttributeSet att)
    {
        super(context, att);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10f);

        canvas = new Canvas();

        mbitmap = Bitmap.createBitmap(1200,700, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(mbitmap);
        canvas.drawColor(Color.WHITE);
    }

    public MyView(Context context, AttributeSet att, int ref)
    {
        super(context, att, ref);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10f);

        canvas = new Canvas();

        mbitmap = Bitmap.createBitmap(1200,700, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(mbitmap);
        canvas.drawColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        if(mbitmap != null)
            canvas.drawBitmap(mbitmap,0,0,null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int X = (int)event.getX();
        int Y = (int)event.getY();

        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN :
                oldX = X; oldY = Y;
                break;

            case MotionEvent.ACTION_MOVE :
                if(oldX != -1)
                {
                    canvas.drawLine(oldX,oldY,X,Y,paint);
                    invalidate();
                    oldX = X; oldY = Y;
                }
                break;

            case MotionEvent.ACTION_UP :
                if(oldX != -1)
                {
                    canvas.drawLine(oldX,oldY,X,Y,paint);
                }
                invalidate();
                oldX = -1; oldY = -1;

                break;
        }

        //invalidate();
        return true;
    }


    public Bitmap getMbitmap(){
        return mbitmap;
    }

    public void erase()
    {
        canvas.drawColor(Color.WHITE);
    }
}