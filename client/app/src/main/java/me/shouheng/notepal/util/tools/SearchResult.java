package me.shouheng.notepal.util.tools;

import java.util.List;

import me.urakalee.next2.model.Note;

/**
 * Created by shouh on 2018/3/18.*/
public class SearchResult {

    private List<Note> notes;

    public SearchResult(List<Note> notes) {
        this.notes = notes;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "notes=" + notes +
                '}';
    }
}
