package labs.android.iu9.bmstu.ru.lab7;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class CurrencyExchange implements Serializable {
    private String fsym;
    private HashMap<String, Double> mapTsyms;

    public CurrencyExchange() { }

    public CurrencyExchange(String fsym) {
        this.fsym = fsym;
    }

    public CurrencyExchange(String fsym, HashMap<String, Double> mapTsyms) {
        this.fsym = fsym;
        this.mapTsyms = mapTsyms;
    }

    public String getFsym() {
        return fsym;
    }

    public void setFsym(String fsym) {
        this.fsym = fsym;
    }

    public Map<String, Double> getTsyms() {
        return mapTsyms;
    }

    public void addTsym(String name, double val) {
        this.mapTsyms.put(name, val);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CurrencyExchange that = (CurrencyExchange) o;
        return Objects.equals(fsym, that.fsym) &&
                Objects.equals(mapTsyms, that.mapTsyms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fsym, mapTsyms);
    }
}
