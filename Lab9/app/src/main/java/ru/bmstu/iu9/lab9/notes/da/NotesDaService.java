package ru.bmstu.iu9.lab9.notes.da;

import java.util.List;

import ru.bmstu.iu9.lab9.notes.entity.Note;

public interface NotesDaService {
    List<Note> getAll();
    Note findById(long id);
    boolean delete(Note note);
    boolean update(Note note);
    boolean create(Note note);
}
