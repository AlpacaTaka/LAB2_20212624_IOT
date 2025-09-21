package com.example.lab2_20212624;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab2_20212624.databinding.ActivityTelecatBinding;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TeleCatActivity extends AppCompatActivity {

    private static final String TAG = "TeleCatActivity";
    private static final long TIEMPO_POR_IMAGEN = 4000; // 4 segundos por imagen

    private ActivityTelecatBinding binding;
    

    private CountDownTimer countDownTimer;
    private ExecutorService executorService;
    private Handler mainHandler;

    private int cantidadImagenes;
    private long tiempoTotalMs;
    private long tiempoRestanteMs;
    private int imagenActualIndex = 0;
    private String textoPersonalizado = "";
    private boolean timerTerminado = false;

    private static long tiempoRestanteGlobal = 0;
    private static boolean timerActivoGlobal = false;

    public static void limpiarEstadoGlobal() {
        tiempoRestanteGlobal = 0;
        timerActivoGlobal = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Inicializar View Binding
        binding = ActivityTelecatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        inicializarComponentes();
        obtenerDatosIntent();
        configurarExecutorService();
        
        if (savedInstanceState != null) {
            restaurarEstado(savedInstanceState);
        } else if (timerActivoGlobal && tiempoRestanteGlobal > 0) {
            // Restaurar desde variables estáticas (rotación de pantalla)
            tiempoRestanteMs = tiempoRestanteGlobal;
            iniciarTimer();
        } else {
            // Primera vez que se abre la actividad
            inicializarTimer();
        }
        
        cargarPrimeraImagen();
        configurarEventos();
    }
    
    private void inicializarComponentes() {
        mainHandler = new Handler(Looper.getMainLooper());
    }
    
    private void obtenerDatosIntent() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            cantidadImagenes = extras.getInt("cantidad", 3);
            textoPersonalizado = extras.getString("texto", "");
            
            binding.tvCantidad.setText("Cantidad = " + cantidadImagenes);
            tiempoTotalMs = cantidadImagenes * TIEMPO_POR_IMAGEN;
        } else {
            // Valores por defecto
            cantidadImagenes = 3;
            tiempoTotalMs = cantidadImagenes * TIEMPO_POR_IMAGEN;
            binding.tvCantidad.setText("Cantidad = " + cantidadImagenes);
        }
    }
    
    private void configurarExecutorService() {
        executorService = Executors.newFixedThreadPool(2);
    }
    
    private void inicializarTimer() {
        tiempoRestanteMs = tiempoTotalMs;
        timerActivoGlobal = true;
        tiempoRestanteGlobal = tiempoRestanteMs;
        iniciarTimer();
    }
    
    private void iniciarTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        
        countDownTimer = new CountDownTimer(tiempoRestanteMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tiempoRestanteMs = millisUntilFinished;
                tiempoRestanteGlobal = millisUntilFinished;
                
                // Actualizar UI del tiempo
                long segundos = millisUntilFinished / 1000;
                long minutos = segundos / 60;
                segundos = segundos % 60;
                
                String tiempoFormateado = String.format("%02d:%02d", minutos, segundos);
                binding.tvTiempoRestante.setText(tiempoFormateado);
                
                // Verificar si es tiempo de cambiar imagen (cada 4 segundos)
                long tiempoTranscurridoMs = tiempoTotalMs - millisUntilFinished;
                int imagenQueDebeMostrar = (int) (tiempoTranscurridoMs / TIEMPO_POR_IMAGEN);
                
                if (imagenQueDebeMostrar > imagenActualIndex && imagenQueDebeMostrar < cantidadImagenes) {
                    imagenActualIndex = imagenQueDebeMostrar;
                    cargarSiguienteImagen();
                }
            }
            
            @Override
            public void onFinish() {
                timerTerminado = true;
                timerActivoGlobal = false;
                tiempoRestanteGlobal = 0;
                
                binding.tvTiempoRestante.setText("00:00");
                binding.btnSiguiente.setEnabled(true);
                binding.btnSiguiente.setBackgroundColor(0xFF27AE60); // Verde
                
                Toast.makeText(TeleCatActivity.this, "¡Tiempo terminado! Presiona Siguiente", 
                              Toast.LENGTH_LONG).show();
            }
        };
        
        countDownTimer.start();
    }
    
    private void cargarPrimeraImagen() {
        cargarImagenDeAPI(0);
    }
    
    private void cargarSiguienteImagen() {
        cargarImagenDeAPI(imagenActualIndex);
    }
    
    private void cargarImagenDeAPI(final int indiceImagen) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String urlImagen;
                    

                    if (textoPersonalizado != null && !textoPersonalizado.trim().isEmpty()) {

                        urlImagen = "https://cataas.com/cat/says/" + 
                                   java.net.URLEncoder.encode(textoPersonalizado.trim(), "UTF-8") +
                                   "?fontSize=30&fontColor=white";
                    } else {

                        urlImagen = "https://cataas.com/cat?t=" + System.currentTimeMillis(); // timestamp para evitar cache
                    }
                    
                    Log.d(TAG, "Cargando imagen " + (indiceImagen + 1) + " desde: " + urlImagen);

                    URL url = new URL(urlImagen);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.setConnectTimeout(10000); // 10 segundos timeout
                    connection.setReadTimeout(10000);
                    connection.connect();
                    
                    InputStream input = connection.getInputStream();
                    final Bitmap bitmap = BitmapFactory.decodeStream(input);
                    connection.disconnect();
                    

                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (bitmap != null) {
                                binding.ivGato.setImageBitmap(bitmap);
                                Log.d(TAG, "Imagen " + (indiceImagen + 1) + " cargada exitosamente");
                            } else {
                                Log.e(TAG, "Error al decodificar imagen " + (indiceImagen + 1));
                                mostrarImagenError();
                            }
                        }
                    });
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error al cargar imagen " + (indiceImagen + 1) + ": " + e.getMessage());
                    
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mostrarImagenError();
                        }
                    });
                }
            }
        });
    }
    
    private void mostrarImagenError() {
        // Mostrar imagen por defecto en caso de error
        binding.ivGato.setImageResource(android.R.drawable.ic_menu_gallery);
        Toast.makeText(this, "Error al cargar imagen", Toast.LENGTH_SHORT).show();
    }
    
    private void configurarEventos() {
        binding.btnSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timerTerminado) {

                    irAVistaFinalizacion();
                } else {
                    Toast.makeText(TeleCatActivity.this, 
                                  "Espera a que termine el tiempo", 
                                  Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    private void irAVistaFinalizacion() {

        guardarInteraccionEnHistorial();

        Intent intent = new Intent(this, HistorialActivity.class);
        startActivity(intent);
        

        finish();
    }
    
    private void guardarInteraccionEnHistorial() {
        try {
            HistorialManager historialManager = HistorialManager.getInstance(this);
            

            historialManager.agregarInteraccion(cantidadImagenes, textoPersonalizado);
            
            Log.d(TAG, "Interacción guardada en historial: " + cantidadImagenes + " imágenes, texto: '" + textoPersonalizado + "'");
            
            Toast.makeText(this, "¡Sesión guardada en el historial!", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Log.e(TAG, "Error al guardar en historial: " + e.getMessage());
            Toast.makeText(this, "Error al guardar sesión", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("tiempoRestante", tiempoRestanteMs);
        outState.putInt("imagenActualIndex", imagenActualIndex);
        outState.putBoolean("timerTerminado", timerTerminado);
    }
    
    private void restaurarEstado(Bundle savedInstanceState) {
        tiempoRestanteMs = savedInstanceState.getLong("tiempoRestante", tiempoTotalMs);
        imagenActualIndex = savedInstanceState.getInt("imagenActualIndex", 0);
        timerTerminado = savedInstanceState.getBoolean("timerTerminado", false);
        
        tiempoRestanteGlobal = tiempoRestanteMs;
        
        if (!timerTerminado && tiempoRestanteMs > 0) {
            iniciarTimer();
        } else if (timerTerminado) {
            binding.btnSiguiente.setEnabled(true);
            binding.btnSiguiente.setBackgroundColor(0xFF27AE60);
            binding.tvTiempoRestante.setText("00:00");
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }

        if (timerTerminado) {
            timerActivoGlobal = false;
            tiempoRestanteGlobal = 0;
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();

        Log.d(TAG, "Actividad pausada, timer continúa");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Actividad resumida");
        

        if (timerActivoGlobal && !timerTerminado) {

            Log.d(TAG, "Timer sigue activo, tiempo restante: " + tiempoRestanteGlobal + "ms");
        }
    }
}