package com.poslano.qrscannerapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface ScannedCodeDao {

    // Metoda za umetanje jednog QR koda u bazu
    @Insert
    void insert(ScannedCode scannedCode);

    // Metoda za dohvaćanje svih skeniranih QR kodova
    @Query("SELECT * FROM scanned_codes")
    List<ScannedCode> getAllScannedCodes();

    // Metoda za brisanje svih zapisa iz baze
    @Query("DELETE FROM scanned_codes")
    void deleteAll();

    // Metoda za brisanje jednog
    @Delete
    void delete(ScannedCode scannedCode);
}
