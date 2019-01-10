package android.iu9.bmstu.ru.rkapp;

public interface Const {


    final class Key {
        public static final String currencyData = "currencyData";
        public static final String apiKey = "apiKey";
        public static final String limit = "limit";
        public static final String fsym = "fsym";
        public static final String tsym = "tsym";
        public static final String tsyms = "tsyms";
        public static final String ts = "ts";
    }

    final class Pref {
        public static final String currency = "selected_currency";
        public static final String noOfDays = "selected_no_of_days";
    }

    final class Loader {
        public static final int currencyLoaderId = 1;
    }

    final class ActivityReq {
        public static final int settingsChangeReq = 1;
    }

    final class Json {
        public static final String response = "Response";
        public static final String data = "Data";
        public static final String message = "Message";
        public static final String open = "open";
        public static final String close = "close";
        public static final String low = "low";
        public static final String high = "high";
        public static final String time = "time";
    }

    final class Currency {
        public static final String eur = "EUR";
        public static final String rub = "RUB";
        public static final String usd = "USD";
    }

    final class Svc {
        public static final String detailedCurrency = "https://min-api.cryptocompare.com/data/pricehistorical";
        public static final String history = "https://min-api.cryptocompare.com/data/histoday";
    }

}
