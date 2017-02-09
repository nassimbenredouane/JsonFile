package corp.nassim.jsonfile;

import android.*;
import android.content.Intent;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ArrayList<HashMap<String, String>> parkingList = new ArrayList<>();
    private LocationManager locationManager = null;
    private Location pos = null;
    private LatLng position = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager)getSystemService(getApplicationContext().LOCATION_SERVICE);
        boolean gps_enabled = false;

        ArrayList<LocationProvider> providers = new ArrayList<LocationProvider>();


        List<String> names = locationManager.getProviders(true);
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled) {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
            for(String name : names)
        {
            providers.add(locationManager.getProvider(name));
        }

        if(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == 0)
        {
            pos = locationManager.getLastKnownLocation(providers.get(0).getName());
        }

        String lat;
        String lon;

        if (pos != null)
        {
            position = new LatLng(pos.getLatitude(), pos.getLongitude());
            lat = String.valueOf(pos.getLatitude());
            lon = String.valueOf(pos.getLongitude());
        }else{
            lat = "50.333";
            lon = "3.3333";
        }
        Task t = new Task();
        t.execute(lat, lon);

    }

    Comparator<HashMap<String, String>> distanceComparator = new Comparator<HashMap<String, String>>() {

        @Override
        public int compare(HashMap<String, String> o1, HashMap<String, String> o2) {

            Integer distance1 = Integer.parseInt(o1.get("distancevaleur"));
            Integer distance2 = Integer.parseInt(o2.get("distancevaleur"));

            return distance1.compareTo(distance2);
        }
    };

    public class Task extends AsyncTask<String, Void, JSONObject> {

        HttpURLConnection con = null;
        InputStream is = null;
        String url = "https://opendata.lillemetropole.fr/api/records/1.0/search/?dataset=disponibilite-parkings&rows=24&facet=libelle&facet=ville&facet=etat&refine.etat=OUVERT&exclude.dispo=0&timezone=Europe%2FParis";
        JSONObject jsonObj = null;
        String chaine = "";

        HttpURLConnection con2 = null;
        InputStream is2 = null;
        String urldebut = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=";
        String destination = "&destinations=";

        String urlfin = "&language=fr-FR&key=AIzaSyAbI2e4CCWj9wHooM6pc-9WNdiHLDSLc3I";
        String listcoord="";
        JSONObject jsonObj2 = null;
        String chaine2 = "";

        @Override
        protected JSONObject doInBackground(String... params) {
            try {
                con = (HttpURLConnection) (new URL(url)).openConnection();
                con.setRequestMethod("GET");
                con.setDoInput(true);
                con.setDoOutput(false);
                con.connect();

                // Let's read the response

                //StringBuffer buffer = new StringBuffer();
                is = con.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String ligne = null;
                while ((ligne = br.readLine()) != null) {
                    chaine += (ligne + "\r\n");
                }


                jsonObj = new JSONObject(chaine);

                JSONArray parking = jsonObj.getJSONArray("records");
                for(int i=0; i<parking.length(); i++) {
                    JSONObject p = parking.getJSONObject(i);
                    String etat;
                    String dispo;
                    String datemaj;

                    try {
                        etat = p.getJSONObject("fields").getString("etat");
                    } catch (Throwable t) {
                        t.printStackTrace();
                        etat = "Non Disponible";
                    }

                    try {
                        dispo = "Place(s) disponible(s): " + p.getJSONObject("fields").getString("dispo");
                    } catch (Throwable t) {
                        t.printStackTrace();
                        dispo = "Information non disponible";
                    }

                    if (p.getJSONObject("fields").getString("ville").equals("Roubaix")) {
                        datemaj = p.getString("record_timestamp");
                    } else {
                        datemaj = p.getJSONObject("fields").getString("datemaj");
                    }

                    String ville = p.getJSONObject("fields").getString("ville");
                    String coordgeo = p.getJSONObject("fields").getString("coordgeo");
                    String adresse = p.getJSONObject("fields").getString("adresse");
                    String max = p.getJSONObject("fields").getString("max");
                    String lat = p.getJSONObject("fields").getJSONArray("coordgeo").getString(0);
                    String lon =  p.getJSONObject("fields").getJSONArray("coordgeo").getString(1);
                    String libelle = p.getJSONObject("fields").getString("libelle");



                    HashMap<String, String> pList = new HashMap<>();

                    pList.put("etat", etat);
                    pList.put("ville", ville);
                    pList.put("coordgeo", coordgeo);
                    pList.put("dispo", dispo);
                    pList.put("adresse", adresse);
                    pList.put("datemaj", datemaj);
                    pList.put("max", max);
                    pList.put("lat", lat);
                    pList.put("lon", lon);
                    pList.put("libelle", libelle);

                    parkingList.add(pList);
                }

                is.close();
                con.disconnect();

                try{
                    for(int i=0; i<parkingList.size(); i++){
                        listcoord += parkingList.get(i).get("lat")+","+parkingList.get(i).get("lon")+"|";
                    }
                    System.out.println(urldebut+params[0]+","+params[1]+destination+listcoord+urlfin);
                    con2 = (HttpURLConnection) (new URL(urldebut+params[0]+","+params[1]+destination+listcoord+urlfin)).openConnection();

                    con2.setRequestMethod("GET");
                    con2.setDoInput(true);
                    con2.setDoOutput(false);
                    con2.connect();

                    is2 = con2.getInputStream();
                    BufferedReader br2 = new BufferedReader(new InputStreamReader(is2));
                    String ligne2 = null;
                    System.out.println(ligne2);
                    while ((ligne2 = br2.readLine()) != null) {
                        chaine2 += (ligne2 + "\r\n");
                    }

                    jsonObj2 = new JSONObject(chaine2);
                    JSONObject rows = jsonObj2.getJSONArray("rows").getJSONObject(0);
                    JSONArray elements = rows.getJSONArray("elements");

                    for(int i=0; i<elements.length(); i++){

                        parkingList.get(i).put("distance", elements.getJSONObject(i).getJSONObject("distance").getString("text"));
                        parkingList.get(i).put("distancevaleur", elements.getJSONObject(i).getJSONObject("distance").getString("value"));
                        parkingList.get(i).put("duree", elements.getJSONObject(i).getJSONObject("duration").getString("text"));
                    }

                    Collections.sort(parkingList, distanceComparator);


                    is2.close();
                    con2.disconnect();


                }catch(Throwable t){}

                return jsonObj;
            } catch (Throwable t) {
                t.printStackTrace();
                return null;
            } finally {
                try {
                    is.close();
                } catch (Throwable t) {
                }
                try {
                    con.disconnect();
                } catch (Throwable t) {
                }
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            TextView tv =(TextView)findViewById(R.id.datemaj);

            String heure = parkingList.get(0).get("datemaj").toString();
            String[] dateheure = new  String[2];
            String[] heureheure = new String[2];

            int i=0;
            for (String retval: heure.split("T")) {
                dateheure[i]=retval;
                i++;
            }
            i=0;
            for(String retval: dateheure[1].split("\\+")){
                heureheure[i] = retval;
                i++;
            }

            tv.setText("Dernière MàJ "+heureheure[0]);


            ListView lv = (ListView) findViewById(R.id.adressePark);
            ListAdapter adapter = new SimpleAdapter(MainActivity.this, parkingList, R.layout.list_item, new String[]{"ville", "adresse", "dispo", "distance"}, new int[]{R.id.ville, R.id.adresse, R.id.dispo, R.id.distance});

            lv.setAdapter(adapter);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                    intent.putExtra("parking", parkingList.get(position));

                    startActivity(intent);

                }
            });
        }

    }
}
