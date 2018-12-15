package android.iu9.bmstu.ru.rkapp;

import android.content.Context;
import android.iu9.bmstu.ru.rkapp.entity.CurrencyEntity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CurrencyListAdapter extends RecyclerView.Adapter<CurrencyListAdapter.ViewHolder> {
    private List<CurrencyEntity> currencyList;

    CurrencyListAdapter() {}

    public void setData(List<CurrencyEntity> data) {
        this.currencyList = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context ctx = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view = inflater.inflate(R.layout.currency_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(currencyList != null) {
            CurrencyEntity currency = currencyList.get(position);

            DateFormat fmt = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            fmt.setTimeZone(TimeZone.getTimeZone("GMT"));

            holder.dateView.setText(fmt.format(currency.getDate()));
            holder.openView.setText(String.valueOf(currency.getOpen()));
            holder.closeView.setText(String.valueOf(currency.getClose()));
        }
    }

    @Override
    public int getItemCount() {
        return currencyList == null ? 0 : currencyList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateView;
        TextView openView;
        TextView closeView;

        ViewHolder(View itemView) {
            super(itemView);

            dateView = itemView.findViewById(R.id.dateValue);
            openView = itemView.findViewById(R.id.openValue);
            closeView = itemView.findViewById(R.id.closeValue);
        }
    }
}
