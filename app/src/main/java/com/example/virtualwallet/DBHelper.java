package com.example.virtualwallet;



import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context) {
        super(context, "PersonalInformation.db", null, 4);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create Table person(name TEXT, balance TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop Table if exists person");

    }

    public Boolean insertperson(String name,String balance) {
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name",name);
        contentValues.put("balance",balance);
        long result = DB.insert("person", null, contentValues);
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public Cursor getPersonData() {
        String query = "SELECT* FROM person";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        if (db != null) {
            cursor = db.rawQuery(query, null);
        }
        return cursor;

    }
    public void UpdateData(String newval,String where) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE  " + "person SET balance="+"'"+newval +"' WHERE " + "name" + "='" + where + "'");
        db.close();



    }


public Cursor tablecheck(){

    String query = "SELECT COUNT(*) FROM person";
    SQLiteDatabase db = this.getWritableDatabase();
    Cursor cursor = null;
    if (db != null) {
        cursor = db.rawQuery(query, null);
    }

    return cursor;

}

}


