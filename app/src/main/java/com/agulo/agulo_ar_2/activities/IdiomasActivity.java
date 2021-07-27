package com.agulo.agulo_ar_2.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.agulo.agulo_ar_2.R;

import java.util.Locale;

public class IdiomasActivity extends AppCompatActivity {

    Button espanol;
    Button ingles;
    Button aleman;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idiomas);

        espanol = findViewById(R.id.botonEspanol);
        ingles = findViewById(R.id.botonIngles);
        aleman = findViewById(R.id.botonAleman);

        espanol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Cambiamos el idioma
                Locale localizacion = new Locale("es", "ES");
                Locale.setDefault(localizacion);
                Configuration config = new Configuration();
                config.locale = localizacion;
                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

                //Iniciamos la pantalla principal
                Intent n = new Intent(IdiomasActivity.this,PrincipalActivity.class);
                startActivity(n);
            }
        });

        ingles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Cambiamos el idioma
                Locale localizacion = new Locale("en", "EN");
                Locale.setDefault(localizacion);
                Configuration config = new Configuration();
                config.locale = localizacion;
                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

                //Iniciamos la pantalla principal
                Intent n = new Intent(IdiomasActivity.this,PrincipalActivity.class);
                startActivity(n);
            }
        });
        aleman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Cambiamos el idioma
                Locale localizacion = new Locale("de", "DE");
                Locale.setDefault(localizacion);
                Configuration config = new Configuration();
                config.locale = localizacion;
                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

                //Iniciamos la pantalla principal
                Intent n = new Intent(IdiomasActivity.this,PrincipalActivity.class);
                startActivity(n);
            }
        });

    }
}