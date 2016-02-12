package com.example.claudiu.investitiipublice.IRUserInterface;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.example.claudiu.initiativaromania.R;
import com.example.claudiu.investitiipublice.IRObjects.Contract;
import com.example.claudiu.investitiipublice.IRObjects.ContractManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.LinkedList;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    /* UI consts */
    private static final String TAB_MAP             = "Harta";
    private static final String TAB_STATISTICS      = "Statistici";
    public static final int CIRCLE_DEFAULT_RADIUS   = 550;
    public static final int CIRCLE_MIN_RADIUS       = 100;
    public static final int CIRCLE_MAX_RADIUS       = 5000;
    private static final int CIRCLE_ALPHA           = 70;
    private static final int CIRCLE_MARGIN          = 2;
    private static final int MAP_DEFAULT_ZOOM       = 14;
    private static final int DEFAULT_COLOR_RED      = 119;
    private static final int DEFAULT_COLOR_GREEN    = 203;
    private static final int DEFAULT_COLOR_BLUE     = 212;


    /* Setup Objects */
    private GoogleMap mMap;
    private IRLocationListener locationListener = null;
    private IRSeekBarListener seekBarListener = null;
    private String tabtitles[] = new String[] {TAB_MAP, TAB_STATISTICS };

    /* UI objects */
    private Circle circle;
    private Marker currentPos;
    private SeekBar seekBar;
    private SupportMapFragment mapFragment;
    private int currentTab = 0, lastTab = 0;

    /* Data objects */
    HashMap<Marker, Contract> markerContracts;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homescreen);
        System.out.println("On create homescrewn");

        /* Initialize UI components */
        initUI();
    }


    /**
     * Initialize UI components
     */
    private void initUI() {
        /* Tab Bar */
        tabSetup();

        /* Seek bar */
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBarListener = new IRSeekBarListener(circle);
        seekBar.setOnSeekBarChangeListener(seekBarListener);
        seekBar.setProgress(100 * (CIRCLE_DEFAULT_RADIUS - CIRCLE_MIN_RADIUS) / (CIRCLE_MAX_RADIUS - CIRCLE_MIN_RADIUS));

        /* Obtain the SupportMapFragment and get notified when the map is ready to be used. */
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Initiatlize data from the server on the UI
     */
    private void initData() {
        System.out.println("Getting all the data from server");

        /* Init the hashmap Marker - Contracts */
        markerContracts = new HashMap<Marker, Contract>();

        /* Add all the contracts on the google map and the hash map*/
        LinkedList<Contract> contracts = ContractManager.getAllContracts();
        for (Contract contract : contracts) {
            LatLng location = new LatLng(contract.latitude, contract.longitude);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(location).title(contract.valueEUR + ""));

            markerContracts.put(marker, contract);
        }

        /* Set on click listener for each pin */
        mMap.setOnMarkerClickListener(
                new GoogleMap.OnMarkerClickListener() {

                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        Contract contract = markerContracts.get(marker);


                        if (contract != null) {
                            /* Start a separate view for a contract */
                            Intent intent = new Intent(getBaseContext(), ContractActivity.class);
                            intent.putExtra(ContractActivity.EXTRA_CONTRACT_ID, contract);
                            startActivity(intent);
                        } else {
                            /* Offer details about the user's position */
                            Toast.makeText(getBaseContext(),
                                    "Asta e pozitia ta. Modifica aria de interes si vezi statistici " +
                                            "despre contractele din jurul tau",
                                    Toast.LENGTH_LONG).show();
                        }

                        return true;
                    }
                }
        );
    }


    /**
     * Create primary tab view
     * @param context
     * @param tabText
     * @return
     */
    private View createTabView(Context context, String tabText) {
        View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
        TextView tv = (TextView) view.findViewById(R.id.tabsText);
        tv.setText(tabText);
        return view;
    }


    /* Setup tab navigation bar */
    private void tabSetup() {
        int viewId;
        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);

        tabHost.setup();

        View tabView = createTabView(this, TAB_MAP);
        TabHost.TabSpec spec = tabHost.newTabSpec("tab1").setIndicator(tabView)
                .setContent(R.id.tabMap);
        tabHost.addTab(spec);

        tabView = createTabView(this, TAB_STATISTICS);
        spec = tabHost.newTabSpec("tab2").setIndicator(tabView)
                .setContent(R.id.tabStatistics);
        tabHost.addTab(spec);
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        System.out.println("Map ready");

        mMap = googleMap;

        // Add a marker in Bucharest and move the camera
        LatLng bucharest = new LatLng(44.435503, 26.102513);
        circle = mMap.addCircle(new CircleOptions().center(bucharest)
                .radius(CIRCLE_DEFAULT_RADIUS)
                .fillColor(Color.argb(CIRCLE_ALPHA, DEFAULT_COLOR_RED, DEFAULT_COLOR_GREEN, DEFAULT_COLOR_BLUE))
                .strokeColor(Color.rgb(DEFAULT_COLOR_RED, DEFAULT_COLOR_GREEN, DEFAULT_COLOR_BLUE)).strokeWidth(CIRCLE_MARGIN));
        seekBarListener.setCircle(circle);

        currentPos = mMap.addMarker(new MarkerOptions().position(bucharest).title("Locatia ta"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(bucharest));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(bucharest, MAP_DEFAULT_ZOOM));

        /* Initialize GPS location */
        this.locationListener = IRLocationListener.getLocationManager(this);

        /* Init data from server */
        initData();
    }


    /**
     * Permission handler for location
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case IRLocationListener.IR_PERMISSION_ACCESS_COURSE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("Permissions granted");
                    locationListener.setupLocation();

                } else {
                    System.out.println("Permissions were not granted");
                }
                return;
            }
        }
    }


    /**
     * Set the view point on the current location
     * @param location
     */
    public void setInitialPosition(Location location) {

        if (location == null)
            return;

        updateLocationComponents(location);

        // Zoom to the current location
        LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, MAP_DEFAULT_ZOOM));
    }

    /**
     * Update the map with the current GPS location
     * @param location
     */
    public void updateLocationComponents(Location location) {

        if (location == null)
            return;

        System.out.println("Location update lat " + location.getLatitude() + " long " + location.getLongitude());

        // Move the circle and the pin to the current location
        LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
        this.circle.setCenter(current);
        this.currentPos.setPosition(current);
    }
}