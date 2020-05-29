package com.example.betrush;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Team extends RealmObject {
    @PrimaryKey
    public String name;
    public String getName() {
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
}
