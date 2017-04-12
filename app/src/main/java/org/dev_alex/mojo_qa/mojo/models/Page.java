package org.dev_alex.mojo_qa.mojo.models;

import android.widget.LinearLayout;

public class Page {
    public String name;
    public String id;
    public LinearLayout layout;

    public Page(String name, String id, LinearLayout layout) {
        this.name = name;
        this.id = id;
        this.layout = layout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Page page = (Page) o;

        if (name != null ? !name.equals(page.name) : page.name != null) return false;
        if (id != null ? !id.equals(page.id) : page.id != null) return false;
        return layout != null ? layout.equals(page.layout) : page.layout == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (layout != null ? layout.hashCode() : 0);
        return result;
    }
}
