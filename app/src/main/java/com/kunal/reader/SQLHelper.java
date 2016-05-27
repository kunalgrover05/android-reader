package com.kunal.reader;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class SQLHelper extends SQLiteOpenHelper {

    // Database Name
    private static final String DATABASE_NAME = "reader";

    // Contacts table name
    private static final String TABLE_BOOKS = "books";

    // Books Table Columns names
    private static final String KEY_NAME = "name";
    private static final String KEY_PAGE = "page";

    public SQLHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_BOOKS_TABLE = "CREATE TABLE " + TABLE_BOOKS + "("
                + KEY_NAME + " TEXT PRIMARY KEY," + KEY_PAGE + " INTEGER" + ")";
        db.execSQL(CREATE_BOOKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);

        // Create tables again
        onCreate(db);
    }

    public void addBook(String file, int pg) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, file);
        values.put(KEY_PAGE, pg);

        Cursor cursor = db.query(TABLE_BOOKS, new String[] { KEY_NAME, KEY_PAGE }, KEY_NAME + "=?",
                new String[] { file }, null, null, null, null);
        if (cursor != null) {
            // updating row
            db.update(TABLE_BOOKS, values, KEY_NAME + " = ?", new String[]{file});
            cursor.close();
        } else {
            // Inserting Row
            db.insert(TABLE_BOOKS, null, values);
        }
        db.close();
    }

    public int getBook(String file) {
        SQLiteDatabase db = this.getWritableDatabase();
        int page;

        Cursor cursor = db.query(TABLE_BOOKS, new String[] { KEY_NAME, KEY_PAGE }, KEY_NAME + "=?",
                new String[] { file }, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            page = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PAGE)));
            cursor.close();
        } else {
            // Inserting Row
            page = 0;
            ContentValues values = new ContentValues();
            values.put(KEY_NAME, file);
            values.put(KEY_PAGE, page);

            db.insert(TABLE_BOOKS, null, values);
        }
        db.close();
        return page;
    }

    public List<String> getBooks() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> files = new ArrayList<>();

        Cursor cursor = db.query(TABLE_BOOKS, new String[] { KEY_NAME, KEY_PAGE }, null,
                null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                files.add(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return files;
    }
}
