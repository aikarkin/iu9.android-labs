package iu9.bmstu.ru.lab8;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MainActivity extends ListActivity {
    private static final String TAG = "MainActivity";
    private static final String[] PERMS = new String[] {
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private static File curDir = Environment.getExternalStorageDirectory();

    private FilesAdapter filesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean isPermsRequired = false;

        for(String perm : PERMS) {
            if(checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(PERMS, ActivityRequests.REQUIRE_PERMISSION);
                isPermsRequired = true;
                break;
            }
        }
        if(!isPermsRequired) {
            forceReadDirectory(Environment.getExternalStorageDirectory());
        }

        FloatingActionButton createBtn = findViewById(R.id.createButton);
        createBtn.setOnClickListener(btn -> showCreateDialog());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == ActivityRequests.REQUIRE_PERMISSION) {

            if(grantResults.length > 0) {
                for (int grantRes : grantResults) {
                    if (grantRes == PackageManager.PERMISSION_GRANTED) {
                        Log.i(TAG, "onRequestPermissionsResult: Permission granted");
                    } else {
                        Log.w(TAG, "onRequestPermissionsResult: Permission was not granted: " + grantRes);
                    }
                }
            }

            if (isExternalStorageReadable()) {
                forceReadDirectory(curDir);
            } else {
                Log.w(TAG, "onRequestPermissionsResult: External storage is not readable");
            }
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        FilesAdapter.FileItem item = filesAdapter.getItem(position);
        if(item != null && item.getFileName().equals("..")) {
            Log.i(TAG, "onListItemClick: open parent directory");

            curDir = curDir.getParentFile();
            forceReadDirectory(curDir);
        } else {
            Log.i(TAG, "onListItemClick: open matched item");
            if(item == null) {
                Toast.makeText(this, "Failed to open item.", Toast.LENGTH_SHORT).show();
                return;
            }

            File matchedFile = item.getFile();

            if(matchedFile.isDirectory()) {
                Log.i(TAG, "onListItemClick: matched dir");
                curDir = matchedFile;
                forceReadDirectory(curDir);
                filesAdapter.notifyDataSetChanged();
            } else {
                Log.i(TAG, "onListItemClick: matched file");
                openFile(matchedFile);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ActivityRequests.EDIT_TEXT_FILE) {
            if(resultCode == EditFileRespCodes.FILE_SAVED) {
                Bundle intentBundle = data.getExtras();
                if(intentBundle != null && intentBundle.containsKey(EditFileExtras.FILE_NAME)) {
                    String filename = intentBundle.getString(EditFileExtras.FILE_NAME);
                    if(filename != null) {
                        filesAdapter.add(new FilesAdapter.FileItem(new File(filename)));
                        filesAdapter.notifyDataSetChanged();
                    }
                } else {
                    Log.w(TAG, "onActivityResult: something is wrong - no file provided from text editor");
                }
            } else {
                Log.i(TAG, "onActivityResult: file not changed");
                if(resultCode == EditFileRespCodes.FILE_SAVE_FAIL) {
                    Log.w(TAG, "onActivityResult: failed to save file");
                }
            }
        }
    }

    private void showCreateDialog() {
        CharSequence[] options = new CharSequence[] {"Text file", "Folder"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(options, (dialog, i) -> {
            if(i == 0) {
                showEnterFileNameDialog("Enter file name", (filename) ->  this.openFileInTextEditor(filename, false));
            }
            else if(i == 1) {
                showEnterFileNameDialog("Enter folder name", this::createNewFolder);
            }
        });

        builder.create().show();
    }

    private void showEnterFileNameDialog(String title, Consumer<String> onCreate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);

        builder.setView(editText);

        builder.setPositiveButton(
                "Create",
                (dialog, i) ->
                        onCreate.accept(curDir.getAbsolutePath() + "/" + editText.getText().toString())
        );

        builder.setNegativeButton("Cancel", (dialog, i) -> dialog.cancel());

        builder.create().show();
    }

    private void createNewFolder(String filePath) {
        Log.i(TAG, "createNewFolder: create folder: " + filePath);
        File dir = new File(filePath);

        if(!dir.exists()) {
            boolean isDirCreated = dir.mkdirs();
            if(isDirCreated) {
                filesAdapter.add(new FilesAdapter.FileItem(dir));
                filesAdapter.notifyDataSetChanged();
            } else {
                Log.e(TAG, "createNewFolder: failed to create folder: " + filePath);
                Toast.makeText(getApplicationContext(), "Failed to create folder: " + filePath, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void openFile(File file) {
        if(!file.canRead()) {
            Log.w(TAG, "openFile: unable to read file");
            Toast.makeText(getApplicationContext(), "Unable to read file", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", file);
        String mime = getContentResolver().getType(uri);

        // if it is text file, then open in internal editor
        if(mime != null && mime.startsWith("text")) {
            openFileInTextEditor(file.getAbsolutePath(), !file.canWrite());
        } else { // otherwise, open file with other installed app
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mime);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(intent);
        }
    }

    private void openFileInTextEditor(String filepath, boolean readonly) {
        Intent intent = new Intent(MainActivity.this, EditFileActivity.class);
        intent.putExtra(EditFileExtras.FILE_NAME, filepath);
        intent.putExtra(EditFileExtras.READ_ONLY, readonly);
        startActivityForResult(intent, 0);
    }

    private void forceReadDirectory(final File dir) {
        if(!dir.exists()) {
            Log.w(TAG, "forceReadDirectory: file not exists");
            return;
        }

        if(!dir.isDirectory()) {
            Log.w(TAG, "forceReadDirectory: passed file is not directory");
            return;
        }

        Log.i(TAG, "forceReadDirectory: open dir: " + dir.getAbsolutePath());

        File[] filesArr = dir.listFiles();
        if(filesArr == null) {
            Log.i(TAG, "forceReadDirectory: failed to read files from dir: " + dir.getAbsolutePath());
            return;
        }
        List<FilesAdapter.FileItem> files = Arrays.stream(filesArr).map(FilesAdapter.FileItem::new)
                .collect(Collectors.toList());

        if(dir.getParentFile() != null && !dir.equals(Environment.getExternalStorageDirectory())) {
            FilesAdapter.FileItem parentFileItem = new FilesAdapter.FileItem(dir.getParentFile());
            parentFileItem.setFileName("..");
            files.add(0, parentFileItem);
        }

        if(filesAdapter == null) {
            filesAdapter = new FilesAdapter(this, files);
            setListAdapter(filesAdapter);

            // hide loading message
            TextView tvLoader = findViewById(R.id.tvLoader);
            tvLoader.setText("");
            tvLoader.setVisibility(TextView.INVISIBLE);

            // set action for SwipeRefreshLayout
            final SwipeRefreshLayout swipeRefresh = findViewById(R.id.swipeRefresh);
            swipeRefresh.setOnRefreshListener(() -> {
                swipeRefresh.setRefreshing(true);
                forceReadDirectory(curDir);
                swipeRefresh.setRefreshing(false);
            });
        } else {
            filesAdapter.clear();
            filesAdapter.addAll(files);
        }
    }

    private static boolean isExternalStorageReadable() {
        String storageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(storageState) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(storageState);
    }

}
