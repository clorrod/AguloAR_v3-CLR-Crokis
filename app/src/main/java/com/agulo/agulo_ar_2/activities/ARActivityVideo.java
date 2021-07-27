package com.agulo.agulo_ar_2.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.agulo.agulo_ar_2.R;
import com.agulo.agulo_ar_2.utils.myARNode;
import com.android.volley.toolbox.ImageLoader;
import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.ExternalTexture;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Texture;
import com.google.ar.sceneform.rendering.TextureInternalData;
import com.google.ar.sceneform.ux.ArFragment;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class ARActivityVideo extends AppCompatActivity {

    private ModelRenderable modelRen;
    private float HEIGHT = 0.95f;
    //private String urlBase = "https://github.com/heyletscode/Play-Video-On-Augmented-Image/blob/master/Project/app/src/main/assets/video_screen.sfb?raw=true";
    private String urlBase = "https://github.com/Harichachi/Agulo_AR_modelos_Publico/blob/main/base_imagen.glb?raw=true";
    private String urlImg = "https://github.com/Harichachi/Agulo_AR_modelos_Publico/blob/main/video_prueba.mp4?raw=true";

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_r_video);

        //Creamos la textura para poner el vídeo
        ExternalTexture externalTexture = new ExternalTexture();
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.video_prueba);
        mediaPlayer.setSurface(externalTexture.getSurface());
        mediaPlayer.setLooping(true);

        /*CompletableFuture<Texture> texture = Texture.builder().setSource(loadImage())
                .setUsage(Texture.Usage.NORMAL)
                .setSampler(
                        Texture.Sampler.builder()
                                //.setMagFilter(Texture.Sampler.MagFilter.LINEAR)
                                //.setMinFilter(Texture.Sampler.MinFilter.LINEAR_MIPMAP_LINEAR)
                                .build()).build();

        CompletableFuture<Material> m = MaterialFactory.makeOpaqueWithTexture(this, texture.getNow(null));*/

        //Renderizamos el modelo
        ModelRenderable.builder()
                /*.setSource(this,
                        RenderableSource.builder().setSource(
                                this,
                                Uri.parse(urlBase),
                                RenderableSource.SourceType.GLB)
                                .setScale(0.005f)
                                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                                .build())
                .setRegistryId(urlBase)*/
                .setSource(this,R.raw.video_screen_2)
                .build()
        .thenAccept(modelRenderable -> {
            modelRen = modelRenderable;
            //modelRen.setMaterial(m.getNow(null));
            //modelRen.getMaterial().setTexture("texture",texture.getNow(null));
            modelRen.getMaterial().setExternalTexture("videoTexture",externalTexture);
            //modelRen.getMaterial().setExternalTexture("default material",externalTexture);
            //modelRen.getMaterial().setExternalTexture("default material",externalTexture);
            Log.e("MATERIAL",modelRen.getMaterial().getExternalTexture("default material") + "");
            modelRen.getMaterial().setFloat4("keyColor",new Color(0.01843f,1.0f,0.098f));
        });

        ArFragment arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragmentVideo);
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            //Quaternion q = Quaternion.axisAngle(new Vector3(0.0f, 0.0f,1.0f), 90);

            AnchorNode anchorNode = new AnchorNode(hitResult.createAnchor());

            //anchorNode.setLocalRotation(q);

            //anchorNode = new AnchorNode(hitResult.createAnchor());

            //Lo rotamos
            //anchorNode.getLocalRotation().w = 90;
            //Pose pose = anchorNode.getAnchor().getPose();

            //Log.e("Node w",anchorNode.getLocalRotation() + "");
            /*
            Quaternion q1 = anchorNode.getLocalRotation();
            Log.e("Node loca rot",q1 + "");
            Quaternion q2 = Quaternion.axisAngle(new Vector3(0.0f, 0.0f,1.0f), 90);
            anchorNode.setWorldRotation(Quaternion.multiply(q1, q2));
            Log.e("Node loca rot_2",q1 + "");*/

            if(!mediaPlayer.isPlaying()){
                mediaPlayer.start();
                externalTexture.getSurfaceTexture().setOnFrameAvailableListener(surfaceTexture -> {
                    anchorNode.setRenderable(modelRen);
                    externalTexture.getSurfaceTexture().setOnFrameAvailableListener(null);
                });
            } //para videos
            else{
                anchorNode.setRenderable(modelRen);
            }

            float ancho = mediaPlayer.getVideoWidth();
            float alto = mediaPlayer.getVideoHeight();
            //float ancho = loadImage().getWidth();
            //float alto = loadImage().getHeight();

            //Lo posicionamos
            anchorNode.setLocalScale(new Vector3(HEIGHT * (ancho/alto),HEIGHT,0.95f));

            //anchorNode.setLocalRotation(q2);

            //Lo añadimos a la escena
            arFragment.getArSceneView().getScene().addChild(anchorNode);
        });

    }

    //Método para cargar la imagen
    private Bitmap loadImage() {
        try {
            InputStream inputStream = getAssets().open("imagen_target_prueba.jpeg");
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}