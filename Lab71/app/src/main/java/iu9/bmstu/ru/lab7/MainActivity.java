package iu9.bmstu.ru.lab7;

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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private NotesViewAdapter notesViewAdapter;
    private NotesDaService notesSvc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notesSvc = new NotesDaService(this);
        notesViewAdapter = new NotesViewAdapter(notesSvc);
        RecyclerView rView = findViewById(R.id.recycler_view);
        rView.setAdapter(notesViewAdapter);
        rView.setItemAnimator(new DefaultItemAnimator());
        rView.setLayoutManager(new LinearLayoutManager(this));

        Log.i(TAG, "onCreate: view size: " + notesViewAdapter.getItemCount());

        FloatingActionButton addNoteBtn = findViewById(R.id.addNoteButton);
        addNoteBtn.setOnClickListener(view ->
                showNoteDialog(new Note(), false, (note) -> {
                        notesSvc.create(note);
                        notesViewAdapter.addItem(note);
                })
        );

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
        builder.setTitle("Choose action");
        builder.setItems(options, (dialog, i) -> {
            final Note curNote = notesViewAdapter.noteAt(pos);
            if(i == 0) { // "Edit"
                showNoteDialog(curNote, true, (note) -> {
                    curNote.setCreatedAt(note.getCreatedAt());
                    curNote.setLastModified(note.getLastModified());
                    curNote.setContent(note.getContent());

                    notesSvc.update(note);
                    notesViewAdapter.notifyItemChanged(pos);
                });
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
                .setTitle("Do you realy want to remove note?")
                .setPositiveButton("Yes", (dialog, i) -> {
                    Note curNote = notesViewAdapter.noteAt(pos);
                    notesViewAdapter.removeItem(pos);
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

    private void showNoteDialog(Note note, boolean shouldUpdate, Consumer<Note> onOk) {
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View view = inflater.inflate(R.layout.note_edit_dialog, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        dialogBuilder.setView(view);

        TextView titleView = view.findViewById(R.id.dialog_title);
        EditText contentView = view.findViewById(R.id.dialog_content);

        titleView.setText(shouldUpdate ? "Edit note" : "Create note");
        if(shouldUpdate && note.getContent() != null) {
            contentView.setText(note.getContent());
        }

        dialogBuilder
                .setCancelable(true)
                .setPositiveButton("Save", (dialogInterface, i) -> {
                    if(onOk != null) {
                        Date curDate = Calendar.getInstance().getTime();

                        note.setContent(contentView.getText().toString());
                        note.setLastModified(curDate);

                        if(!shouldUpdate) {
                            note.setCreatedAt(curDate);
                        }

                        onOk.accept(note);
                    }
                })
                .setNegativeButton("Cancel", ((dialogInterface, i) -> dialogInterface.cancel()));

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

}
