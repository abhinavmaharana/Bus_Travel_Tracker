package com.example.bus_travel_tracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnPolylineClickListener,
        GoogleMap.OnPolygonClickListener {

    //Initialisation of variables

    private static final String TAG = "MapsActivity" ;
    private GoogleMap mMap;
    private final double CHENNAI_LAT= 12.823080;
    private final double CHENNAI_LNG= 80.041004;
    public static final int DEFAULT_ZOOM = 15;
    private static final PatternItem DOT = new Dot();
    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);
    private FirebaseFirestore mBusLocation;
    private Polyline polyline;
    boolean locationPermission = false;
    Location myLocation = null;
    Location myUpdatedLocation = null;
    float Bearing = 0;
    boolean AnimationStatus = false;
    static Marker carMarker;
    Bitmap BitMapMarker;
    private final static int LOCATION_REQUEST_CODE = 23;

    ListView listView;
    String mTitle[] = {"Bus Stop 1", "Bus Stop 2", "Bus Stop 3", "Bus Stop 4", "Bus Stop 5"};
    String mDescription[] = {"Java Bus Stop","Hospital Bus Stop", "Opp.Hospital Bus Stop", "Opp.Java Bus Stop", "MainCampus Bus Stop"};
    int images[] = {R.drawable.bus1, R.drawable.bus2, R.drawable.bus3, R.drawable.bus4, R.drawable.bus5};

    //Creation of multiple marker points for various bus stops

    ArrayList<LatLng> arrayList= new ArrayList<LatLng>();
    LatLng buspoint1 = new LatLng(12.822993, 80.044391);
    LatLng buspoint2 = new LatLng(12.823076, 80.046685);
    LatLng buspoint3 = new LatLng(12.822867, 80.046696);
    LatLng buspoint4 = new LatLng(12.822853, 80.044310);
    LatLng buspoint5 = new LatLng(12.821124, 80.037832);
    String s1[], s2[];
    private double lat,lng;
    ImageView backButton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        requestPermision();
        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.car_marker);
        Bitmap b = bitmapdraw.getBitmap();
        BitMapMarker = Bitmap.createScaledBitmap(b, 110, 60, false);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //backbutton to go back to first page.
        backButton2 = findViewById(R.id.backBtn2);

        backButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //for Restricting google map to certain area
        //Restricting to a specific location
        if (mMap != null) {

            double bottomBoundry=12.820901;
            double leftBoundry= 80.037417;
            double topBoundry=12.825023;
            double rightBoundry=80.047963;

            LatLngBounds CHENNAI_BOUNDS=new LatLngBounds(
                    new LatLng(bottomBoundry,leftBoundry),
                    new LatLng(topBoundry,rightBoundry)
            );

            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(CHENNAI_BOUNDS,1));
        }
        arrayList.add(buspoint1);
        arrayList.add(buspoint2);
        arrayList.add(buspoint3);
        arrayList.add(buspoint4);
        arrayList.add(buspoint5);

        //For user permission for location
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);

        //Creating instance for storing data of bus in firebase
        mBusLocation = FirebaseFirestore.getInstance();
        Map<String, Object> bus = new HashMap<>();
        bus.put("Bus name", "Bus A1");
        bus.put("Route ", "Reaching Java Bus Stop");
        bus.put("Time", new Timestamp(new Date()));
        listView = findViewById(R.id.listView);

        //Collecting point of the bus  to store in firebase
        ArrayList<Object> busLocation = new ArrayList<>();
        final LatLng busA1 = new LatLng(12.822989, 80.043880);
        Collections.addAll(busLocation,busA1);
        bus.put("busLocation",busLocation);

        //this will check the condition if user give the permission of the location then it will store the required data in firebase
        mBusLocation.collection("BusRoutes").document("BusStatus")
                .set(bus)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        mMap.addMarker(new MarkerOptions().position(busA1).title("BusA1").icon(bitmapDescriptorFromVector(getApplicationContext(),R.drawable.ic_directions_bike_black_24dp)));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(busA1));

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });

        // created an adapter class
        MyAdapter adapter = new MyAdapter(this, mTitle, mDescription, images);
        listView.setAdapter(adapter);

        listView.setItemsCanFocus(true);
        // now set item click on list view
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position ==  0) {
                    Toast.makeText(MapsActivity.this, "Java BusStop", Toast.LENGTH_SHORT).show();
                }
                if (position ==  1) {
                    Toast.makeText(MapsActivity.this, "Hospital BusStop", Toast.LENGTH_SHORT).show();
                }
                if (position ==  2) {
                    Toast.makeText(MapsActivity.this, "Opp.Hospital BusStop", Toast.LENGTH_SHORT).show();
                }
                if (position ==  3) {
                    Toast.makeText(MapsActivity.this, "Opp.Java BusStop", Toast.LENGTH_SHORT).show();
                }
                if (position ==  4) {
                    Toast.makeText(MapsActivity.this, "MainCampus BusStop", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //to get user location
    private void getMyLocation(double CHENNAI_LAT, double CHENNAI_LNG) {
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {

                if (AnimationStatus) {
                    myUpdatedLocation = location;
                } else {
                    myLocation = location;
                    myUpdatedLocation = location;
                    LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
                    carMarker = mMap.addMarker(new MarkerOptions().position(latlng).
                            flat(true).icon(BitmapDescriptorFactory.fromBitmap(BitMapMarker)));
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                            latlng, 17f);
                    mMap.animateCamera(cameraUpdate);
                }
                Bearing = location.getBearing();
                LatLng updatedLatLng = new LatLng(myUpdatedLocation.getLatitude(), myUpdatedLocation.getLongitude());
                changePositionSmoothly(carMarker, updatedLatLng, Bearing);

            }
        });
    }
    //Asking permission from user
    private void requestPermision() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_REQUEST_CODE);
        } else {
            LocationstatusCheck();
            locationPermission = true;
            //init google map fragment to show map.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LocationstatusCheck();
                    //if permission granted.
                    locationPermission = true;
                    //init google map fragment to show map.
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this);
                    // getMyLocation();

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }



    private void LocationstatusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }
    }
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //getMyLocation(CHENNAI_LAT,CHENNAI_LNG);
        for (int i = 0; i < arrayList.size(); i++) {
            gotoLocation(CHENNAI_LAT, CHENNAI_LNG);
            Polyline polyline1 = googleMap.addPolyline((new PolylineOptions())
                    .clickable(true)
                    .add(new LatLng(CHENNAI_LAT, CHENNAI_LNG),
                            new LatLng(12.823072, 80.041301),
                            new LatLng(12.823022, 80.042625),
                            new LatLng(12.823072, 80.042768),
                            new LatLng(12.823009, 80.042870),
                            new LatLng(12.822996, 80.043734),
                            new LatLng(12.822993, 80.044391),
                            new LatLng(12.822973, 80.044719),
                            new LatLng(12.823054, 80.044834),
                            new LatLng(12.822991, 80.044931),
                            new LatLng(12.823015, 80.045162),
                            new LatLng(12.823042, 80.045500),
                            new LatLng(12.823034, 80.046168),
                            new LatLng(12.823076, 80.046685),
                            new LatLng(12.823031, 80.047249),
                            new LatLng(12.822924, 80.047248),
                            new LatLng(12.822902, 80.044939),
                            new LatLng(12.822831, 80.044834),
                            new LatLng(12.822895, 80.044719),
                            new LatLng(12.822883, 80.043897),
                            new LatLng(12.822880, 80.043253),
                            new LatLng(12.822919, 80.042851),
                            new LatLng(12.822848, 80.042746),
                            new LatLng(12.822926, 80.042632),
                            new LatLng(12.822971, 80.040990),
                            new LatLng(12.822518, 80.040970),
                            new LatLng(12.820991, 80.041274),
                            new LatLng(12.821039, 80.041015),
                            new LatLng(12.821133, 80.040796),
                            new LatLng(12.822042, 80.039380),
                            new LatLng(12.822502, 80.038606),
                            new LatLng(12.821823, 80.038158),
                            new LatLng(12.821269, 80.037800),
                            new LatLng(12.821124, 80.037832)
                    ));


            // Set listeners for click events.
            googleMap.setOnPolylineClickListener(this);
            googleMap.setOnPolygonClickListener(this);

            mMap.addMarker(new MarkerOptions().position(arrayList.get(i)).title("Marker"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(arrayList.get(i)));
        }

    }
    //for smooth movement of the car in map
    void changePositionSmoothly(final Marker myMarker, final LatLng newLatLng, final Float bearing) {

        final LatLng startPosition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        final LatLng finalPosition = newLatLng;
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 3000;
        final boolean hideMarker = false;

        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {
                myMarker.setRotation(bearing);
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                LatLng currentPosition = new LatLng(
                        startPosition.latitude * (1 - t) + finalPosition.latitude * t,
                        startPosition.longitude * (1 - t) + finalPosition.longitude * t);

                myMarker.setPosition(currentPosition);

                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        myMarker.setVisible(false);
                    } else {
                        myMarker.setVisible(true);
                    }
                }
                myLocation.setLatitude(newLatLng.latitude);
                myLocation.setLongitude(newLatLng.longitude);
            }
        });
    }
    //custom marker for bus
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId){
        Drawable vectorDrawable= ContextCompat.getDrawable(context,vectorResId);
        vectorDrawable.setBounds(0,0,vectorDrawable.getIntrinsicWidth(),vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap=Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),vectorDrawable.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    // setting the location to SRM Chennai
    private void gotoLocation(double chennai_lat, double chennai_lng) {
        LatLng latLng=new LatLng(lat,lng);
        CameraUpdate cameraUpdate= CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM);
        showMarker(latLng);

        mMap.moveCamera(cameraUpdate);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

    }

    private void showMarker(LatLng latLng) {
        MarkerOptions markerOptions=new MarkerOptions();
        markerOptions.position(latLng);
        mMap.addMarker(markerOptions);
    }

    //Creating a route in map
    @Override
    public void onPolygonClick(Polygon polygon) {
        if ((polyline.getPattern() == null) || (!polyline.getPattern().contains(DOT))) {
            polyline.setPattern(PATTERN_POLYLINE_DOTTED);
        } else {
            // The default pattern is a solid stroke.
            polyline.setPattern(null);
        }

        Toast.makeText(this, "Route type " + polyline.getTag().toString(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPolylineClick(Polyline polyline) {

    }
}
