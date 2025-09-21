package com.example.lab2_20212624;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.lab2_20212624.databinding.ActivityHistorialBinding;

import java.util.List;

public class HistorialActivity extends AppCompatActivity {

    private static final String TAG = "HistorialActivity";

    private ActivityHistorialBinding binding;
    
    private HistorialManager historialManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHistorialBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        inicializarComponentes();
        configurarEventos();
        cargarHistorial();
    }
    
    private void inicializarComponentes() {
        historialManager = HistorialManager.getInstance(this);
    }
    
    private void configurarEventos() {
        binding.btnVolverAJugar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoConfirmacion();
            }
        });

    }
    
    private void cargarHistorial() {
        try {
            List<HistorialManager.HistorialEntry> historial = historialManager.obtenerHistorial();

            if (historial.isEmpty()) {
                mostrarMensajeVacio();
            } else {
                mostrarEntradasHistorial(historial);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error al cargar historial: " + e.getMessage());
            Toast.makeText(this, "Error al cargar historial", Toast.LENGTH_SHORT).show();
        }
    }
    
    
    private void mostrarMensajeVacio() {
        TextView tvVacio = new TextView(this);
        tvVacio.setText("🎯 ¡Aún no hay historial!\n\nComienza tu primera sesión de TeleCat para ver tu historial aquí.");
        tvVacio.setTextSize(16);
        tvVacio.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvVacio.setPadding(32, 32, 32, 32);
        tvVacio.setTextColor(getResources().getColor(android.R.color.darker_gray));
        
        binding.layoutHistorial.addView(tvVacio);
    }
    
    private void mostrarEntradasHistorial(List<HistorialManager.HistorialEntry> historial) {
        for (int i = 0; i < historial.size(); i++) {
            HistorialManager.HistorialEntry entry = historial.get(i);
            CardView cardView = crearCardHistorial(entry, i + 1);
            binding.layoutHistorial.addView(cardView);

            if (i < historial.size() - 1) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) cardView.getLayoutParams();
                params.bottomMargin = 16;
                cardView.setLayoutParams(params);
            }
        }
    }
    
    private CardView crearCardHistorial(HistorialManager.HistorialEntry entry, int numeroSesion) {
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(12);
        cardView.setCardElevation(4);
        cardView.setPadding(20, 20, 20, 20);

        LinearLayout layoutInterno = new LinearLayout(this);
        layoutInterno.setOrientation(LinearLayout.VERTICAL);

        TextView tvTitulo = new TextView(this);
        tvTitulo.setText("Interacción " + numeroSesion);
        tvTitulo.setTextSize(18);
        tvTitulo.setTypeface(null, Typeface.BOLD);
        tvTitulo.setTextColor(getResources().getColor(R.color.purple_700));
        tvTitulo.setPadding(0, 0, 0, 8);

        TextView tvInfo = new TextView(this);
        String textoInfo = String.format(
            "%d imágenes",
            entry.getCantidadImagenes()
        );

        if (entry.getTextoPersonalizado() != null && !entry.getTextoPersonalizado().trim().isEmpty()) {
            textoInfo += " - Texto: " + entry.getTextoPersonalizado();
        }
        
        tvInfo.setText(textoInfo);
        tvInfo.setTextSize(14);
        tvInfo.setTextColor(getResources().getColor(android.R.color.black));
        tvInfo.setPadding(0, 0, 0, 8);

        layoutInterno.addView(tvTitulo);
        layoutInterno.addView(tvInfo);

        cardView.addView(layoutInterno);
        
        return cardView;
    }
    
    private void mostrarDialogoConfirmacion() {
        // Crear el diálogo
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_confirmar_juego);
        dialog.setCancelable(true);

        dialog.getWindow().setLayout(
            (int) (getResources().getDisplayMetrics().widthPixels * 0.85),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        // Obtener referencias a los botones
        Button btnSi = dialog.findViewById(R.id.btnSi);
        Button btnNo = dialog.findViewById(R.id.btnNo);
        
        // Configurar botón Sí
        btnSi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                // Limpiar estado global del timer
                TeleCatActivity.limpiarEstadoGlobal();
                
                // Volver a MainActivity
                Intent intent = new Intent(HistorialActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        
        // Configurar botón No
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

}
