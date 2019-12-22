package com.capston.hari;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import static android.provider.SettingsSlicesContract.KEY_LOCATION;

public class GPS extends AppCompatActivity implements OnMapReadyCallback, LocationListener {
    private GoogleMap googleMap;
    double latitude;
    double longitude;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    ListView gpslistview;
    GPSDataListAdapter gpsDataListAdapter;
    EditText editText;
    Button search;

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(latitude, longitude), 10));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gps_page);

        editText = (EditText)findViewById(R.id.EditText_search);
        gpsDataListAdapter = new GPSDataListAdapter();
        gpslistview = (ListView)findViewById(R.id.ListView_gpslist);
        gpslistview.setAdapter(gpsDataListAdapter);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
            }
        });

        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (editText.getText().toString().isEmpty()) {
                    search.setEnabled(false);
                } else {
                    search.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        search = (Button) findViewById(R.id.Button_gpsSearch);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeviceLocation();

                if (!editText.getText().toString().isEmpty()) {
                    gpsDataListAdapter.clear();

                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                String url_text = "https://dapi.kakao.com/v2/local/search/keyword.xml?y=" + latitude + "&x=" + longitude + "&radius=20000&query=" + editText.getText().toString();
                                String buf, Text = null;
                                String apikey = "b48b862175b2eaba31b9e39fc734035f"; //apikey
                                List<String> data = new ArrayList<String>();

                                URL url = new URL(url_text);
                                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                                String auth = "KakaoAK " + apikey;
                                conn.setRequestMethod("GET");
                                conn.setRequestProperty("X-Requested-With", "curl");
                                conn.setRequestProperty("Authorization", auth);

                                BufferedReader br = new BufferedReader(new InputStreamReader(
                                        conn.getInputStream(), "UTF-8"));
                                while ((buf = br.readLine()) != null) {
                                    Text = buf;
                                }

                                XPath xpath = XPathFactory.newInstance().newXPath();
                                InputSource is = new InputSource(new StringReader(Text));
                                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
                                String expressions = "//*/documents";
                                NodeList cols = (NodeList) xpath.compile(expressions).evaluate(document, XPathConstants.NODESET);

                                for (int idx = 0; idx < cols.getLength(); idx++) {
                                    XPathExpression expression1 = xpath.compile("place_name");
                                    Node node1 = (Node) expression1.evaluate(cols.item(idx), XPathConstants.NODE);
                                    XPathExpression expression2 = xpath.compile("road_address_name");
                                    Node node2 = (Node) expression2.evaluate(cols.item(idx), XPathConstants.NODE);
                                    XPathExpression expression3 = xpath.compile("phone");
                                    Node node3 = (Node) expression3.evaluate(cols.item(idx), XPathConstants.NODE);
                                    XPathExpression expression4 = xpath.compile("x");
                                    Node node4 = (Node) expression4.evaluate(cols.item(idx), XPathConstants.NODE);
                                    XPathExpression expression5 = xpath.compile("y");
                                    Node node5 = (Node) expression5.evaluate(cols.item(idx), XPathConstants.NODE);

                                    gpsDataListAdapter.addItem(node1.getTextContent(), node2.getTextContent(), node3.getTextContent(), Double.parseDouble(node4.getTextContent()), Double.parseDouble(node5.getTextContent()));
                                }
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };

                    thread.start();
                    try {
                        thread.join();
                    } catch (Exception e) {}

                    gpsDataListAdapter.notifyDataSetChanged();
                }
            }
        });


        gpslistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                googleMap.clear();
                LatLng target = new LatLng(((FindGPSData)gpsDataListAdapter.getItem(position)).getY(), ((FindGPSData)gpsDataListAdapter.getItem(position)).getX());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(target);
                markerOptions.title(((FindGPSData)gpsDataListAdapter.getItem(position)).getPlace_name());
                markerOptions.snippet("목표 장소");
                googleMap.addMarker(markerOptions);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (googleMap != null) {
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        getDeviceLocation();
    }

    private void getDeviceLocation() {
        getLocationPermission();

        if (mLocationPermissionGranted) {
            Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        mLastKnownLocation = task.getResult();

                        try {
                            latitude = mLastKnownLocation.getLatitude();
                            longitude = mLastKnownLocation.getLongitude();
                        } catch (Exception e) {
                            latitude = (double) 37;
                            longitude = (double) 126;
                        }
                        /*
                        LatLng SEOUL = new LatLng(latitude, longitude);
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(SEOUL);
                        markerOptions.title("서울");
                        markerOptions.snippet("한국의 수도");
                        googleMap.addMarker(markerOptions);
                        */
                    } else {

                    }
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
                    googleMap.setMyLocationEnabled(true);
                }
            });
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        }
    }
}