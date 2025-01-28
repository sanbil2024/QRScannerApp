package com.poslano.qrscannerapp;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ScannedCode.class}, version = 1, exportSchema = false)
public abstract class ScannedCodeDatabase extends RoomDatabase {

    private static volatile ScannedCodeDatabase INSTANCE;

    // Referenca na DAO
    public abstract ScannedCodeDao scannedCodeDao();

    // Singleton metoda za dobivanje instance baze podataka
    public static ScannedCodeDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ScannedCodeDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    ScannedCodeDatabase.class, "scanned_code_database")
                            .fallbackToDestructiveMigration() // Brisanje baze kod promjene verzije (opcionalno)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
