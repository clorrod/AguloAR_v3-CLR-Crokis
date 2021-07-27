package com.agulo.agulo_ar_2.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.agulo.agulo_ar_2.R;
import com.agulo.agulo_ar_2.utils.myARNode;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.rendering.ExternalTexture;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class ARActivityVideoTarget extends AppCompatActivity implements Scene.OnUpdateListener {

    private ArSceneView arSceneView;
    private Session session;
    private boolean configurarSesion = false;
    private Boolean started = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_r_video_target);
        //Vista
        arSceneView = findViewById(R.id.arFragmentVideoTarget);

        //Pedimos los permisos
        Dexter.withActivity(this).withPermission(Manifest.permission.CAMERA).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                setSession(); //Comenzamos la sesión
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                //Cuadro de diálogo para que permita los permisos
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

            }
        }).check();


        //Comenzamos la vista de AR
        empezarARView();

    }


    //Método para hacer la sesión
    public void setSession(){
        if(session == null){
            try {
                session = new Session(this);

            } catch (UnavailableArcoreNotInstalledException e) {
                e.printStackTrace();
            } catch (UnavailableApkTooOldException e) {
                e.printStackTrace();
            } catch (UnavailableSdkTooOldException e) {
                e.printStackTrace();
            } catch (UnavailableDeviceNotCompatibleException e) {
                e.printStackTrace();
            }
            configurarSesion = true;
        }
        if(configurarSesion){
            configSesion();
            configurarSesion = false;
            arSceneView.setupSession(session);
        }
        try {
            session.resume();
            arSceneView.resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
            session = null;
            return;
        }
    }

    //Método para configurar la sesión
    public void configSesion(){

        Config config = new Config(session);
        if(!buildDatabase(config)){
            Toast.makeText(this,"Error database",Toast.LENGTH_LONG);
        }
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
        session.configure(config);
    }

    //Método para crear database
    private boolean buildDatabase(Config config) {
        AugmentedImageDatabase augmentedImageDatabase;
        Bitmap bitmap = loadImage();
        if(bitmap == null){
            return false;
        }
        augmentedImageDatabase = new AugmentedImageDatabase(session);
        augmentedImageDatabase.addImage("pruebaVideo",bitmap);
        config.setAugmentedImageDatabase(augmentedImageDatabase);
        return true;
    }

    //Método para cargar imagen
    private Bitmap loadImage() {
        try {
            InputStream inputStream = getAssets().open("imagen_target_prueba.jpeg");
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Método para comenzar la vista AR
    private void empezarARView() {
        arSceneView.getScene().addOnUpdateListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onUpdate(FrameTime frameTime) {
        Frame frame = arSceneView.getArFrame();
        Collection<AugmentedImage> updateAugmentedImage = frame.getUpdatedTrackables(AugmentedImage.class);

        for(AugmentedImage imagen : updateAugmentedImage){
            if(imagen.getTrackingState() == TrackingState.TRACKING && !started){
                if(imagen.getName().equals("pruebaVideo")){
                    myARNode myARNode = new myARNode(this);
                    myARNode.setImagen(imagen);
                    arSceneView.getScene().addChild(myARNode);
                    started = true;
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Pedimos los permisos
        Dexter.withActivity(this).withPermission(Manifest.permission.CAMERA).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                setSession(); //Comenzamos la sesión
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                //Cuadro de diálogo para que permita los permisos
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

            }
        }).check();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(session != null){
            arSceneView.pause();
            session.pause();
        }
    }

}