package com.example.virtualwallet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        int num=1;
        DBHelper dbHelper=new DBHelper(SplashScreen.this);
        Cursor cursor=dbHelper.tablecheck();
        while(cursor.moveToNext()){
            num=Integer.parseInt(cursor.getString(0));
        }
        if(num==0){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent=new Intent(SplashScreen.this,onBoard.class);
                    startActivity(intent);
                    finish();
                }
            },3000);

        }
        else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent=new Intent(SplashScreen.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            },3000);
        }


    }
}