package com.example.virtualwallet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;

public class onBoard extends AppCompatActivity {
EditText name,balance;
Button next;
Boolean check=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_board);
        name=findViewById(R.id.editTextTextPersonName);
        balance=findViewById(R.id.editTextTextPersonName2);
        next=findViewById(R.id.button2);

        DBHelper dbHelper=new DBHelper(onBoard.this);


        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                check=true;
               if(name.getText().toString().isEmpty()){
                   name.setError("Name Required to make wallet");
                   check=false;
               }
                if(balance.getText().toString().isEmpty()){
                    balance.setError("Balance Required to make wallet");
                    check=false;
                }
                if(check==true){
                   if(dbHelper.insertperson(name.getText().toString().trim(),balance.getText().toString().trim())){
                       Intent intent=new Intent(onBoard.this,MainActivity.class);
                       startActivity(intent);
                       finish();
                   }
                   else{
                       Toast.makeText(onBoard.this, "Database Error!", Toast.LENGTH_SHORT).show();
                   }
                }

            }
        });

    }
}