package com.poslano.qrscannerapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int CAMERA_PERMISSION_CODE = 100;

    private ScannedCodeDatabase database; // Room baza podataka
    private List<ScannedCode> scanHistory = new ArrayList<>();
    private HistoryAdapter adapter;
    private RecyclerView recyclerView;
    private Button btnScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnScan = findViewById(R.id.btnScan);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Inicijaliziraj adapter s long click listenerom
        adapter = new HistoryAdapter(this, scanHistory, this::deleteEntry);
        recyclerView.setAdapter(adapter);

        // Provjera dozvole za kameru
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        } else {
            initializeDatabase();
        }

        btnScan.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Dozvola za kameru nije odobrena!", Toast.LENGTH_SHORT).show();
                requestCameraPermission();
            }
        });
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Toast.makeText(this, "Ova aplikacija zahtijeva dozvolu za kameru", Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }

    private void initializeDatabase() {
        database = ScannedCodeDatabase.getDatabase(this);

        // Dohvati postojeće QR kodove iz baze
        new Thread(() -> {
            List<ScannedCode> storedCodes = database.scannedCodeDao().getAllScannedCodes();
            scanHistory.addAll(storedCodes); // Dodaj QR kodove u listu
            runOnUiThread(() -> adapter.notifyDataSetChanged()); // Osvježi prikaz
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Dozvola za kameru odobrena", Toast.LENGTH_SHORT).show();
                initializeDatabase(); // Inicijaliziraj bazu nakon što je dozvola odobrena
            } else {
                Toast.makeText(this, "Dozvola za kameru odbijena. Aplikacija neće raditi ispravno!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            scanBarcode(imageBitmap);
        }
    }

    private void scanBarcode(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        BarcodeScanner scanner = BarcodeScanning.getClient();

        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    for (Barcode barcode : barcodes) {
                        String qrValue = barcode.getRawValue();
                        ScannedCode scannedCode = new ScannedCode(qrValue);
                        scanHistory.add(scannedCode);
                        adapter.notifyDataSetChanged();

                        // Spremi QR kod u bazu
                        new Thread(() -> database.scannedCodeDao().insert(scannedCode)).start();

                        if (qrValue.startsWith("http")) {
                            openURL(qrValue);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Greška kod skeniranja!", Toast.LENGTH_SHORT).show());
    }

    private void deleteEntry(int position) {
        // Provjera je li pozicija unutar valjanog raspona
        if (position >= 0 && position < scanHistory.size()) {
            ScannedCode scannedCode = scanHistory.get(position); // Dohvati unos na poziciji

            // Ukloni unos iz baze podataka
            new Thread(() -> {
                database.scannedCodeDao().delete(scannedCode); // Brisanje iz baze
                runOnUiThread(() -> {
                    // Nakon što je iz baze izbrisano, uklonite ga iz liste i osvježite prikaz
                    scanHistory.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, scanHistory.size()); // Osvježi ostale stavke
                });
            }).start();
        } else {
            Toast.makeText(this, "Nevažeća pozicija za brisanje!", Toast.LENGTH_SHORT).show();
        }
    }



    private void openURL(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
