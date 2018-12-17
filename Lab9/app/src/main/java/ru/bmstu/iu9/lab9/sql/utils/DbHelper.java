package ru.bmstu.iu9.lab9.sql.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {
    private static final String TAG = "DbHelper";

    private static final String DB_NAME = "notes.sql";
    private static final int DB_VERSION = 1;

    public DbHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "onCreate: ");

        String createTableQuery = String.format(
                "CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s BIGINT NOT NULL, %s BIGINT NOT NULL, %s TEXT NOT NULL);",
                DbContract.dbEntry.TABLE_NAME,
                DbContract.dbEntry._ID,
                DbContract.dbEntry.CREATED_AT,
                DbContract.dbEntry.UPDATED_AT,
                DbContract.dbEntry.CONTENT
        );

        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "onUpgrade: ");

        String dropTableQuery = String.format(
                "DROP TABLE %s;",
                DbContract.dbEntry.TABLE_NAME
        );

        db.execSQL(dropTableQuery);
        onCreate(db);
    }

}
