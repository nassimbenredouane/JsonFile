package corp.nassim.jsonfile;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

import static android.widget.Toast.LENGTH_LONG;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    HashMap<String, String> parking;
    LocationManager lManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        parking = (HashMap<java.lang.String, java.lang.String>) intent.getSerializableExtra("parking");


        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        LocationManager lManager = (LocationManager) getSystemService((Context.LOCATION_SERVICE));

                mMap = googleMap;
        LatLng point = new LatLng(Double.parseDouble(parking.get("lat")), Double.parseDouble(parking.get("lon")));
        mMap.setMinZoomPreference(15);
        mMap.addMarker(new MarkerOptions().position(point).title(parking.get("libelle")));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(point));

    }
}
