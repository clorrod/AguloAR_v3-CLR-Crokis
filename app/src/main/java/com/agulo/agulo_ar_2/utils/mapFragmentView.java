package com.agulo.agulo_ar_2.utils;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.agulo.agulo_ar_2.R;
import com.agulo.agulo_ar_2.activities.ARActivityVideo;
import com.agulo.agulo_ar_2.activities.ARActivityVideoTarget;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPolyline;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.IconCategory;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.MapEngine;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.common.RoadElement;
import com.here.android.mpa.common.ViewObject;
import com.here.android.mpa.guidance.LaneInformation;
import com.here.android.mpa.guidance.NavigationManager;
import com.here.android.mpa.mapping.AndroidXMapFragment;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapGesture;
import com.here.android.mpa.mapping.MapLabeledMarker;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.mapping.MapPolyline;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.mapping.MapState;
import com.here.android.mpa.mapping.OnMapRenderListener;
import com.here.android.mpa.routing.CoreRouter;
import com.here.android.mpa.routing.Route;
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteResult;
import com.here.android.mpa.routing.RouteWaypoint;
import com.here.android.mpa.routing.RoutingError;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class mapFragmentView{
    private AndroidXMapFragment m_mapFragment;
    private Map m_map;

    private MapMarker m_positionIndicatorFixed = null;
    private PointF m_mapTransformCenter;
    private boolean m_returningToRoadViewMode = false;
    private double m_lastZoomLevelInRoadViewMode = 0.0;
    private AppCompatActivity m_activity;
    private MapRoute m_currentRoute;
    private LinearLayout m_laneInfoView;
    private MapLabeledMarker marcadorTitulo;
    private alertDialog alertDialog = new alertDialog();
    private ProgressDialog prog;

    String[] longitudes;
    String[] latitudes;
    String[] titulos;

    //Constructor
    public mapFragmentView(AppCompatActivity activity) {
        m_activity = activity;
        initMapFragment();
        initVoicePackagesButton();
    }

    //Método para recoger el mapa
    private AndroidXMapFragment getMapFragment() {
        return (AndroidXMapFragment) m_activity.getSupportFragmentManager().findFragmentById(R.id.mapfragment);
    }

    //Método para iniciar el mapa
    private void initMapFragment(){
        prog = new ProgressDialog(m_activity);;
        m_mapFragment = getMapFragment();
        m_laneInfoView = m_activity.findViewById(R.id.laneInfoLayout);
        // This will use external storage to save map cache data, it is also possible to set
        // private app's path
        String path = new File(m_activity.getExternalFilesDir(null), ".here-map-data")
                .getAbsolutePath();
        //Log.e("path",new File(m_activity.getExternalFilesDir(null), ".here-map-data").getAbsolutePath() + "");
        // This method will throw IllegalArgumentException if provided path is not writable
        com.here.android.mpa.common.MapSettings.setDiskCacheRootPath(path);

        if (m_mapFragment != null) {

            /* Initialize the AndroidXMapFragment, results will be given via the called back. */
            m_mapFragment.init(new OnEngineInitListener() {
                @Override
                public void onEngineInitializationCompleted(OnEngineInitListener.Error error) {

                    if (error == OnEngineInitListener.Error.NONE) {
                        m_mapFragment.getMapGesture().addOnGestureListener(gestureListener, 100, true);
                        // retrieve a reference of the map from the map fragment
                        m_map = m_mapFragment.getMap();

                        PositioningManager.getInstance().start(PositioningManager.LocationMethod.GPS_NETWORK); //Ubicación usuario

                        //GeoCoordinate centro = PositioningManager.getInstance().getLastKnownPosition().getCoordinate();
                        GeoCoordinate centro =  new GeoCoordinate(PositioningManager.getInstance().getLastKnownPosition().getCoordinate().getLatitude(),
                                PositioningManager.getInstance().getLastKnownPosition().getCoordinate().getLongitude(),0.0);
                        //centro.setAltitude(0.0);
                        //centro.setAltitude(0.0);

                        //Añadimos dónde estamos en el mapa
                        m_map.setCenter(centro, Map.Animation.NONE);
                        /*m_map.setCenter(new GeoCoordinate(28.18823, -17.19464, 0.0),
                                Map.Animation.NONE);*/ //Donde aparecemos en el mapa
                        m_map.setMapScheme(Map.Scheme.CARNAV_DAY); //Estilo del mapa
                        m_map.setProjectionMode(Map.Projection.MERCATOR); //Proyección del plano
                        m_map.setZoomLevel(16.5);
                        m_map.addTransformListener(onTransformListener);


                        // Ponemos el marcador para ver donde estamos
                        m_mapFragment.getPositionIndicator().setVisible(true);
                        m_mapFragment.getPositionIndicator().setSmoothPositionChange(true);
                        m_mapFragment.getPositionIndicator().setAccuracyIndicatorVisible(true);
                        Log.e("Position indicator co",m_mapFragment.getPositionIndicator().getAccuracyIndicatorColor()+ "");
                        //m_positionIndicatorFixed.setCoordinate(PositioningManager.getInstance().getPosition().getCoordinate());
                        //m_map.addMapObject(m_positionIndicatorFixed);

                        //Añdimos los marcadores
                        longitudes = m_activity.getResources().getStringArray(R.array.longitudes);
                        latitudes = m_activity.getResources().getStringArray(R.array.latitudes);
                        titulos = m_activity.getResources().getStringArray(R.array.titulos_paradas);
                        for (int i = 0; i < longitudes.length; i++) {
                            marcadorTitulo = new MapLabeledMarker(new GeoCoordinate(Double.parseDouble(longitudes[i]),
                                    Double.parseDouble(latitudes[i]), 0.0));
                            marcadorTitulo.setTitle(titulos[i]);
                            marcadorTitulo.setLabelText("spa", titulos[i]); //https://www.loc.gov/marc/languages/language_code.html
                            marcadorTitulo.setIcon(IconCategory.ZOO);
                            m_map.addMapObject(marcadorTitulo);

                            /*
                            marcador = new MapMarker();
                            marcador.setCoordinate(new GeoCoordinate(Double.parseDouble(longitudes[i]),
                                    Double.parseDouble(latitudes[i]),0.0));

                            marcador.setTitle(titulos[i]);
                            marcador.setDescription("Hola");

                            Log.e("Titulo" ,marcador.getTitle());

                            m_map.addMapObject(marcador);
                            Image img = new Image();
                            try {
                                img.setImageResource(R.drawable.gps_position);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            //marcador.setIcon(img);*/
                        }

                        //Creamos la ruta
                        //m_map.addMapObject(createPolyline());
                        Log.e("Posicion del usuario", PositioningManager.getInstance().hasValidPosition() + "");


                    } else {
                        new AlertDialog.Builder(m_activity).setMessage(
                                "Error : " + error.name() + "\n\n" + error.getDetails())
                                .setTitle("Error")
                                .setNegativeButton(android.R.string.cancel,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                m_activity.finish();
                                            }
                                        }).create().show();
                    }
                }
            });

            m_mapFragment.addOnMapRenderListener(new OnMapRenderListener() {
                @Override
                public void onPreDraw() {
                    if (m_positionIndicatorFixed != null) {
                        if (NavigationManager.getInstance()
                                .getMapUpdateMode().equals(NavigationManager
                                        .MapUpdateMode.POSITION)) {
                            if (!m_returningToRoadViewMode) {
                                GeoCoordinate geoCoordinate = m_map.pixelToGeo(m_mapTransformCenter);
                                //geoCoordinate.setAltitude(0.0);
                                if (geoCoordinate != null && geoCoordinate.isValid()) {
                                    m_positionIndicatorFixed.setCoordinate(geoCoordinate);
                                }
                            }
                        }
                    }
                }

                @Override
                public void onPostDraw(boolean var1, long var2) {
                }

                @Override
                public void onSizeChanged(int var1, int var2) {
                }

                @Override
                public void onGraphicsDetached() {
                }

                @Override
                public void onRenderBufferCreated() {
                }
            });

                //if (m_mapFragment.getPositionIndicator() != null) {
                  //  if (m_mapFragment.getPositionIndicator().isAccuracyIndicatorVisible()) {
                        m_activity.findViewById(R.id.calculate).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //m_positionIndicatorFixed.setVisible(false); //quitamos la posición inicial del usuario
                                calculateAndStartNavigation();
                            }
                        });
                    //}
                //}
            }
    }

    private void calculateAndStartNavigation() {
        if (m_map == null) {
            Toast.makeText(m_activity, "El mapa aún no está disponible", Toast.LENGTH_SHORT).show();
            return;
        }
        if (NavigationManager.getInstance().getRunningState()
                == NavigationManager.NavigationState.RUNNING) {
            Toast.makeText(m_activity, "Se está navegando", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        final RoutePlan routePlan = new RoutePlan();
        //Punto de salida y de entrada
        /*routePlan.addWaypoint(new RouteWaypoint(new GeoCoordinate(Double.parseDouble(
                m_activity.getResources().getString(R.string.cueva_aborigen_longitud)),
                Double.parseDouble(
                        m_activity.getResources().getString(R.string.cueva_aborigen_latitud)))));
        routePlan.addWaypoint(new RouteWaypoint(new GeoCoordinate(Double.parseDouble(
                m_activity.getResources().getString(R.string.el_chorro_longitud)),
                Double.parseDouble(
                        m_activity.getResources().getString(R.string.el_chorro_latitud)))));*/

        routePlan.addWaypoint(new RouteWaypoint(PositioningManager.getInstance().getPosition().getCoordinate()));
        //PositioningManager.LocationMethod metodo = PositioningManager.LocationMethod.GPS_NETWORK;


        routePlan.addWaypoint(new RouteWaypoint((new GeoCoordinate(28.4264,-16.3058))));


        // Calculamos la ruta
        CoreRouter coreRouter = new CoreRouter();
        coreRouter.calculateRoute(routePlan, new CoreRouter.Listener() {
            @Override
            public void onCalculateRouteFinished(List<RouteResult> list,
                                                 RoutingError routingError) {
                if (routingError == RoutingError.NONE) {
                    Route route = list.get(0).getRoute();

                    m_currentRoute = new MapRoute(route);
                    m_map.addMapObject(m_currentRoute);

                    m_currentRoute.setColor(Color.GREEN); //Color de la ruta
                    m_currentRoute.setOutlineColor(Color.TRANSPARENT);
                    m_currentRoute.setTraveledColor(Color.TRANSPARENT);
                    //m_currentRoute.setUpcomingColor(Color.CYAN);

                    m_map.addMapObject(m_currentRoute); //Lo añadimos al mapa

                    //PONEMOS LAS OPCIONES DE RUTA
                    RouteOptions routeOptions = new RouteOptions();
                    routeOptions.setTransportMode(RouteOptions.TransportMode.PEDESTRIAN);//La ruta es a pie
                    routeOptions.setHighwaysAllowed(true);
                    routeOptions.setRouteType(RouteOptions.Type.SHORTEST); //Ponemos la ruta más corta
                    //routeOptions.setRouteCount(1); //Calculamos una ruta solo
                    routePlan.setRouteOptions(routeOptions); //Añadimos las opciones a la ruta


                    //Movemos el mapa al inicio de la ruta
                    m_map.setCenter(routePlan.getWaypoint(0).getNavigablePosition(),
                            Map.Animation.NONE);
                    //Para ajustar el 3D de la navegación
                    m_map.setTilt(80);
                    m_map.setZoomLevel(19);

                    //Ponemos un marcador al final de la ruta
                    Image i = new Image();
                    MapMarker m = new MapMarker();
                    try {
                        i.setImageResource(R.drawable.gps_position);
                        m.setIcon(i);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    m.setVisible(true);
                    m.setCoordinate(routePlan.getWaypoint(1).getNavigablePosition());
                    m_map.addMapObject(m);


                    NavigationManager navigationManager =
                            NavigationManager.getInstance();
                    navigationManager.setMapUpdateMode(
                            NavigationManager.MapUpdateMode.POSITION_ANIMATION);

                    //Ajustamos el centro de la navegación


                    m_mapTransformCenter = new PointF(m_map.getTransformCenter().x, (m_map
                            .getTransformCenter().y * 85 / 50)); //85 / 50
                    m_map.setTransformCenter(m_mapTransformCenter);

                    // Creamos un marcador por dónde vamos
                    Image icon = new Image();
                    m_positionIndicatorFixed = new MapMarker();
                    try {
                        icon.setImageResource(R.drawable.gps_position);
                        m_positionIndicatorFixed.setIcon(icon);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    m_mapFragment.getPositionIndicator().setVisible(true);
                    m_positionIndicatorFixed.setVisible(false);
                    m_positionIndicatorFixed.setCoordinate(m_map.getCenter());
                    //m_positionIndicatorFixed.setCoordinate(PositioningManager.getInstance().getPosition().getCoordinate());
                    m_map.addMapObject(m_positionIndicatorFixed);
                    Log.e("Posición del indicador",m_positionIndicatorFixed.getCoordinate() + "");

                    //Añadimos la info
                    navigationManager.addLaneInformationListener(
                            new WeakReference<>(m_laneInformationListener));

                    //Añadimos el rerouting
                    //navigationManager.addRerouteListener(new WeakReference<>(rerouteListener));

                    //Para ver updates en las posiciones
                    PositioningManager.getInstance().addListener(
                            new WeakReference<>(mapPositionHandler));


                    //Para ver updates del RoadView que nos dice dónde está el centro
                    navigationManager.getRoadView().addListener(
                            new WeakReference<>(roadViewListener));


                    navigationManager.addNavigationManagerEventListener(
                            new WeakReference<>(
                                    navigationManagerEventListener));

                    //Vemos cuando se llega al lugar
                    NavigationManager.NavigationManagerEventListener navigationManagerEventListener = new NavigationManager.NavigationManagerEventListener() {
                        @Override
                        public void onEnded(NavigationManager.NavigationMode navigationMode) {
                            super.onEnded(navigationMode);
                            alertDialog.crearAlerta(m_activity, "Texto", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //Intent n = new Intent(m_activity, ArActivity.class);
                                    //Intent n = new Intent(m_activity, ARActivityTarget.class);
                                    //Intent n = new Intent(m_activity, ARActivityVideo.class);
                                    Intent n = new Intent(m_activity, ARActivityVideoTarget.class);
                                    m_activity.startActivity(n);
                                }
                            });
                            Toast.makeText(m_activity,
                                    "Hemos llegado! ",
                                    Toast.LENGTH_LONG).show(); //https://github.com/heremaps/here-android-sdk-examples/blob/master/turn-by-turn-navigation/app/src/main/java/com/here/android/example/guidance/MapFragmentView.java
                        }
                    };
                    navigationManager.addNavigationManagerEventListener(new WeakReference<>(navigationManagerEventListener));

                    //Toast.makeText(m_activity, m_laneInformationListener + "", Toast.LENGTH_LONG).show();

                    //Para info de la navegación
                    navigationManager.getDistanceUnit();

                    //Para simular la ruta
                    navigationManager.simulate(route, 20);

                    navigationManager.setMap(m_map); //Para iniciar la navegación en el mismo mapa
                    //navigationManager.startNavigation(route); //Para empezar la ruta;
                    if(navigationManager.getInstance().getRunningState() == NavigationManager.NavigationState.RUNNING) {

                    }

                } else {
                    Toast.makeText(m_activity,
                            "Error:no se ha podido calcular la ruta " + routingError,
                            Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onProgress(int i) {

            }
        });
    }

    private void initVoicePackagesButton() {
        Button m_voicePackagesButton = m_activity.findViewById(R.id.voiceCtrlButton);
        m_voicePackagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(m_activity, voiceSkinsActivity.class);
                m_activity.startActivity(intent);
            }
        });
    }

    //Eventos de posición
    private PositioningManager.OnPositionChangedListener mapPositionHandler = new PositioningManager.OnPositionChangedListener() {
        @Override
        public void onPositionUpdated(PositioningManager.LocationMethod method, GeoPosition position,
                                      boolean isMapMatched) {
            if (NavigationManager.getInstance().getMapUpdateMode().equals(NavigationManager
                    .MapUpdateMode.NONE) && !m_returningToRoadViewMode)
                Toast.makeText(m_activity, "Position updated", Toast.LENGTH_SHORT).show();
                m_positionIndicatorFixed.setCoordinate(position.getCoordinate());
                //rerouteListener.onRerouteBegin();
                m_mapFragment.getPositionIndicator().setSmoothPositionChange(true);
        }

        @Override
        public void onPositionFixChanged(PositioningManager.LocationMethod method,
                                         PositioningManager.LocationStatus status) {


        }
    };

    //Pausar el RoadView para que el mapa deje de moverse
    private void pauseRoadView() {

        if (NavigationManager.getInstance().getMapUpdateMode().equals(NavigationManager.MapUpdateMode.POSITION)) {
            NavigationManager.getInstance().setMapUpdateMode(NavigationManager.MapUpdateMode.POSITION_ANIMATION);
            NavigationManager.getInstance().getRoadView().removeListener(roadViewListener);
            m_lastZoomLevelInRoadViewMode = m_map.getZoomLevel();
        }
    }

    //Mueve el mapa a la posición en la que estaba
    private void resumeRoadView() {
        m_map.setCenter(PositioningManager.getInstance().getPosition().getCoordinate(), Map
                        .Animation.NONE, m_lastZoomLevelInRoadViewMode, Map.MOVE_PRESERVE_ORIENTATION,
                80);
        m_returningToRoadViewMode = true;
    }

    private MapGesture.OnGestureListener gestureListener = new MapGesture.OnGestureListener() {
        @Override
        public void onPanStart() {
            pauseRoadView();
        }

        @Override
        public void onPanEnd() {
        }

        @Override
        public void onMultiFingerManipulationStart() {
        }

        @Override
        public void onMultiFingerManipulationEnd() {
        }

        @Override
        public boolean onMapObjectsSelected(List<ViewObject> objects) {
            //Aparece la descripción cuando se clica en el marcador
            for (ViewObject viewObject : objects) {
                if (viewObject.getBaseType() == ViewObject.Type.USER_OBJECT) {
                    MapObject mapObject = (MapObject) viewObject;

                    if (mapObject.getType() == MapObject.Type.LABELED_MARKER) {

                        MapMarker window_marker = ((MapLabeledMarker) mapObject);

                        Log.e("Parada", "Título: " + window_marker.getTitle());
                    }

                }
            }
            return false;
        }

        @Override
        public boolean onTapEvent(PointF p) {
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(PointF p) {
            return false;
        }

        @Override
        public void onPinchLocked() {
        }

        @Override
        public boolean onPinchZoomEvent(float scaleFactor, PointF p) {
            pauseRoadView();
            return false;
        }

        @Override
        public void onRotateLocked() {
        }

        @Override
        public boolean onRotateEvent(float rotateAngle) {
            return false;
        }

        @Override
        public boolean onTiltEvent(float angle) {
            pauseRoadView();
            return false;
        }

        @Override
        public boolean onLongPressEvent(PointF p) {
            return false;
        }

        @Override
        public void onLongPressRelease() {
        }

        @Override
        public boolean onTwoFingerTapEvent(PointF p) {
            return false;
        }
    };

    final private NavigationManager.RoadView.Listener roadViewListener = new NavigationManager.RoadView.Listener() {
        @Override
        public void onPositionChanged(GeoCoordinate geoCoordinate) {
            m_mapTransformCenter = m_map.projectToPixel
                    (geoCoordinate).getResult();
        }
    };

    final private Map.OnTransformListener onTransformListener = new Map.OnTransformListener() {
        @Override
        public void onMapTransformStart() {
        }

        @Override
        public void onMapTransformEnd(MapState mapsState) {
            if (m_returningToRoadViewMode) {
                NavigationManager.getInstance().setMapUpdateMode(NavigationManager.MapUpdateMode
                        .POSITION_ANIMATION);
                NavigationManager.getInstance().getRoadView()
                        .addListener(new WeakReference<>(roadViewListener));
                m_returningToRoadViewMode = false;
            }
        }
    };

    final private NavigationManager.NavigationManagerEventListener navigationManagerEventListener =
            new NavigationManager.NavigationManagerEventListener() {
                @Override
                public void onRouteUpdated(Route route) { //AQUI PROBAR LUEGO
                    prog.setTitle("Rerouting");
                    m_map.removeMapObject(m_currentRoute);
                    m_currentRoute = new MapRoute(route);
                    m_currentRoute.setUpcomingColor(Color.RED);
                    m_map.addMapObject(m_currentRoute);
                    prog.dismiss();
                    Toast.makeText(m_activity, "Route updated", Toast.LENGTH_SHORT).show();
                    //Toast.makeText(m_activity, "Ruta: " + m_currentRoute, Toast.LENGTH_SHORT).show();
                    //rerouteListener.onRerouteBegin();
                }
            };

    //Rerouting
    /*private NavigationManager.RerouteListener rerouteListener = new NavigationManager.RerouteListener() {
        @Override
        public void onRerouteBegin() {
            super.onRerouteBegin();
            prog.setTitle("Rerouting");
            prog.show();

        }

        @Override
        public void onRerouteEnd(@NonNull RouteResult routeResult, RoutingError routingError) {
            super.onRerouteEnd(routeResult, routingError);
            prog.dismiss();
        }
    };*/
    //Instrucciones de la navegación
    final private NavigationManager.LaneInformationListener
            m_laneInformationListener = new NavigationManager.LaneInformationListener() {
        @Override
        public void onLaneInformation(@NonNull List<LaneInformation> list, @Nullable RoadElement roadElement) {
            super.onLaneInformation(list, roadElement);
            laneInfoUtils.displayLaneInformation(m_laneInfoView,list);
        }
    };

    public void onDestroy() {
        if (m_map != null) {
            m_map.removeMapObject(m_positionIndicatorFixed);
        }
        if (MapEngine.isInitialized()) {
            NavigationManager.getInstance().stop();
            PositioningManager.getInstance().stop();
        }

        NavigationManager.getInstance().removeLaneInformationListener(m_laneInformationListener);
        NavigationManager.getInstance()
                .removeNavigationManagerEventListener(navigationManagerEventListener);
    }

    public void onBackPressed() {
        if (NavigationManager.getInstance().getMapUpdateMode().equals(NavigationManager
                .MapUpdateMode.NONE)) {
            resumeRoadView();
        } else {
            m_activity.finish();
        }
    }

    //Método para dibujar la ruta
    private MapPolyline createPolyline() {
        ArrayList<GeoCoordinate> coordinates = new ArrayList<>();
        for(int i = 0; i < longitudes.length; i++){
            coordinates.add(new GeoCoordinate(Double.parseDouble(
                    longitudes[i]),Double.parseDouble(latitudes[i])));
            Log.e("Coordenadas de " + titulos[i],longitudes[i] + "," + latitudes[i]);

            final RoutePlan routePlan = new RoutePlan();
            // these two waypoints cover suburban roads
            //Ruta
            if(i != longitudes.length) {
                routePlan.addWaypoint(new RouteWaypoint(new GeoCoordinate(Double.parseDouble(
                        longitudes[i]),Double.parseDouble(latitudes[i]))));
                routePlan.addWaypoint(new RouteWaypoint(new GeoCoordinate(Double.parseDouble(
                        longitudes[i + 1]),Double.parseDouble(latitudes[i + 1]))));
            }
            else{
                routePlan.addWaypoint(new RouteWaypoint(new GeoCoordinate(Double.parseDouble(
                        longitudes[i - 1]),Double.parseDouble(latitudes[i - 1]))));
                routePlan.addWaypoint(new RouteWaypoint(new GeoCoordinate(Double.parseDouble(
                        longitudes[i]),Double.parseDouble(latitudes[i]))));
            }
        }

        GeoPolyline geoPolyline;
        try {
            geoPolyline = new GeoPolyline(coordinates);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        MapPolyline mapPolyline = new MapPolyline(geoPolyline);
        mapPolyline.setLineColor(Color.RED);
        mapPolyline.setLineWidth(10);

        return mapPolyline;
    }
}
