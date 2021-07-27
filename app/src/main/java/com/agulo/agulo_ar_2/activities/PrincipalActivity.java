package com.agulo.agulo_ar_2.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.agulo.agulo_ar_2.R;

public class PrincipalActivity extends AppCompatActivity {

    Button visita;
    Button info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        visita = findViewById(R.id.botonVisitasPrincipal);
        info = findViewById(R.id.botonInfoPrincipal);

        //Botón Visita
        visita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent n = new Intent(PrincipalActivity.this, VisitasActivity.class);
                startActivity(n);
            }
        });

        //Botón info recorridos
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent n = new Intent(PrincipalActivity.this, InfoRecorridosActivity.class);
                startActivity(n);
            }
        });

    }
}