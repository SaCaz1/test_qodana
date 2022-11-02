package com.example.outilpourfairefaceauxcanicules;

import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.outilpourfairefaceauxcanicules.databinding.ActivityMapsBinding;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    List <Marker> arbres = new ArrayList<>();
    List <Marker> clim = new ArrayList<>();
    List <Marker> eau = new ArrayList<>();
    List <Marker> parcs = new ArrayList<>();
    int counter = 0;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        UiSettings settings = mMap.getUiSettings();
        settings.setZoomControlsEnabled(true);

        // Add a marker in Repentigny and move the camera
        LatLng repentigny = new LatLng(45.753284, -73.440079);
        mMap.addMarker(new MarkerOptions().position(repentigny).title("Marker in Repentigny"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(repentigny,15));

        //Marqueurs d'arbres -- regex being weird here I think
        arbres.addAll(addDataPoint(mMap, "data-arbres.csv", 8, 9, "Arbre", BitmapDescriptorFactory.HUE_GREEN));
        arbres.addAll(addDataPoint(mMap, "data-mtl-arbres-publics.csv", 21, 20, "Arbre", BitmapDescriptorFactory.HUE_GREEN));

        //Marqueurs de bâtiments climatisés
        clim.addAll(addDataPoint(mMap, "data-clim.csv",12, 11, "Bâtiment climatisé", BitmapDescriptorFactory.HUE_VIOLET));
        clim.addAll(addDataPoint(mMap, "clim-mtl.csv",1,0,"Bâtiment climatisé",BitmapDescriptorFactory.HUE_VIOLET));

        //Marqueurs de parcs
        parcs.addAll(addDataPoint(mMap, "data-parcs.csv", 7, 6, "Parc", BitmapDescriptorFactory.HUE_ORANGE));

        //Marqueurs d'eau
        eau.addAll(addDataPoint(mMap, "data-mtl-piscines.csv", 11, 10, "Piscine", BitmapDescriptorFactory.HUE_BLUE));
        eau.addAll(addDataPoint(mMap, "data-eau.csv",15,14,"Eau",BitmapDescriptorFactory.HUE_BLUE));
        eau.addAll(addDataPoint(mMap, "data-mtl-fontaine-eau.csv",12,11,"Point d'eau", BitmapDescriptorFactory.HUE_BLUE));

        //Marqueurs d'ilôts de chaleur à éviter

        LatLng ilotsLatLng = new LatLng(45.753284, -73.440079);
        GroundOverlayOptions ilotsChaleur = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromAsset("ilots-chaleurs.bmp"))
                .position(ilotsLatLng, 8600f, 6500f)
                .transparency(0.5f);
        mMap.addGroundOverlay(ilotsChaleur);
    }

    //General function for looping over each dataset and adding it to the map.
    public List<Marker> addDataPoint(GoogleMap map, String filename, int lat, int longi, String type, float colour) {
        List<List<String>> records = new ArrayList<>();
        AssetManager assetManager = getAssets();
        List<Marker> markers = new ArrayList<>();

        try {
            InputStream input = assetManager.open(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line;
            reader.readLine(); //reads first line
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                records.add(Arrays.asList(values));
                LatLng coord = new LatLng(Double.parseDouble(values[lat]), Double.parseDouble(values[longi]));
                //Adding the elemtn with visibility = False to be able to switch between visible and not visible.
                Marker marker = mMap.addMarker(new MarkerOptions().position(coord).title(type).snippet(infos.get(counter % (infos.size()))).visible(false).icon(BitmapDescriptorFactory.defaultMarker(colour)));
                counter = counter + 1;
                markers.add(marker);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return markers;
    }



    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.checkbox_arbres:
                if (checked)
                    for (Marker a : arbres)
                        a.setVisible(true);
                else
                    for (Marker a : arbres)
                        a.setVisible(false);
                break;
            case R.id.checkbox_eau:
                if (checked)
                    for (Marker e : eau)
                        e.setVisible(true);
                else
                    for (Marker e : eau)
                        e.setVisible(false);
                break;
            case R.id.checkbox_clim:
                if (checked)
                    for (Marker c : clim)
                        c.setVisible(true);
                else
                    for (Marker c : clim)
                        c.setVisible(false);
            case R.id.checkbox_parcs:
                if (checked)
                    for (Marker p : parcs)
                        p.setVisible(true);
                else
                    for (Marker p : parcs)
                        p.setVisible(false);
                break;
            case R.id.checkbox_chaleur:
                //if (checked)
                break;
        }
    }

    //Liste des informations éducatives à inclure dans l'application.
    List<String> infos = Arrays.asList("Les arbres sont bons pour diminuer la chaleur",
            "Les parcs peuvent procurer une meilleure solution.",
            "info sur les piscines", "s'hyadrater est important.");


}