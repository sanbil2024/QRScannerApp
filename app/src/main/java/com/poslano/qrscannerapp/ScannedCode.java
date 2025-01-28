package com.poslano.qrscannerapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "scanned_codes")
public class ScannedCode {
    @PrimaryKey(autoGenerate = true)
    public int id; // Primarni ključ koji se automatski generira

    public String qrValue; // Vrijednost skeniranog QR koda
    public long timestamp; // Datum i vrijeme

    // Konstruktor za stvaranje objekata
    public ScannedCode(String qrValue) {
        this.qrValue = qrValue;
        this.timestamp = System.currentTimeMillis();
    }
}
