package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventoryapp.data.ItemContract.ItemEntry;

/**
 * Created by Timo on 23.07.2017.
 */

public class ItemDbHelper extends SQLiteOpenHelper {

        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "items.db";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + ItemEntry.TABLE_NAME + " ("
                        + ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + ItemEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, "
                        + ItemEntry.COLUMN_ITEM_PRICE + " TEXT NOT NULL, "
                        + ItemEntry.COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL, "
                        + ItemEntry.COLUMN_ITEM_SUPPLIER + " TEXT NOT NULL, "
                        + ItemEntry.COLUMN_ITEM_PICTURE + " TEXT NOT NULL);";

        public static final String SQL_DELETE_ENTRIES = "";

        public ItemDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

    }