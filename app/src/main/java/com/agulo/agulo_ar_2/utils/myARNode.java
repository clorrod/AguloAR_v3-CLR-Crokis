package com.agulo.agulo_ar_2.utils;

import android.content.Context;
import android.graphics.ColorSpace;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.agulo.agulo_ar_2.R;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.ExternalTexture;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;

import java.util.concurrent.CompletableFuture;

public class myARNode extends AnchorNode {
    private AugmentedImage imagen;
    private CompletableFuture<ModelRenderable> completableFuture;
    private ExternalTexture externalTexture;
    private int modo;
    private MediaPlayer mediaPlayer;
    private ModelRenderable modelRen;

    //Constructor para modelos
    @RequiresApi(api = Build.VERSION_CODES.N)
    public myARNode(Context context, String modelId){
        if(completableFuture == null){
            completableFuture = ModelRenderable.builder()
                    .setSource(context,
                            RenderableSource.builder().setSource(
                                    context,
                                    Uri.parse(modelId),
                                    RenderableSource.SourceType.GLB)
                                    .setScale(0.001f)
                                    .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                                    .build())
                    .setRegistryId(modelId)
                    .build();
        }
        modo = 0;
    }

    //Constructor para vídeos
    @RequiresApi(api = Build.VERSION_CODES.N)
    public myARNode(Context context){
        if(completableFuture == null) {
            //Creamos la textura para poner el vídeo
            externalTexture = new ExternalTexture();
            mediaPlayer = MediaPlayer.create(context, R.raw.video_prueba);
            mediaPlayer.setSurface(externalTexture.getSurface());
            mediaPlayer.setLooping(true);

            completableFuture = ModelRenderable.builder()
                    .setSource(context,R.raw.video_screen_2)
                    .setRegistryId(R.raw.video_screen_2)
                    .build();
        }
        completableFuture.thenAccept(modelRenderable -> {
                    modelRen = modelRenderable;
                    modelRen.getMaterial().setExternalTexture("videoTexture", externalTexture);
                    modelRen.getMaterial().setFloat4("keyColor",new Color(0.01843f,1.0f,0.098f));
                });
        modo = 1;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setImagen(AugmentedImage imagen) {
        this.imagen = imagen;
        if(!completableFuture.isDone()){
            CompletableFuture.allOf(completableFuture)
                    .thenAccept((Void aVoid)-> {
                        setImagen(imagen);
                    })
                    .exceptionally(throwable -> {
                return null;
            });
        }
        setAnchor(imagen.createAnchor(imagen.getCenterPose()));
        Node node = new Node();
        node.setParent(this);

        if(modo == 0) {
            Pose pose = Pose.makeTranslation(0.0f, 0.0f, 0.25f);
            node.setLocalPosition(new Vector3(pose.tx(), pose.ty(), pose.tx()));
            node.setLocalRotation(new Quaternion(pose.qx(), pose.qy(), pose.qz(), pose.qw()));
            node.setRenderable(completableFuture.getNow(null));
        }
        if(modo == 1) {
            if(!mediaPlayer.isPlaying()){
                mediaPlayer.start();
                externalTexture.getSurfaceTexture().setOnFrameAvailableListener(surfaceTexture -> {
                    node.setRenderable(modelRen);
                    externalTexture.getSurfaceTexture().setOnFrameAvailableListener(null);
                });
            } //para videos
            else{
                node.setRenderable(modelRen);
            }
            float ancho = mediaPlayer.getVideoWidth();
            float alto = mediaPlayer.getVideoHeight();

            node.setLocalScale(new Vector3(0.95f * (ancho/alto),0.95f,0.95f));
        }

    }

    public AugmentedImage getImagen() {
        return imagen;
    }
}
