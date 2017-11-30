package mymap.miki.inc.com.mymap.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import mymap.miki.inc.com.mymap.Utils.Place;
import mymap.miki.inc.com.mymap.Utils.PlacesService;
import mymap.miki.inc.com.mymap.R;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, AdapterView.OnItemSelectedListener {

    String API_KEY;

    private GoogleMap mMap;
    boolean mapReady = false;
    @BindView(R.id.btnMap)
    ImageButton btnMap;
    @BindView(R.id.btnSatellite)
    ImageButton btnSatellite;
    @BindView(R.id.btnHybrid)
    ImageButton btnHybrid;
    @BindView(R.id.places_spinner)
    Spinner places_spinner;


    private String[] places;
    private CameraPosition city;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ButterKnife.bind(this);


        API_KEY = getResources().getString(R.string.google_maps_key);

        places = getResources().getStringArray(R.array.places_values);

        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        city = getCity();


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.places, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        places_spinner.setAdapter(adapter);
        places_spinner.setOnItemSelectedListener(this);

        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapReady)
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            }
        });

        btnSatellite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapReady)
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            }
        });

        btnHybrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapReady)
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }
        });

        // Obtain the SupportMapFragment and get notified when the mapsatellite is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    //When got back from settings fetch city
    @Override
    protected void onResume() {
        super.onResume();
        city = getCity();
        if (mapReady) {
            mMap.clear();
            flyTo(city);
            new GetPlaces(MapsActivity.this,
                    places[0].toLowerCase().replace("-", "_")
            ).execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        // return true so that the menu pop up is opened
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.choose_city_item:
                Intent intent = new Intent(MapsActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Manipulates the mapsatellite once available.
     * This callback is triggered when the mapsatellite is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapReady = true;
        mMap = googleMap;
        flyTo(city);

    }

    private void flyTo(CameraPosition city) {
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(city), 1500, null);
    }

    //On spinner item selected
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (mapReady) {
            mMap.clear();
            new GetPlaces(MapsActivity.this,
                    places[pos].toLowerCase().replace("-", "_")
            ).execute();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Toast.makeText(this, getResources().getString(R.string.warning), Toast.LENGTH_SHORT).show();
    }

    //Async task for fetching places
    private class GetPlaces extends AsyncTask<Void, Void, ArrayList<Place>> {

        private Context context;
        private String places;

        public GetPlaces(Context context, String places) {
            this.context = context;
            this.places = places;
        }

        @Override
        protected ArrayList<Place> doInBackground(Void... arg0) {
            try {
                PlacesService service = new PlacesService(API_KEY);

                String pref_key = getResources().getString(R.string.city);
                SharedPreferences prefs = getApplicationContext().getSharedPreferences(pref_key, Context.MODE_PRIVATE);


                String value = prefs.getString(pref_key, null);
                double latitude = 0;
                double longitude = 0;

                if (value != null) {
                    String[] split = value.split(",");
                    latitude = Double.parseDouble(split[0]);
                    longitude = Double.parseDouble(split[1]);
                }
                ArrayList<Place> findPlaces = service.findPlaces(latitude, longitude, places);

                return findPlaces;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Place> result) {
            super.onPostExecute(result);
            if (result != null) {
                for (int i = 0; i < result.size(); i++) {
                    mMap.addMarker(new MarkerOptions()
                            .title(result.get(i).getName())
                            .position(
                                    new LatLng(result.get(i).getLatitude(), result
                                            .get(i).getLongitude()))
                            .icon(BitmapDescriptorFactory
                                    .fromResource(R.drawable.pin))
                            .snippet(result.get(i).getVicinity()));

                }
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(city));
                setInfoAdapter(mMap);
            }
        }
    }

    //get city from preferences
    public CameraPosition getCity() {
        String pref_key = getResources().getString(R.string.city);
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(pref_key, Context.MODE_PRIVATE);


        String value = prefs.getString(pref_key, null);


        if (value != null) {
            String[] split = value.split(",");
            double latitude = Double.parseDouble(split[0]);
            double longitude = Double.parseDouble(split[1]);
            city = CameraPosition.builder()
                    .target(new LatLng(latitude, longitude))
                    .zoom(15)
                    .bearing(0)
                    .tilt(45)
                    .build();

        } else {
            city = CameraPosition.builder()
                    .target(new LatLng(43.50773, 18.77499))
                    .zoom(15)
                    .bearing(0)
                    .tilt(45)
                    .build();
        }

        return city;
    }


    //set up infoView for Marker
    public void setInfoAdapter(GoogleMap googleMap) {
        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(Marker arg0) {
                View v = null;
                try {
                    v = getLayoutInflater().inflate(R.layout.custom_infowindow, null);
                    TextView titleTxt = v.findViewById(R.id.titleTxt);
                    TextView descTxt = v.findViewById(R.id.descTxt);
                    titleTxt.setText(arg0.getTitle());
                    descTxt.setText(arg0.getSnippet());

                } catch (Exception ev) {
                    System.out.print(ev.getMessage());
                }

                return v;
            }
        });
    }
}
