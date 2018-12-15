package iu9.bmstu.ru.lab8;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;


public class EditFileActivity extends AppCompatActivity {
    private static final String TAG = "EditFileActivity";

    private static File editedFile;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_file);

        editText = findViewById(R.id.editText);

        Intent curIntent = getIntent();

        if(curIntent.hasExtra(EditFileExtras.FILE_NAME)) {
            String filename = curIntent.getStringExtra(EditFileExtras.FILE_NAME);
            editedFile = new File(filename);

            if(editedFile.exists() && editedFile.canRead()) {
                loadFileIntoEditBox(editedFile);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.file_edit_menu, menu);

        enableSaveButton(false);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                enableSaveButton(true);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        Bundle extras = getIntent() == null ? null : getIntent().getExtras();

        if(itemId == R.id.item_save) {
            try {
                saveFile();
                setResult(EditFileRespCodes.FILE_SAVED);
                finish();
            } catch (IOException e) {
                Log.e(TAG, "onOptionsItemSelected: Failed to save file", e);
                Toast.makeText(getApplicationContext(), "Failed to save file", Toast.LENGTH_SHORT).show();
                if(extras != null) {
                    extras.putString(EditFileExtras.FILE_NAME, null);
                }
                setResult(EditFileRespCodes.FILE_SAVE_FAIL);
                finish();
            }
        } else if(itemId == R.id.item_close) {
            if(extras != null) {
                extras.putString(EditFileExtras.FILE_NAME, null);
            }
            setResult(EditFileRespCodes.FILE_CLOSED);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void enableSaveButton(boolean value) {
//        ImageButton saveBtnItem = findViewById(R.id.item_save);
//        if(saveBtnItem != null) {
//            saveBtnItem.setEnabled(value);
//            Log.i(TAG, "enableSaveButton: button enabled prop is changed");
//        } else {
//            Log.w(TAG, "enableSaveButton: save button item is null - nothing happened");
//        }
    }

    private void loadFileIntoEditBox(File file) {
        try {
            Bundle passedData = getIntent().getExtras();
            boolean isReadonlyMod =
                    (!file.canWrite())
                ||
                    (passedData != null && passedData.containsKey(EditFileExtras.READ_ONLY) && passedData.getBoolean(EditFileExtras.READ_ONLY));

            if(isReadonlyMod) {
                editText.setEnabled(false);
                return;
            }
            FileInputStream inputStream = new FileInputStream(file);
            Scanner scanner = new Scanner(inputStream);
            StringBuilder content = new StringBuilder();
            while (scanner.hasNext()) {
                content.append(scanner.nextLine()).append("\n");
            }
            editText.setText(content.toString());
        } catch (FileNotFoundException e) {
            String absolutePath = file.getAbsolutePath();
            Toast.makeText(getApplicationContext(), "Failed to load file: " + absolutePath, Toast.LENGTH_LONG).show();
            Log.e(TAG, "loadFileIntoEditBox: Failed to load file: " + absolutePath, e);
        }

        enableSaveButton(true);
    }

    private void saveFile() throws IOException {
        byte[] content = editText.getText().toString().getBytes();

        if(!editedFile.exists()) {
            boolean isFileCreated = editedFile.createNewFile();
            if(!isFileCreated) {
                Log.e(TAG, "saveFile: unable to save file: " + editedFile);
                throw new FileNotFoundException("No file created");
            }
        }
        FileOutputStream fileOut = new FileOutputStream(editedFile, false);
        fileOut.write(content);
        fileOut.flush();
        fileOut.close();
    }
}
