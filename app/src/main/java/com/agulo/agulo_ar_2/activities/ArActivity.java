package com.agulo.agulo_ar_2.activities;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.agulo.agulo_ar_2.R;
import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class ArActivity extends AppCompatActivity {

    private ArFragment arFragment;
    //private String url = "https://modelviewer.dev/shared-assets/models/Astronaut.glb";
    //private String url = "https://modelviewer.dev/assets/ShopifyModels/ToyTrain.glb";
    //private String url = "https://github.com/google/model-viewer/blob/master/packages/shared-assets/models/shishkebab.glb?raw=true";
    //private String url = "https://github.com/Harichachi/Agulo_AR_modelos/blob/main/IronMan.obj?raw=true";
    //private String url = "https://github.com/Harichachi/Agulo_AR_modelos/blob/main/ironman.glb?raw=true";
    //private String url = "https://github.com/Harichachi/Agulo_AR_modelos/blob/main/sputnik.glb?raw=true";
    //private String url = "https://github.com/Harichachi/Agulo_AR_modelos_Publico/blob/main/shishkebab.glb?raw=true";
    private String url = "https://github.com/Harichachi/Agulo_AR_modelos_Publico/blob/main/personaje_negro_chica.png?raw=true";
    private String urlTarget = "https://github.com/Harichachi/Agulo_AR_modelos_Publico/blob/main/imagen_target_prueba.jpeg";

    private Button masInfo;
    private Button capturar;
    private AugmentedImage imagen;
    CompletableFuture<ModelRenderable> completableFuture;
    //private CompletableFuture

    Bitmap bitmap;

    private int n = 0;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);
        masInfo = findViewById(R.id.arMasinfo);
        capturar = findViewById(R.id.arCapturar);

        //Vemos si tenemos permiso de acceder a los datos
        validaPermisos();

        //Botón de más info
        masInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent n = new Intent(ArActivity.this,FichaPuntoActivity.class);
                startActivity(n);
            }
        });

        //Botón capturar
        capturar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //capturar();
                if(validaPermisos()) {
                    capturar();
                }
                else{
                    Toast.makeText(ArActivity.this, "No hay permisos!",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        DrawObject(url);

    }
    //Método para dibujar el objeto
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void DrawObject(String objectName) {
            arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
                if(n == 0) {
                    Anchor anchor = hitResult.createAnchor();

                    completableFuture = ModelRenderable.builder()
                            /*.setSource(this,
                                    RenderableSource.builder().setSource(
                                            this,
                                            Uri.parse(objectName),
                                            RenderableSource.SourceType.GLB)
                                            .setScale(0.008f)
                                            .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                                            .build())*/
                            .setSource(this,Uri.parse(objectName))
                            .setRegistryId(objectName)
                            //.setSource(this,Uri.parse(objectName))
                            //.setRegistryId(objectName)
                            .build();

                    completableFuture.thenAccept(modelRenderable -> addModelToScene(anchor, modelRenderable))
                            .exceptionally(throwable -> {
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder.setMessage(throwable.getMessage()).show();
                                return null;
                            });
                    n++;
                }
                else{
                    Log.e("Ya no se puede mas"," No se puede añadir más objetos");
                }
            });



    }

    //Método para añadir el modelo a la escena
    private void addModelToScene(Anchor anchor, ModelRenderable modelRenderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
        transformableNode.setParent(anchorNode);
        transformableNode.setRenderable(modelRenderable);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        transformableNode.getRotationController().setEnabled(false);
        transformableNode.getScaleController().setEnabled(false);
        transformableNode.getTranslationController().setEnabled(false);
        transformableNode.select();
    }

    //Método para capturar la pantalla
    private void capturar(){
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            //Creamos el contexto a capturar
            Context contexto = arFragment.getArSceneView().getContext();
            View vista = arFragment.getArSceneView();
            //Context contexto = getWindow().getDecorView().getRootView().getContext();
            //View vista = getWindow().getDecorView().getRootView();
            /*View contexto = getWindow().getDecorView().getRootView();
            contexto.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(contexto.getDrawingCache());
            contexto.setDrawingCacheEnabled(false);*/

            //ContextWrapper cw = new ContextWrapper(contexto.getContext());

            //Guardamos la imagen
            OutputStream outputStream;
            //File fichero = cw.getExternalFilesDir(mPath);

            ContentValues contentValues = new ContentValues();
            ContentResolver contentResolver = contexto.getContentResolver();

            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME,now + ".jpg"); //Nombre
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, ImageFormat.JPEG); //Tipo
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/AguloAR"); //Directorio
            contentValues.put(MediaStore.Images.Media.IS_PENDING,1); //La imagen se está procesando

            Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            Uri imagenUri = contentResolver.insert(collection,contentValues);

            //Abrimos el flujo para la escritura
            outputStream = contentResolver.openOutputStream(imagenUri);

            //Limpiamos los valores
            contentValues.clear();
            contentValues.put(MediaStore.Images.Media.IS_PENDING,0); //Ya se terminó de procesar

            //Actualizamos los datos que estamos introduciendo
            contentResolver.update(imagenUri,contentValues,null,null);

            //Guardamos la imagen
            vista.setDrawingCacheEnabled(true);
            vista.buildDrawingCache();
            bitmap = Bitmap.createBitmap(vista.getDrawingCache());

            /*Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            final Bitmap bitmap = Bitmap.createBitmap(size.x, size.y, Bitmap.Config.ARGB_4444);

            final Resources.Theme theme = contexto.getTheme();
            final TypedArray ta = theme
                    .obtainStyledAttributes(new int[] { android.R.attr.windowBackground });
            final int res = ta.getResourceId(0, 0);
            final Drawable background = vista.getResources().getDrawable(res);

            Canvas canvas = new Canvas(bitmap);
            background.draw(canvas);
            vista.draw(canvas);*/
            //bitmap = vista.getDrawingCache();
            vista.setDrawingCacheEnabled(false);

            //


            boolean guardado = bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
            if(guardado){
                Toast.makeText(this, "Se ha guardado la imagen!" ,
                        Toast.LENGTH_LONG).show();
            }
            //Limpiamos el outputstream
            if(outputStream != null){
                outputStream.flush();
                outputStream.close();
            }
            //if()

            //Guardamos la imagen
            /*Intent galleryIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri picUri = Uri.fromFile(fichero);
            galleryIntent.putExtra(MediaStore.EXTRA_OUTPUT, picUri);
            Toast.makeText(this, "Se ha realizado la captura!" + picUri ,
                    Toast.LENGTH_LONG).show();*/


            /*File outputFile = new File(fichero,now + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            int quality = 100;
            screenShot(contexto).compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(outputFile.getAbsolutePath())));
            if(outputFile.exists()){
                Toast.makeText(this, "Existe la foto!" ,
                        Toast.LENGTH_LONG).show();
            }

            Toast.makeText(this, "Se ha realizado la captura!" + outputFile.getAbsolutePath() ,
                        Toast.LENGTH_LONG).show();*/

        } catch (Throwable e) {
            e.printStackTrace();
            Toast.makeText(this, "No se ha podido sacar la foto: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();

        }
    }

    //Método para pedir permiso de lectura
    private boolean validaPermisos() {

        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.M){
            return true;
        }

        if((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)&&
                (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED)){
            return true;
        }

        if((shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) ||
                (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))){
        }else{
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},100);
        }

        return false;
    }
}

