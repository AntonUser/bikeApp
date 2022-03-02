package com.example.bikeapp.adapter;

import java.util.Objects;

public class ListItem {
    private String name;
    private String address;

    public ListItem(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListItem listItem = (ListItem) o;
        return Objects.equals(name, listItem.name) &&
                Objects.equals(address, listItem.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, address);
    }
}
