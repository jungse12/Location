package edu.msu.jungse12.arewethereyetjungse12;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.security.Policy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.lang.Integer.parseInt;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private LocationManager locationManager = null;

    public ArrayList<ListPlace> list;
    public ArrayList<ListPlace> favoriteList;

    private double latitude = 0;
    private double longitude = 0;
    private boolean valid = false;

    private double toLatitude = 0;
    private double toLongitude = 0;
    private String to = "";

    private char transMode = 'd';

    private SharedPreferences settings = null;

    private final static String TO = "to";
    private final static String TOLAT = "tolat";
    private final static String TOLONG = "tolong";

    private final static String ITEMLIST = "itemlist";

    private ActiveListener activeListener = new ActiveListener();

    public String currentTitle = "";

    public int count;

    Menu mainMenu;
    MenuItem menuList;

    public ArrayList<ListPlace> getFavoriteList() {
        return favoriteList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        to = settings.getString(TO, "2250 Engineering");
        toLatitude = Double.parseDouble(settings.getString(TOLAT, "42.724303"));
        toLongitude = Double.parseDouble(settings.getString(TOLONG, "-84.480507"));
        count = parseInt(settings.getString("count", "0"));


        loadData();
        loadFavorite();

        Spinner spinner = findViewById(R.id.spinnerTransportation);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.transportation, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Also, dont forget to add overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //   int[] grantResults)
                // to handle the case where the user grants the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Force the screen to say on and bright
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    private void loadMenu(Menu menu) {
        if (list.size() != 0) {
            for (int i = 0; i < list.size(); i++) {
                menu.add(0,list.get(i).id,0,list.get(i).getTitle());
                System.out.println("ID: " + list.get(i).getId() + " " + list.get(i).getTitle());
            }
        }
    }

    /**
     * Set all user interface components to the current state
     */
    private void setUI() {
        TextView viewLatitude = (TextView) findViewById(R.id.textLatitude);
        TextView viewLongitude = (TextView) findViewById(R.id.textLongitude);
        TextView viewDistance = (TextView) findViewById(R.id.textDistance);
        float[] distance = new float[1];

        TextView textTo = (TextView) findViewById(R.id.textTo);
        textTo.setText(to);
        if (valid == false) {
            viewLatitude.setText("");
            viewLongitude.setText("");
            viewDistance.setText("");
        } else {
            viewLatitude.setText(String.valueOf(latitude));
            viewLongitude.setText(String.valueOf(longitude));
            Location.distanceBetween(latitude, longitude, toLatitude, toLongitude, distance);
            viewDistance.setText(String.format("%1$6.1fm", distance[0]));
        }
    }

    /**
     * Called when this application becomes foreground again.
     */
    @Override
    protected void onResume() {
        super.onResume();
        TextView viewProvider = (TextView) findViewById(R.id.textProvider);
        viewProvider.setText("");

        setUI();
        registerListeners();
    }

    public void onFavorite(View view) {
        FavSetDlg dlg2 = new FavSetDlg();
        dlg2.show(getSupportFragmentManager(), "save");
    }

    /**
     * Called when this application is no longer the foreground application.
     */
    @Override
    protected void onPause() {
        unregisterListeners();
        super.onPause();
    }

    private void registerListeners() {
        unregisterListeners();

        // Create a Criteria object
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(false);

        String bestAvailable = locationManager.getBestProvider(criteria, true);

        if (bestAvailable != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(bestAvailable, 500, 1, activeListener);
            TextView viewProvider = (TextView)findViewById(R.id.textProvider);
            viewProvider.setText(bestAvailable);
            Location location = locationManager.getLastKnownLocation(bestAvailable);
            onLocation(location);
        }
    }

    private void unregisterListeners() {
        locationManager.removeUpdates(activeListener);
    }
    private void onLocation(Location location) {
        if(location == null) {
            return;
        }

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        valid = true;

        setUI();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        switch (text) {
            case ("Driving"):
                transMode = 'd';
                break;

            case ("Bicycling"):
                transMode = 'b';
                break;

            case ("Walking"):
                transMode = 'w';
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void onRoute(View view) {
        // Create a Uri from an intent string. Use the result to create an Intent.
        Uri gmmIntentUri = Uri.parse("google.navigation:q="+toLatitude + ',' + toLongitude + "&mode=" + transMode);

        // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        // Make the Intent explicit by setting the Google Maps package
        mapIntent.setPackage("com.google.android.apps.maps");

        // Attempt to start an activity that can handle the Intent
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    private class ActiveListener implements LocationListener {


        @Override
        public void onLocationChanged(Location location) {
            onLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {
            registerListeners();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mainMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        loadMenu(menu);
        return true;
    }


    /**
     * Handle an options menu selection
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //menuList = item;
        int id = item.getItemId();
        //System.out.println("SELECTED ID: " + id);
        switch (id) {
            case R.id.itemSparty:
                newTo("Sparty", 42.731138, -84.487508);
                currentTitle = "Sparty";
                return true;

            case R.id.itemHome:
                newTo("2843 Charter Dr. APT 204", 42.5617613, -83.1408383);
                currentTitle = "Home";
                return true;

            case R.id.item2250:
                newTo("2250 Engineering", 42.724303, -84.480507);
                currentTitle = "2250 Engineering";
                return true;

            case R.id.itemDorm:
                newTo("Rather Hall",42.732876, -84.4987464);
                currentTitle = "Dorm";
                return true;

            case R.id.itemPalace:
                newTo("Buckingham Palace", 51.5013673, -0.1440787);
                currentTitle = "Palace";
                return true;

        }

        searchId(id);

        return super.onOptionsItemSelected(item);
    }

    public void searchId(int id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == id) {
                newTo(list.get(i).getTitle(), list.get(i).getLatitude(), list.get(i).getLongitude());
                currentTitle = list.get(i).getTitle();
                break;
            }
        }
    }

    public void onSave(MenuItem item) {
        SaveDlg dlg = new SaveDlg();
        dlg.show(getSupportFragmentManager(), "save");
    }

    public void onFav(MenuItem item) {
        FavDlg dlg = new FavDlg();
        dlg.show(getSupportFragmentManager(), "fav");
    }

    public void saveToDevice(String title) {
        count += 1;
        int id = count;
        ListPlace myObject = new ListPlace(title,toLatitude,toLongitude,id);
        list.add(myObject);
        mainMenu.add(0,id,0,title);

        searchId(id);

        SharedPreferences sharedPreferences = getSharedPreferences("list", MODE_PRIVATE);
        SharedPreferences.Editor editor2 = settings.edit();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor2.putString("count", Integer.toString(count));
        editor.putString("item list", json);
        editor2.commit();
        editor.apply();
    }

    public boolean saveToFavorite() {
        if (currentTitle.equals("")) {
            Toast.makeText(MainActivity.this,"Current Destination is not saved! Save first!", Toast.LENGTH_SHORT).show();
            return false;
        }
        count += 1;
        int id = count;
        ListPlace myObject = new ListPlace(currentTitle,toLatitude,toLongitude,id);
        favoriteList.add(myObject);

        SharedPreferences sharedPreferences = getSharedPreferences("list", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        SharedPreferences.Editor editor2 = settings.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor2.putString("count", Integer.toString(count));
        editor.putString("favorite list", json);
        editor2.commit();
        editor.apply();
        return true;
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("list", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("item list", null);

        Type type = new TypeToken<ArrayList<ListPlace>>() {}.getType();

        list = gson.fromJson(json, type);

        if (list == null) {
            list = new ArrayList<>();
        }
    }

    private void loadFavorite() {
        SharedPreferences sharedPreferences = getSharedPreferences("list", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("favorite list", null);

        Type type = new TypeToken<ArrayList<ListPlace>>() {}.getType();

        favoriteList = gson.fromJson(json, type);

        if (favoriteList == null) {
            favoriteList = new ArrayList<>();
        }
    }

    /**
     * Handle setting a new "to" location.
     * @param address Address to display
     * @param lat latitude
     * @param lon longitude
     */
    public void newTo(String address, double lat, double lon) {
        to = address;
        toLatitude = lat;
        toLongitude = lon;

        SharedPreferences.Editor editor = settings.edit();

        editor.putString(TO,to);
        editor.putString(TOLAT,String.valueOf(toLatitude));
        editor.putString(TOLONG,String.valueOf(toLongitude));
        editor.commit();

        setUI();
    }

    public void onNew(View view) {
        EditText location = (EditText)findViewById(R.id.editLocation);
        final String address = location.getText().toString().trim();
        newAddress(address);
    }

    private void newAddress(final String address) {
        if(address.equals("")) {
            // Don't do anything if the address is blank
            return;
        }
        new Thread(new Runnable() {

            @Override
            public void run() {
                lookupAddress(address);

            }

        }).start();
    }

    /**
     * Look up the provided address. This works in a thread!
     * @param address Address we are looking up
     */
    private void lookupAddress(String address) {
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.US);
        boolean exception = false;
        List<Address> locations;
        try {
            locations = geocoder.getFromLocationName(address, 1);
        } catch(IOException ex) {
            // Failed due to I/O exception
            locations = null;
            exception = true;
        }


        final String temp_address = address;
        final boolean temp_exception = exception;
        final List<Address> temp_location = locations;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                newLocation(temp_address, temp_exception, temp_location);
            }
        });
    }

    private void newLocation(String address, boolean exception, List<Address> locations) {

        if(exception) {
            Toast.makeText(MainActivity.this, R.string.exception, Toast.LENGTH_SHORT).show();
        } else {
            if(locations == null || locations.size() == 0) {
                Toast.makeText(this, R.string.couldnotfind, Toast.LENGTH_SHORT).show();
                return;
            }

            EditText location = (EditText)findViewById(R.id.editLocation);
            location.setText("");

            // We have a valid new location
            Address a = locations.get(0);
            newTo(address, a.getLatitude(), a.getLongitude());

        }
    }

}
