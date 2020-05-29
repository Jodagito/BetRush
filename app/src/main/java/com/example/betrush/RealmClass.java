package com.example.betrush;

import android.app.Application;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmClass extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        Realm.init(this);
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .name("betrushdb")
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(configuration);
    }
}
