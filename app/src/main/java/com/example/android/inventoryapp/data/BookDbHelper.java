package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventoryapp.data.BookContract.BookEntry;

public class BookDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "books.db";

    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String COMMA_SEP = ",";
        String SQL_CREATE_BOOKS_TABLE = "CREATE TABLE " + BookEntry.TABLE_NAME + " (" +
                BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
                BookEntry.COLUMN_BOOK_NAME + " TEXT NOT NULL" + COMMA_SEP +
                BookEntry.COLUMN_BOOK_PRICE + " INTEGER NOT NULL DEFAULT 0" + COMMA_SEP +
                BookEntry.COLUMN_BOOK_QUANTITY + " INTEGER NOT NULL DEFAULT 0" + COMMA_SEP +
                BookEntry.COLUMN_BOOK_SUPPLIER_NAME + " TEXT" + COMMA_SEP +
                BookEntry.COLUMN_BOOK_SUPPLIER_PHONE + " TEXT" +
                ")";

        sqLiteDatabase.execSQL(SQL_CREATE_BOOKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String SQL_DELETE_BOOKS_TABLE = "DELETE TABLE IF EXISTS " + BookEntry.TABLE_NAME;

        sqLiteDatabase.execSQL(SQL_DELETE_BOOKS_TABLE);
        onCreate(sqLiteDatabase);
    }
}
