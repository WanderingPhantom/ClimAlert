package com.example.climalert;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.climalert.databinding.FragmentMapsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private FragmentMapsBinding binding;
    String[][] res;
    LocationManager locationManager;
    AlertDialog alert = null;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    double lat;
    double lon;
    LatLng ll1;
    LatLng ll2;
    Marker UBI1;
    Marker UBI2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://climalert.herokuapp.com/notificacion";

        // Request a string response from the provided URL.
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        JSONObject Notificacion;
                        res = new String[response.length()][6];
                        try {
                            // Log.d("ALGO", String.valueOf(response.length()));
                            for (int i = 0; i < response.length(); ++i) {
                                Notificacion = response.getJSONObject(i);
                                JSONObject incidenciaFenomeno = Notificacion.getJSONObject("incidenciaFenomeno");
                                res[i][0] = incidenciaFenomeno.getString("fecha");
                                JSONObject IndicacionIncidencia = Notificacion.getJSONObject("indicacionIncidencia");
                                res[i][1] = IndicacionIncidencia.getString("indicacion");
                                JSONObject incidencia = incidenciaFenomeno.getJSONObject("incidencia");
                                res[i][2] = incidencia.getString("radio");
                                JSONObject localizacion = incidencia.getJSONObject("localizacion");
                                res[i][3] = localizacion.getString("latitud");
                                res[i][4] = localizacion.getString("longitud");
                                JSONObject femomenoMeteo = incidenciaFenomeno.getJSONObject("fenomenoMeteo");
                                res[i][5] = femomenoMeteo.getString("nombre");
                                Log.d("ALGO", "estoy en el bucle");
                                Log.d("ALGO", res[i][0]);

                                       /*  Log.d("ALGO2","1");
                                         Log.d("ALGO2",res[0][0]);
                                         Log.d("ALGO2",res[0][1]);
                                         Log.d("ALGO2",res[0][2]);
                                         Log.d("ALGO2",res[0][3]);
                                         Log.d("ALGO2",res[0][4]);*/
                            }
                            Log.d("ALGO", "he acabado el bucle");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("ALGO", "SOCORRO");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        // Add the request to the RequestQueue.
        queue.add(request);

        binding = FragmentMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getloc();

        Button locActual = findViewById(R.id.loc0);
        Button loc1 = findViewById(R.id.loc1);
        Button loc2 = findViewById(R.id.loc2);
        locActual.setOnClickListener(this);
        loc1.setOnClickListener(this);
        loc2.setOnClickListener(this);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //cada cuanto quiero la loc y que hacer cuando pille una nueva
        // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);*/
    }

    @Override
    public void onClick(View v) {
        Log.d("XD", "onClick: VA1");
        switch (v.getId()) {
            case R.id.loc0:
                if(mMap.isMyLocationEnabled()){
                    Log.d("XD", "onClick: VA2");
                    LatLng act = new LatLng(lat, lon);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(act,8f));
                }
                break;
            case R.id.loc1:
                if(ll1 != null){
                    Log.d("XD", "onClick: VA3");
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ll1,10f));
                }
                break;
            case R.id.loc2:
                if(ll2 != null){
                    Log.d("XD", "onClick: VA4");
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ll2,10f));
                }
                break;
        }
    }



    private void getloc() {
        // Get the location manager
        LocationManager locationManager = (LocationManager)
                getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Alert(0, null);
        }
        Criteria criteria = new Criteria();
        Log.d("per","entro en permisoss1");
        String bestProvider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 99);
            Log.d("per","entro en permisoss");
            return;
        }
        Location location = locationManager.getLastKnownLocation(bestProvider);
        LocationListener loc_listener = new LocationListener() {
            public void onLocationChanged(Location l) {
            }
            public void onProviderEnabled(String p) {
            }
            public void onProviderDisabled(String p) {
            }
            public void onStatusChanged(String p, int status, Bundle extras) {
            }
        };
        Log.d("per","casi entro en permisos");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager
                .requestLocationUpdates(bestProvider, 20, 20.0f, loc_listener);
        location = locationManager.getLastKnownLocation(bestProvider);
        try {
            lat = location.getLatitude();
            lon = location.getLongitude();
            Log.d("ALGO", String.valueOf(lat));
        } catch (NullPointerException e) {
            lat = -1.0;
            lon = -1.0;
            Log.d("ALGO", "ASDASDA");
        }
    }

    private BitmapDescriptor bitmapDescriptorFromVector(MapsActivity context, @DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(this, R.drawable.ic_snow);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(this, vectorDrawableResourceId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth() + 0, vectorDrawable.getIntrinsicHeight() + 0);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void drawCircle(LatLng point, int rad) {
        // Instantiating CircleOptions to draw a circle around the marker
        CircleOptions circleOptions = new CircleOptions();
        // Specifying the center of the circle
        circleOptions.center(point);
        // Radius of the circle
        circleOptions.radius(rad);
        // Border color of the circle
        circleOptions.strokeColor(Color.BLACK);
        // Fill color of the circle
        circleOptions.fillColor(Color.argb(150, 235, 165, 171));
        // Border width of the circle
        circleOptions.strokeWidth(2);
        // Adding the circle to the GoogleMap
        mMap.addCircle(circleOptions);
    }


    public void generarMarcadores(LatLng latLng, String info, String tip, int radio) {
        Log.d("ALGO","3");
        mMap.addMarker(new MarkerOptions()
                .icon(bitmapDescriptorFromVector(MapsActivity.this, R.drawable.ic_snow))
                .snippet(info)
                .position(latLng)
                .alpha(0.7f)
                .title(tip));
        drawCircle(latLng, radio * 2000);
    }

    private void Alert(int i, LatLng latLng) {
        if(i == 0) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("El sistema GPS esta desactivado, ¿Desea activarlo?")
                    .setCancelable(false)
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            alert = builder.create();
            alert.show();

        }
        if(i == 1) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("¿Deseas añadir este punto como ubicación secundaria 1?")
                    .setCancelable(false)
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            ll1 = latLng;

                            UBI1 = mMap.addMarker(new MarkerOptions()
                                    .anchor(0.0f, 1.0f)
                                    .alpha(0.7f)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                                    .position(ll1));
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            alert = builder.create();
            alert.show();
        }
        if(i == 2) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("¿Deseas añadir este punto como ubicación secundaria 2?")
                    .setCancelable(false)
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            ll2 = latLng;
                            UBI2 = mMap.addMarker(new MarkerOptions()
                                    .anchor(0.0f, 1.0f)
                                    .alpha(0.7f)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                                    .position(ll2));
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            alert = builder.create();
            alert.show();
        }
        if(i == 3) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Ya has añadido dos ubicaciones secundarias, ¿deseas eliminar alguna?")
                    .setCancelable(false)
                    .setPositiveButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    })
                    .setNeutralButton("La primera  ", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            UBI1.remove();
                            ll1 = null;
                        }
                    })
                    .setNegativeButton("La segunda", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            UBI2.remove();
                            ll2 = null;
                        }
                    });
            alert = builder.create();
            alert.show();

        }

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

