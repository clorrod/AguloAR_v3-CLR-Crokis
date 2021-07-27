package com.agulo.agulo_ar_2.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.agulo.agulo_ar_2.R;
import com.google.android.material.textfield.TextInputLayout;

public class alertDialog {

    //Constructor
    public alertDialog(){}

    //Método para crear una alerta
    public void crearAlertaOk(Context mContext, String titulo){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View v = LayoutInflater.from(mContext).inflate(R.layout.dialog_alert,null);
        builder.setView(v);

        //view.findViewById(R.id.constraintLayoutDialog)
        ((TextView) v.findViewById(R.id.mensajeDialog)).setText(titulo);

        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(false);
        ((ImageButton)v.findViewById(R.id.botonDialog)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });
        alert.show();
    }

    //Método para crear una alerta con un evento en el click
    public void crearAlerta(Context mContext, String titulo, View.OnClickListener listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View v = LayoutInflater.from(mContext).inflate(R.layout.dialog_alert,null);
        builder.setView(v);

        //view.findViewById(R.id.constraintLayoutDialog)
        ((TextView) v.findViewById(R.id.mensajeDialog)).setText(titulo);

        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(false);
        ((ImageButton)v.findViewById(R.id.botonDialog)).setOnClickListener(listener);
        alert.show();
    }

}
