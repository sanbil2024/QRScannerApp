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
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<String> scanHistory;
    private Context context;

    public HistoryAdapter(Context context, List<String> scanHistory) {
        this.context = context;
        this.scanHistory = scanHistory;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String qrValue = scanHistory.get(position);
        holder.textView.setText(qrValue);

        // Postavljanje click listenera za svaki skenirani kod
        holder.itemView.setOnClickListener(v -> {
            if (qrValue.startsWith("http")) {
                openURL(qrValue);
            } else {
                showToast("Ovo nije važeći URL: " + qrValue);
            }
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
