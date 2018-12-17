package ru.bmstu.iu9.lab9.notes.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import ru.bmstu.iu9.lab9.sql.utils.DbContract;
import ru.bmstu.iu9.lab9.sql.utils.DbHelper;

public class NotesProvider extends ContentProvider {
    private static final String TAG = "NotesProvider";

    public static final String AUTHORITY = "ru.bmstu.iu9.lab9.notesprovider";

    private static final int SINGLE_ROW = 1;
    private static final int MULTIPLE_ROWS = 2;
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH) {{
        addURI(AUTHORITY, "notes/#", SINGLE_ROW);
        addURI(AUTHORITY, "notes", MULTIPLE_ROWS);
    }};

    private SQLiteDatabase database;

    @Override
    public boolean onCreate() {
        DbHelper helper = new DbHelper(getContext());
        this.database = helper.getWritableDatabase();

        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "text/plain";
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Log.i(TAG, "query: " + uri.toString());

        switch(URI_MATCHER.match(uri)) {
            case SINGLE_ROW:
                return selection == null
                        ? database.query(
                            DbContract.dbEntry.TABLE_NAME,
                            projection,
                            DbContract.dbEntry._ID + "=?",
                            new String[] {uri.getLastPathSegment()},
                            null,
                            null,
                            sortOrder
                        )
                        : database.query(
                                DbContract.dbEntry.TABLE_NAME,
                                projection,
                                selection,
                                selectionArgs,
                                null,
                                null,
                                sortOrder
                        );
            case MULTIPLE_ROWS:
                return database.query(
                        DbContract.dbEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null ,
                        sortOrder
                );
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        long id = database.insert(DbContract.dbEntry.TABLE_NAME, null, contentValues);
        Uri rowUri = new Uri.Builder()
                .scheme("content")
                .authority(AUTHORITY)
                .appendPath("notes")
                .appendPath(String.valueOf(id))
                .build();

        return id < 0 ? null : rowUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        if(URI_MATCHER.match(uri) != SINGLE_ROW)
            throw new UnsupportedOperationException("You cannot delete all rows - operation is not permitted");

        String id = uri.getLastPathSegment();
        return selection == null
                ? database.delete(DbContract.dbEntry.TABLE_NAME, DbContract.dbEntry._ID + "=?", new String[]{id})
                : database.delete(DbContract.dbEntry.TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        if(URI_MATCHER.match(uri) != SINGLE_ROW)
            throw new UnsupportedOperationException("You cannot update all rows - operation is not permitted");

        return selection == null
                ? database.update(DbContract.dbEntry.TABLE_NAME, contentValues, DbContract.dbEntry._ID  + "=?", new String[]{uri.getLastPathSegment()})
                : database.update(DbContract.dbEntry.TABLE_NAME, contentValues, selection, selectionArgs);
    }



}
