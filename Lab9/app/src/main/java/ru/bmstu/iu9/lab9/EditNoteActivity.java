package ru.bmstu.iu9.lab9;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.bmstu.iu9.lab9.notes.entity.Note;

import static ru.bmstu.iu9.lab9.EditNote.EXTRA_NOTE;
import static ru.bmstu.iu9.lab9.EditNote.EXTRA_REQ_CODE;

public class EditNoteActivity extends AppCompatActivity {
    private static final String TAG = "EditNoteActivity";
    private static final int REQ_LOAD_IMAGE = 2;

    private MultiAutoCompleteTextView editNoteView;
    private int resCode;
    private Note editedNote;

    private static Note createNote() {
        Note note = new Note();
        note.setCreatedAt(new Date());
        note.setContent("");
        return note;
    }

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
        setRichContent(editedNote.getContent());

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

        FloatingActionButton insertPhotoBtn = findViewById(R.id.insertPhoto);
        insertPhotoBtn.setOnClickListener((btnView) -> runPickPhotoIntent());
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // insert picked photo
        if(requestCode == REQ_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            ContentResolver resolver = getContentResolver();

            if(resolver != null && selectedImage != null) {
                Cursor cursor = getContentResolver().query(
                        selectedImage,
                        filePathColumn,
                        null,
                        null,
                        null
                );
                if(cursor != null) {
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();

                    insertImageIntoEditView(picturePath, editNoteView.getSelectionStart(), editNoteView.getSelectionEnd());
                } else {
                    Log.e(TAG, "onActivityResult: failed to get image from gallery: " + selectedImage.toString());
                }

            } else {
                Log.e(TAG, "onActivityResult: failed to get image content");
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if(itemId == R.id.menuItemSave) {
            finishActivityWithSuccess();
        }

        return super.onOptionsItemSelected(item);
    }

    private void setRichContent(String content) {
        editNoteView.setText(content);

        Pattern refPtr = Pattern.compile("\\[#ref=(.*?)]");
        Matcher refMatcher = refPtr.matcher(content);

        while(refMatcher.find()) {
            String ref = refMatcher.group(1);
            int start = refMatcher.start(), end = refMatcher.end();
            Log.i(TAG, "setRichContent: replace ref with image: " + ref);
            insertImageIntoEditView(ref, start, end);
        }
    }

    private void insertImageIntoEditView(String picPath, int start, int end) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(picPath, options);
        Display display = getWindowManager().getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);
        int bmpWidth = options.outWidth;
        int bmpHeight = options.outHeight;
        Log.i(TAG, "insertImageIntoEditView: env size: " + String.format("%dx%d", screenSize.x, screenSize.y));

        double scale = 0.85;
        double ratio = bmpWidth < bmpHeight ? bmpHeight / bmpWidth : bmpWidth / bmpHeight;
        Bitmap scaledBmp = Bitmap.createScaledBitmap(bitmap, (int)(scale * screenSize.x), (int)(scale * ratio * screenSize.y), false);
        ImageSpan imgSpan = new ImageSpan(getApplicationContext(), scaledBmp);
        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder();
        spannableBuilder.append(editNoteView.getText());

        String imgId = String.format("[#ref=%s]", picPath);

        spannableBuilder.replace(start, end, imgId);
        spannableBuilder.setSpan(imgSpan, start, start + imgId.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        editNoteView.setText(spannableBuilder);
    }

    private void runPickPhotoIntent() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhotoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(pickPhotoIntent, REQ_LOAD_IMAGE);
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

}