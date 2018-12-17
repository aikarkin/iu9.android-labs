package ru.bmstu.iu9.lab9;

public final class EditNote {
    static final String EXTRA_NOTE = "note";
    static final String EXTRA_REQ_CODE = "requestCode";

    private EditNote() { }

    public enum ReqCode {
        CREATE,
        UPDATE;

        int code() {
            return ordinal();
        }
    }

    public enum ResCode {
        FAILED,
        NOT_MODIFIED,
        CREATED,
        UPDATED;

        int code() {
            return ordinal();
        }
    }

}