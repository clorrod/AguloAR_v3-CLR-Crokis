package com.agulo.agulo_ar_2.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.agulo.agulo_ar_2.R;

public class VisitasActivity extends AppCompatActivity {


    Button iniciarRA;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitas);

        iniciarRA = findViewById(R.id.iniciarVisita);
        iniciarRA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent n = new Intent(VisitasActivity.this,MapaActivity.class);
                startActivity(n);
            }
        });

        Intent sceneViewerIntent = new Intent(this,ArActivity.class);

        /*iniciarRA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sceneViewerIntent.setData(Uri.parse("https://arvr.google.com/scene-viewer/1.0?file=https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Avocado/glTF/Avocado.gltf"));
                sceneViewerIntent.setPackage("com.google.android.googlequicksearchbox");
                startActivity(sceneViewerIntent);
            }
        });*/

    }
}