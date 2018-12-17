package ru.bmstu.iu9.lab9;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

import ru.bmstu.iu9.lab9.notes.entity.Note;

import static ru.bmstu.iu9.lab9.EditNote.EXTRA_NOTE;
import static ru.bmstu.iu9.lab9.EditNote.EXTRA_REQ_CODE;

public class EditNoteActivity extends AppCompatActivity {
    private static final String TAG = "EditNoteActivity";

    private MultiAutoCompleteTextView editNoteView;
    private int resCode;
    private Note editedNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        editNoteView = findViewById(R.id.editNoteView);
        TextView noteLabelView = findViewById(R.id.noteLabel);
        Bundle data = getIntent().getExtras();

        if(data == null || !data.containsKey(EXTRA_REQ_CODE)) {
            finishActivityWithFail();
            return;
        }

        int reqCode = data.getInt(EXTRA_REQ_CODE);
        Log.i(TAG, "onCreate: request code: " + reqCode);
        editedNote = null;

        if(reqCode == EditNote.ReqCode.UPDATE.code() && !data.containsKey(EXTRA_NOTE)){
            finishActivityWithFail();
            return;
        }

        if(editedNote == null) {
            Note extraNote = (Note) data.getSerializable(EXTRA_NOTE);
            editedNote = extraNote == null ? createNote() : extraNote;
        }

        Log.i(TAG, "onCreate: edited note: " + editedNote);

        noteLabelView.setText(editedNote.getLastModifiedAsString());
        editNoteView.setVisibility(View.VISIBLE);
        editNoteView.setText(editedNote.getContent());

        resCode = EditNote.ResCode.NOT_MODIFIED.code();

        editNoteView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                resCode = (reqCode == EditNote.ReqCode.CREATE.code()) ? EditNote.ResCode.CREATED.code() : EditNote.ResCode.UPDATED.code();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.note_edit_menu, menu);

//        MenuItem itemSave = findViewById(R.id.menuItemSave);
//        if(resCode == EditNote.ResCode.UPDATED.code()) {
//            itemSave.setVisible(true);
//            itemSave.setEnabled(true);
//        } else {
//            editNoteView.addTextChangedListener(new TextWatcher() {
//                @Override
//                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
//
//                @Override
//                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                }
//
//                @Override
//                public void afterTextChanged(Editable editable) {
//                    MenuItem itemSaveLocal = findViewById(R.id.menuItemSave);
//                    itemSaveLocal.setVisible(true);
//                    itemSaveLocal.setEnabled(true);
//                }
//            });
//        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if(itemId == R.id.menuItemSave) {
            finishActivityWithSuccess();
        }

        return super.onOptionsItemSelected(item);
    }

    private void finishActivityWithSuccess() {
        Log.i(TAG, "finishActivityWithSuccess: res code: " + resCode);
        if(resCode != EditNote.ResCode.FAILED.code() && resCode != EditNote.ResCode.NOT_MODIFIED.code()) {
            if(editedNote == null) {
                editedNote = createNote();
            }
            editedNote.setContent(editNoteView.getText().toString());
            editedNote.setLastModified(new Date());

            Log.i(TAG, "finishActivityWithSuccess: updated - ok");
            Log.i(TAG, "finishActivityWithSuccess: note: " + editedNote);

            Intent curIntent = getIntent();
            curIntent.putExtra(EXTRA_NOTE, editedNote);

            setResult(resCode, curIntent);
        }
        finish();
    }

    private void finishActivityWithFail() {
        String errMsg = getString(R.string.err_note_edit_failed);
        Log.e(TAG, errMsg);
        Toast.makeText(getApplicationContext(), errMsg, Toast.LENGTH_LONG).show();
        setResult(EditNote.ResCode.FAILED.code());
        finish();
    }

    private static Note createNote() {
        Note note = new Note();
        note.setCreatedAt(new Date());
        note.setContent("");
        return note;
    }
}
