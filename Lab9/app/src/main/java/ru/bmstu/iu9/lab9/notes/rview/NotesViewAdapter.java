package ru.bmstu.iu9.lab9.notes.rview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ru.bmstu.iu9.lab9.R;
import ru.bmstu.iu9.lab9.notes.da.NotesDaService;
import ru.bmstu.iu9.lab9.notes.entity.Note;

public class NotesViewAdapter extends RecyclerView.Adapter<NotesViewAdapter.NotesViewHolder> {
    private static final String TAG = "NotesViewAdapter";

    private NotesDaService daService;
    private List<Note> notes;

    public NotesViewAdapter(NotesDaService svc) {
        this.daService = svc;
        this.notes = daService.getAll();
        Log.i(TAG, "NotesViewAdapter: notes - " + notes);
    }

    public void addNote(Note note) {
        this.notes.add(0, note);
        this.notifyItemInserted(0);
    }

    public void updateNote(Note note, int pos) {
        this.notes.set(pos, note);
        this.notifyItemChanged(pos);
        Log.i(TAG, "updateNote: upd note - ok");
    }

    public void removeNote(int pos) {
        this.notes.remove(pos);
        this.notifyItemRemoved(pos);
    }

    public Note at(int position) {
        return this.notes.get(position);
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context ctx = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view = inflater.inflate(R.layout.layout_item, parent, false);

        return new NotesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.lastModified.setText(note.getLastModifiedAsString());
        holder.content.setText(note.getContent());
        holder.itemView.setTag(note.getId());
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public class NotesViewHolder extends RecyclerView.ViewHolder {
        TextView lastModified;
        TextView content;

        public NotesViewHolder(@NonNull View itemView) {
            super(itemView);

            lastModified = itemView.findViewById(R.id.lastModified);
            content = itemView.findViewById(R.id.content);
        }
    }

}

