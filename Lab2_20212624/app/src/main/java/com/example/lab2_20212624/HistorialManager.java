package com.example.lab2_20212624;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistorialManager {
    
    private static final String TAG = "HistorialManager";
    private static final String PREF_NAME = "telecat_historial";
    private static final String KEY_HISTORIAL = "historial_data";
    private static final int MAX_ENTRIES = 50; // Máximo 50 entradas en el historial
    
    private static HistorialManager instance;
    private SharedPreferences sharedPreferences;
    private Context context;
    
    private HistorialManager(Context context) {
        this.context = context.getApplicationContext();
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized HistorialManager getInstance(Context context) {
        if (instance == null) {
            instance = new HistorialManager(context);
        }
        return instance;
    }
    
    public void agregarInteraccion(int cantidadImagenes, String textoPersonalizado) {
        try {
            List<HistorialEntry> historial = obtenerHistorial();

            HistorialEntry nuevaEntrada = new HistorialEntry(
                cantidadImagenes,
                textoPersonalizado != null ? textoPersonalizado : "",
                new Date()
            );
            
            // Agregar al inicio de la lista
            historial.add(0, nuevaEntrada);

            if (historial.size() > MAX_ENTRIES) {
                historial = historial.subList(0, MAX_ENTRIES);
            }

            guardarHistorial(historial);
            
            Log.d(TAG, "Interacción agregada al historial: " + cantidadImagenes + " imágenes, texto: '" + textoPersonalizado + "'");
            
        } catch (Exception e) {
            Log.e(TAG, "Error al agregar interacción al historial: " + e.getMessage());
        }
    }
    
    public List<HistorialEntry> obtenerHistorial() {
        List<HistorialEntry> historial = new ArrayList<>();
        
        try {
            String historialJson = sharedPreferences.getString(KEY_HISTORIAL, "[]");
            JSONArray jsonArray = new JSONArray(historialJson);
            
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                
                int cantidad = jsonObject.getInt("cantidad");
                String texto = jsonObject.getString("texto");
                String fechaStr = jsonObject.getString("fecha");
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date fecha = sdf.parse(fechaStr);
                
                historial.add(new HistorialEntry(cantidad, texto, fecha));
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener historial: " + e.getMessage());
        }
        
        return historial;
    }
    
    private void guardarHistorial(List<HistorialEntry> historial) {
        try {
            JSONArray jsonArray = new JSONArray();
            
            for (HistorialEntry entry : historial) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("cantidad", entry.getCantidadImagenes());
                jsonObject.put("texto", entry.getTextoPersonalizado());
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                jsonObject.put("fecha", sdf.format(entry.getFecha()));
                
                jsonArray.put(jsonObject);
            }
            
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_HISTORIAL, jsonArray.toString());
            editor.apply();
            
        } catch (JSONException e) {
            Log.e(TAG, "Error al guardar historial: " + e.getMessage());
        }
    }
    
    public void limpiarHistorial() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_HISTORIAL);
        editor.apply();
        Log.d(TAG, "Historial limpiado");
    }
    
    public int obtenerTotalSesiones() {
        return obtenerHistorial().size();
    }
    
    public int obtenerTotalImagenes() {
        List<HistorialEntry> historial = obtenerHistorial();
        int total = 0;
        for (HistorialEntry entry : historial) {
            total += entry.getCantidadImagenes();
        }
        return total;
    }

    public static class HistorialEntry {
        private int cantidadImagenes;
        private String textoPersonalizado;
        private Date fecha;
        
        public HistorialEntry(int cantidadImagenes, String textoPersonalizado, Date fecha) {
            this.cantidadImagenes = cantidadImagenes;
            this.textoPersonalizado = textoPersonalizado;
            this.fecha = fecha;
        }
        
        public int getCantidadImagenes() {
            return cantidadImagenes;
        }
        
        public String getTextoPersonalizado() {
            return textoPersonalizado;
        }
        
        public Date getFecha() {
            return fecha;
        }
        
        public String getFechaFormateada() {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return sdf.format(fecha);
        }
        
        public String getTextoResumen() {
            if (textoPersonalizado == null || textoPersonalizado.trim().isEmpty()) {
                return "Sin texto personalizado";
            } else {
                return textoPersonalizado.length() > 20 ? 
                    textoPersonalizado.substring(0, 20) + "..." : 
                    textoPersonalizado;
            }
        }
    }
}
