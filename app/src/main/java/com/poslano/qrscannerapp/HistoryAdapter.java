package com.poslano.qrscannerapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<ScannedCode> scanHistory;
    private Context context;
    private OnItemLongClickListener longClickListener;

    // Sučelje za long click listener
    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    public HistoryAdapter(Context context, List<ScannedCode> scanHistory, OnItemLongClickListener longClickListener) {
        this.context = context;
        this.scanHistory = scanHistory;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScannedCode scannedCode = scanHistory.get(position);
        holder.textView.setText(scannedCode.qrValue);

        // Formatiranje datuma i vremena
        String formattedDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(scannedCode.timestamp));
        holder.timestampView.setText(formattedDate);

        // Postavljanje click listenera za otvaranje URL-a
        holder.itemView.setOnClickListener(v -> {
            try {
                URL parsedUrl = new URL(scannedCode.qrValue); // Provjera je li URL validan
                openURL(parsedUrl.toString());
            } catch (MalformedURLException e) {
                showToast("Ovo nije važeći URL: " + scannedCode.qrValue); // Obavijest za korisnika
            }
        });

        // Postavljanje long click listenera za brisanje unosa
        holder.itemView.setOnLongClickListener(v -> {
            longClickListener.onItemLongClick(position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return scanHistory.size();
    }

    private void openURL(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        TextView timestampView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
            timestampView = itemView.findViewById(android.R.id.text2);
        }
    }
}