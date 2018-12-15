package labs.android.iu9.bmstu.ru.lab7;

import android.provider.BaseColumns;

public class DbContract {
    private DbContract() {}

    public static class dbEntry implements BaseColumns {
        public static final String TABLE_NAME = "exchanges";

        public static final String CURRENCY = "currency";
        public static final String TO_USD = "to_usd";
        public static final String TO_EUR = "to_eur";
        public static final String TO_RUB = "to_rub";
    }
}
