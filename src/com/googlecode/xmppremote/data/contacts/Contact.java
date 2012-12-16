package com.googlecode.xmppremote.data.contacts;

import java.util.ArrayList;

public class Contact implements Comparable<Contact> {
    public ArrayList<Long> ids = new ArrayList<Long>();
    public ArrayList<Long> rawIds = new ArrayList<Long>();
    public String name;

    public int compareTo(Contact another) {
        return name.compareTo(another.name);
    }
}
