package ru.bmstu.iu9.lab9.sql.utils;
import android.provider.BaseColumns;

public class DbContract {
    private DbContract() {}

    public static class dbEntry implements BaseColumns {
        public static final String TABLE_NAME = "notes";

        public static final String CREATED_AT = "created_at";
        public static final String UPDATED_AT = "updated_at";
        public static final String CONTENT = "content";
    }

}
