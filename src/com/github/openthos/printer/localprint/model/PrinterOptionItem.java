package com.github.openthos.printer.localprint.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bboxh on 2016/5/27.
 */
public class PrinterOptionItem {

    private int def = -1;
    private List<String> option = new ArrayList<>();
    private String name;
    private String option_id;
    private int def2;

    public PrinterOptionItem() {
    }

    public List<String> getOption() {
        return option;
    }

    public void add(String item, boolean flag) {
        this.option.add(item);
        if(flag) {
            def = this.option.size() - 1;
        }
    }

    public int getDef() {
        return def;
    }

    public void setDef2(int def2) {
        this.def2 = def2;
    }

    public int getDef2() {
        return def2;
    }

    public String getDefValue() {
        return option.get(def);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOption_id() {
        return option_id;
    }

    public void setOption_id(String option_id) {
        this.option_id = option_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PrinterOptionItem that = (PrinterOptionItem) o;

        if ((def != that.def) || (def2 != that.def2)) {
            return false;
        }
        if (option != null ? !option.equals(that.option) : that.option != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        return option_id != null ? option_id.equals(that.option_id) : that.option_id == null;

    }

    @Override
    public int hashCode() {
        int result = def;
        result = 31 * result + (option != null ? option.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (option_id != null ? option_id.hashCode() : 0);
        result = 31 * result + def2;
        return result;
    }

    @Override
    public String toString() {
        return "PrinterOptionItem{" +
                "def=" + def +
                ", option=" + option +
                ", name='" + name + '\'' +
                ", option_id='" + option_id + '\'' +
                ", def2=" + def2 +
                '}';
    }
}
