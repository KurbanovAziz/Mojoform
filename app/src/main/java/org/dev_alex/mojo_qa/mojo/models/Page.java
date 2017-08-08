package org.dev_alex.mojo_qa.mojo.models;

public class Page {
    public String name;
    public String id;

    public Page(String name, String id) {
        this.name = name;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Page page = (Page) o;

        if (name != null ? !name.equals(page.name) : page.name != null) return false;
        return id != null ? id.equals(page.id) : page.id == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
