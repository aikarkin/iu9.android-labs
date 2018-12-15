package iu9.bmstu.ru.lab7;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "notes.sql";
    public static final int DB_VERSION = 1;

    public DbHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
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
        String dropTableQuery = String.format(
                "DROP TABLE %s;",
                DbContract.dbEntry.TABLE_NAME
        );

        db.execSQL(dropTableQuery);
        onCreate(db);
    }

}
