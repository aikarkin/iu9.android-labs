package labs.android.iu9.bmstu.ru.lab7;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


public abstract class BaseLoaderCallbacks<T> implements LoaderManager.LoaderCallbacks<StringBuilder> {
    private Context context;

    public BaseLoaderCallbacks() {}

    public void setContext(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Loader<StringBuilder> onCreateLoader(int i, @Nullable Bundle bundle) {
        if(context == null) {
            throw new IllegalStateException("Context is not set");
        }

        return new TextLoader(context, CurrencyUtils.buildCoinListUrl(context));
    }

    @Override
    public void onLoadFinished(@NonNull Loader<StringBuilder> loader, StringBuilder resp) {
        T data = parse(resp);
        whenParsed(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<StringBuilder> loader) {

    }

    abstract T parse(StringBuilder data);

    abstract void whenParsed(T data);
}
