package labs.android.iu9.bmstu.ru.lab7;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "crypto-compare.sql";
    public static final int DB_VERSION = 1;

    public DbHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = String.format(
                "CREATE TABLE %s (%s, %s, %s, %s, %s);",
                DbContract.dbEntry.TABLE_NAME,
                DbContract.dbEntry._ID,
                DbContract.dbEntry.CURRENCY,
                DbContract.dbEntry.TO_EUR,
                DbContract.dbEntry.TO_RUB,
                DbContract.dbEntry.TO_USD
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
