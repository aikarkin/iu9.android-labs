package ru.bmstu.iu9.lab9.notes.da;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.bmstu.iu9.lab9.notes.entity.Note;
import ru.bmstu.iu9.lab9.notes.provider.NotesProvider;
import ru.bmstu.iu9.lab9.sql.utils.DbContract;

public class NotesDaServiceImpl implements NotesDaService {
    private static final Uri.Builder URI_BUILDER = new Uri.Builder()
            .scheme("content")
            .authority(NotesProvider.AUTHORITY)
            .appendPath("notes");
    private static final Uri ALL_NOTES_URI = URI_BUILDER.build();

    private ContentResolver resolver;

    public NotesDaServiceImpl(Context ctx) {
        resolver = ctx.getContentResolver();
    }

    @Override
    public List<Note> getAll() {
        Cursor cursor = resolver.query(ALL_NOTES_URI, null, null, null, null);
        List<Note> notes = new ArrayList<>();

        if(cursor == null) {
            return notes;
        }

        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            notes.add(noteFromCursor(cursor));
        }

        cursor.close();
        return notes;
    }

    @Override
    public Note findById(long id) {
        Cursor cursor = resolver.query(getNotesUri(id), null, null, null, null);

        return cursor != null && cursor.moveToFirst() ? noteFromCursor(cursor) : null;
    }

    @Override
    public boolean delete(Note note) {
        return resolver.delete(getNotesUri(note.getId()), null, null) == 0;
    }

    @Override
    public boolean update(Note note) {
        ContentValues values = convertNoteToContentValues(note);
        return resolver.update(getNotesUri(note.getId()), values, null, null) > 0;
    }

    @Override
    public boolean create(Note note) {
        ContentValues values = convertNoteToContentValues(note);
        return resolver.insert(getNotesUri(note.getId()), values) != null;
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

    private static Uri getNotesUri(long id) {
        return URI_BUILDER.appendPath(String.valueOf(id)).build();
    }
}
