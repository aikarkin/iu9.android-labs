package android.iu9.bmstu.ru.rkapp.entity;

import android.content.Context;
import android.iu9.bmstu.ru.rkapp.Const;
import android.iu9.bmstu.ru.rkapp.exception.BadApiRequestException;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CurrencyEntity implements Serializable {
    private static final String TAG = "CurrencyEntity";

    private Date date;
    private double low;
    private double high;
    private double open;
    private double close;

    public CurrencyEntity() { }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getOpen() {
        return open;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public static List<CurrencyEntity> parse(Context ctx, String content) throws JSONException {
        List<CurrencyEntity> currencyList = new ArrayList<>();
        JSONObject json = new JSONObject(content);
        String response = json.getString(Const.Json.response);

        if(response.equals("Success")) {

            JSONArray data = json.getJSONArray(Const.Json.data);
            for (int i = 0; i < data.length(); i++) {
                CurrencyEntity currency = new CurrencyEntity();

                JSONObject currJson = data.getJSONObject(i);
                currency.setOpen(currJson.getDouble(Const.Json.open));
                currency.setClose(currJson.getDouble(Const.Json.close));
                currency.setLow(currJson.getDouble(Const.Json.low));
                currency.setHigh(currJson.getDouble(Const.Json.high));
                Log.i(TAG, "Parse entity - date = " + currJson.getInt(Const.Json.time));
                currency.setDate(new Date((long) currJson.getInt(Const.Json.time) * 1000));

                currencyList.add(currency);
            }

        } else if(response.equals("Error")) {
            String msg = json.getString(Const.Json.message);
            Log.e(TAG, "parse: " + msg);
            throw new BadApiRequestException(msg);
        }
        return currencyList;
    }
}