/*
        LatLng Incendio = new LatLng(41.61760161197995, -0.7357996859859309);
        LatLng Terremoto = new LatLng(41.05635778865671, -3.0090123514715965);
        LatLng Tsunami = new LatLng(40.71786141774278, 0.7363462963273005);
        mMap.addMarker(new MarkerOptions().position(Incendio).title("Incendio")
                .snippet("Propagación rápida, evita el humo").icon(bitmapDescriptorFromVector(this,R.drawable.ic_fire)));
        drawCircle(Terremoto, 50000);

        mMap.addMarker(new MarkerOptions().position(Terremoto).title("Terremoto")
                .snippet("Escondete debajo de la mesa").icon(bitmapDescriptorFromVector(this,R.drawable.ic_baseline_terrain_24)));
        drawCircle(Incendio, 20000);
        mMap.addMarker(new MarkerOptions().position(Tsunami).title("Tsunami")
                .snippet("Impacto en una hora").icon(bitmapDescriptorFromVector(this,R.drawable.ic_snow)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Terremoto,7f));
*/
/*
        Log.d("ALGO","accedo a res");
        for(int i = 0; i < res.length; ++i) {
            Log.d("ALGO","accedo a res en el bucle");
            LatLng ll = new LatLng(Double.parseDouble(res[i][3]),Double.parseDouble(res[i][4]));
            generarMarcadores(ll,res[i][1],res[i][5],Integer.parseInt(res[i][2]));
            Log.d("ALGO", res[i][1] + res[i][5] + (res[i][2]));

        }
 */

        //añadir marcador manteniendo pulsado (buscar como sacar una ventana para confirmar)
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng latLng) {
                if(ll1 == null) {
                    Alert(1, latLng);
                }
                else if(ll2 == null) {
                    Alert(2, latLng);
                }
                else{
                    Alert(3, null);
                }

            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        else{
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            LatLng act = new LatLng(lat,lon);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(act,10f));
        }

    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}