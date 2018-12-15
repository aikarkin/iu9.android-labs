package iu9.bmstu.ru.lab7;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotesDaService {
    private SQLiteDatabase database;

    public NotesDaService(AppCompatActivity context) {
        DbHelper helper = new DbHelper(context);
        this.database = helper.getWritableDatabase();
    }

    public List<Note> getAllNotes() {
        Cursor cursor = database.query(
                DbContract.dbEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                DbContract.dbEntry.UPDATED_AT + " DESC"
        );

        List<Note> notes = new ArrayList<>();

        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            notes.add(noteFromCursor(cursor));
        }

        cursor.close();
        return notes;
    }

    public Note findById(long id) {
        Cursor cursor = database.rawQuery("SELECT * FROM " + DbContract.dbEntry.TABLE_NAME + " WHERE _id = " + id, null);
        return cursor.moveToFirst() ? noteFromCursor(cursor) : null;
    }

    public boolean delete(Note note) {
        return database.delete(DbContract.dbEntry.TABLE_NAME, DbContract.dbEntry._ID + "=" + note.getId(), null) > 0;
    }

    public void create(Note note) {
        ContentValues values = convertNoteToContentValues(note);
        database.insert(DbContract.dbEntry.TABLE_NAME, null, values);
    }

    public boolean update(Note note) {
        ContentValues values = convertNoteToContentValues(note);
        return database.update(DbContract.dbEntry.TABLE_NAME, values, "_id=" + note.getId(), null) > 0;
    }

    private static Note noteFromCursor(Cursor cursor) {
        Date createdAt = new Date(cursor.getLong(cursor.getColumnIndex(DbContract.dbEntry.CREATED_AT)));
        Date lastModified = new Date(cursor.getLong(cursor.getColumnIndex(DbContract.dbEntry.UPDATED_AT)));
        String content = cursor.getString(cursor.getColumnIndex(DbContract.dbEntry.CONTENT));
        long id = cursor.getLong(cursor.getColumnIndex(DbContract.dbEntry._ID));

        return new Note(id, createdAt, lastModified, content);
    }

    private static ContentValues convertNoteToContentValues(Note note) {
        ContentValues values = new ContentValues();

        values.put(DbContract.dbEntry.CREATED_AT, note.getCreatedAt().getTime());
        values.put(DbContract.dbEntry.UPDATED_AT, note.getLastModified().getTime());
        values.put(DbContract.dbEntry.CONTENT, note.getContent());

        return values;
    }

}
