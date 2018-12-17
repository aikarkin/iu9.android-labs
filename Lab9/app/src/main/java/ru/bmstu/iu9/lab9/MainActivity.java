package ru.bmstu.iu9.lab9;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;

import ru.bmstu.iu9.lab9.notes.da.NotesDaService;
import ru.bmstu.iu9.lab9.notes.da.NotesDaServiceImpl;
import ru.bmstu.iu9.lab9.notes.entity.Note;
import ru.bmstu.iu9.lab9.notes.rview.NotesViewAdapter;
import ru.bmstu.iu9.lab9.notes.rview.RecyclerItemClickListener;

import static ru.bmstu.iu9.lab9.EditNote.EXTRA_NOTE;
import static ru.bmstu.iu9.lab9.EditNote.EXTRA_REQ_CODE;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final String[] PERMS = new String[] {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private static final int REQ_PERMS = 6;

    private NotesDaService notesSvc;
    private NotesViewAdapter notesViewAdapter;

    private static int curNotePos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notesSvc = new NotesDaServiceImpl(this);

        notesViewAdapter = new NotesViewAdapter(notesSvc);
        boolean hasPerms = requestPermissionIfRequired();
        if(hasPerms) {
            initRecyclerView();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent childIntent) {
        Log.i(TAG, "onActivityResult: requestCode: " + requestCode);
        Log.i(TAG, "onActivityResult: resultCode: " + resultCode);

        if(resultCode != EditNote.ResCode.FAILED.code()) {
            Bundle data = childIntent.getExtras();

            if (data != null && data.containsKey(EXTRA_NOTE)) {
                Note note = (Note) data.getSerializable(EXTRA_NOTE);

                Log.i(TAG, "onActivityResult: got note: " + note);
                if (requestCode == EditNote.ReqCode.UPDATE.code() && resultCode == EditNote.ResCode.UPDATED.code()) {
                    Log.i(TAG, "onActivityResult: upd note in content provider");
                    notesSvc.update(note);

                    if (curNotePos >= 0) {
                        Log.i(TAG, "onActivityResult: upd note in recycler view");
                        notesViewAdapter.updateNote(note, curNotePos);
                    } else {
                        Log.e(TAG, "onActivityResult: something goes wrong - no previous note position saved before edit");
                    }
                } else if (requestCode == EditNote.ReqCode.CREATE.code() && resultCode == EditNote.ResCode.CREATED.code()) {
                    notesSvc.create(note);
                    notesViewAdapter.addNote(note);
                }
            } else {
                Log.e(TAG, "onActivityResult: something goes wrong - no note provided from EditNoteActivity");
            }
        } else {
            Log.e(TAG, "onActivityResult: failed to process note");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQ_PERMS) {
            if(grantResults.length > 0) {
                for (int grantRes : grantResults) {
                    if (grantRes == PackageManager.PERMISSION_GRANTED) {
                        Log.i(TAG, "onRequestPermissionsResult: Permission granted");
                    } else {
                        Log.w(TAG, "onRequestPermissionsResult: Permission was not granted: " + grantRes);
                    }
                }
            }

            initRecyclerView();
        }
    }

    private boolean requestPermissionIfRequired() {
        for(String perm : PERMS) {
            if(checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(PERMS, REQ_PERMS);
                return false;
            }
        }

        return true;
    }

    private void initRecyclerView() {
        RecyclerView rView = findViewById(R.id.recycler_view);
        rView.setAdapter(notesViewAdapter);
        rView.setItemAnimator(new DefaultItemAnimator());
        rView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton addNoteBtn = findViewById(R.id.addNoteButton);
        addNoteBtn.setOnClickListener(view -> startEditNoteActivity(null));

        rView.addOnItemTouchListener(new RecyclerItemClickListener(this, rView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                showActionsDialog(position);
            }

            @Override
            public void onLongItemClick(View view, int position) {
                showActionsDialog(position);
            }
        }));

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                showReallyWantToDeleteDialog(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(rView);
    }

    private void showActionsDialog(final int pos) {
        Log.i(TAG, "showActionsDialog: ");
        CharSequence[] options = new CharSequence[] {"Edit", "Delete"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_title_action);
        builder.setItems(options, (dialog, i) -> {
            final Note curNote = notesViewAdapter.at(pos);
            if(i == 0) { // "Edit"
                // save current note position to static variable - required to update note after edition in RV
                curNotePos = pos;
                startEditNoteActivity(curNote);
            } else if(i == 1) { // "Delete"
                showReallyWantToDeleteDialog(pos);
            }
        });
        builder.create();
        builder.show();
    }

    private void showReallyWantToDeleteDialog(final int pos) {
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(R.string.dialog_title_rm)
                .setPositiveButton("Yes", (dialog, i) -> {
                    Note curNote = notesViewAdapter.at(pos);
                    notesViewAdapter.removeNote(pos);
                    notesSvc.delete(curNote);
                })
                .setNegativeButton("No", (dialog, i) -> {
                    notesViewAdapter.notifyItemRemoved(pos + 1);
                    notesViewAdapter.notifyItemRangeChanged(pos, notesViewAdapter.getItemCount());
                    dialog.cancel();
                })
                .create()
                .show();
    }

    private void startEditNoteActivity(Note note) {
        int reqCode = (note == null ? EditNote.ReqCode.CREATE : EditNote.ReqCode.UPDATE).code();

        Intent noteEditIntent = new Intent(MainActivity.this, EditNoteActivity.class);
        noteEditIntent.putExtra(EXTRA_NOTE, note);
        noteEditIntent.putExtra(EXTRA_REQ_CODE, reqCode);

        startActivityForResult(noteEditIntent, reqCode);
    }

}
