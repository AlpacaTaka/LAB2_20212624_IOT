package com.example.lab2_20212624;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lab2_20212624.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private boolean conexionVerificada = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        // Inicializar View Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Configurar listeners
        configurarListeners();
    }
    
    private void configurarListeners() {
        binding.rgTexto.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbSi) {
                    binding.etEscribirTexto.setEnabled(true);
                    binding.etEscribirTexto.setHint("Ingrese texto para las imágenes");
                } else if (checkedId == R.id.rbNo) {
                    binding.etEscribirTexto.setEnabled(false);
                    binding.etEscribirTexto.setText("");
                    binding.etEscribirTexto.setHint("Seleccione 'Sí' para habilitar este campo");
                }
            }
        });
        
        // Listener para el botón de comprobar conexión
        binding.btnComprobarConexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comprobarConexionInternet();
            }
        });
        
        // Listener para el botón de comenzar
        binding.btnComenzar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comenzarProceso();
            }
        });
    }
    
    private void comprobarConexionInternet() {
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            
            if (isConnected) {

                Toast.makeText(this, "Conexión a internet disponible",
                    Toast.LENGTH_LONG).show();
                conexionVerificada = true;
                binding.btnComenzar.setEnabled(true);
                binding.btnComprobarConexion.setEnabled(false); // Deshabilitar después de verificar
            } else {

                Toast.makeText(this, "No hay conexión a internet",
                    Toast.LENGTH_LONG).show();
                conexionVerificada = false;
                binding.btnComenzar.setEnabled(false);
            }
        } else {
            Toast.makeText(this, "No se puede verificar la conexión",
                Toast.LENGTH_LONG).show();
            conexionVerificada = false;
            binding.btnComenzar.setEnabled(false);
        }
    }
    
    private void comenzarProceso() {
        if (!conexionVerificada) {
            Toast.makeText(this, "Debe verificar la conexión a internet primero", 
                Toast.LENGTH_SHORT).show();
            return;
        }
        

        String cantidadTexto = binding.etCantidad.getText().toString().trim();
        if (cantidadTexto.isEmpty()) {
            Toast.makeText(this, "Debe ingresar una cantidad de imágenes", 
                Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validar que la cantidad sea un número válido
        try {
            int cantidad = Integer.parseInt(cantidadTexto);
            if (cantidad <= 0) {
                Toast.makeText(this, "La cantidad debe ser mayor a 0", 
                    Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ingrese una cantidad válida", 
                Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validar si se seleccionó "Sí" para texto y no se ingresó texto
        if (binding.rbSi.isChecked()) {
            String texto = binding.etEscribirTexto.getText().toString().trim();
            if (texto.isEmpty()) {
                Toast.makeText(this, "Debe ingresar texto ya que seleccionó 'Sí'", 
                    Toast.LENGTH_SHORT).show();
                return;
            }
        }
        

        Toast.makeText(this, "¡Proceso iniciado correctamente!",
            Toast.LENGTH_SHORT).show();

        // Limpiar estado global del timer antes de comenzar
        TeleCatActivity.limpiarEstadoGlobal();


        Intent intent = new Intent(this, TeleCatActivity.class);
        intent.putExtra("cantidad", Integer.parseInt(cantidadTexto));

        if (binding.rbSi.isChecked()) {
            intent.putExtra("texto", binding.etEscribirTexto.getText().toString().trim());
        }
        
        startActivity(intent);
    }
}