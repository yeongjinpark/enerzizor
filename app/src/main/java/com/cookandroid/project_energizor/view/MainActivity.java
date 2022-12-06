package com.cookandroid.project_energizor.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;


import java.util.ArrayList;
import java.util.List;


import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cookandroid.project_energizor.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.cookandroid.project_energizor.user_location;
import com.google.firebase.database.ValueEventListener;
import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser;
import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.IBeacon;


public class MainActivity extends AppCompatActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener {

    private GoogleMap map;
    PolylineOptions polylineoptions;
    Button btn_search;
    EditText editText;
    private FusedLocationProviderClient fusedLocationClient;
    private Geocoder geocoder;

    LatLng latlng_array[] = {new LatLng(36.142278, 128.393955),
            new LatLng(36.136696, 128.398629),
            new LatLng(36.135953, 128.406375),
            new LatLng(36.137371, 128.414080),
            new LatLng(36.137371, 128.414080),
            new LatLng(36.136222, 128.422051),
            new LatLng(36.136987, 128.428747),
            new LatLng(36.138515, 128.437377),
            new LatLng(36.138515, 128.437377),
            new LatLng(36.138497, 128.439095),
            new LatLng(36.140689, 128.439062),
            new LatLng(36.140710, 128.442272)
    };

    //BLE
    private ImageView iv_Noti;
    private ImageView iv_Noti_f;
    private BluetoothAdapter bluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1000;
    private static final long SCAN_PERIOD = 10000;
    public String SERVICE_STRING = "ffd15047-ef43-40bd-9d5f-18d758e79b7c";
    MediaPlayer mediaPlayer;

    //BLE_SCAN
    private Handler handler;
    private boolean mScanning;
    ////-> UUID
    BluetoothLeScanner scanner;
    boolean leScanning;


    private TextView BeaconUUID;
    ArrayList<BluetoothDevice> arraylist;
    ArrayList<String> arrUUID;


    @SuppressLint("MissingInflatedId")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BeaconUUID = (TextView) findViewById(R.id.beaconUUID);
        arraylist = new ArrayList<>();
        arrUUID = new ArrayList<>();
        arrUUID.add("ffd15047-ef43-40bd-9d5f-18d758e79b7c");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        init();
        bleInit();
        bleSupportCheck();

        geocoder = new Geocoder(getApplicationContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        editText = (EditText) findViewById(R.id.editText);
        btn_search = (Button) findViewById(R.id.btn_search);
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAddress();

            }
        });

        //지도 API를 사용하기위해 객체 선언 하는 부분
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) { //지도를 사용할 준비가 되었고, 지도API를 사용하는곳
                Log.i("MyLocTest", "지도 준비됨");
                map = googleMap;

                getNowLocation();

                map.addPolyline(polylineoptions.color(Color.BLUE));

                MarkerOptions makerOptions = new MarkerOptions();
                makerOptions
                        .position(new LatLng(36.140710, 128.442272))
                        .title("구미코"); // 타이틀.
                map.addMarker(makerOptions);

                MarkerOptions makerOptions1 = new MarkerOptions();
                makerOptions1
                        .position(new LatLng(36.142278, 128.393955))
                        .title("금오공대"); // 타이틀.
                map.addMarker(makerOptions1);

                map.setMyLocationEnabled(true);
                onMyLocationButtonClick();
            }
        });
        scanLeDevice(true);

        //파이어베이스에서 실시간 데이터호출
        DatabaseReference mRootDatabaseReference = FirebaseDatabase.getInstance().getReference();
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location loc_Current = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        Double cur_lat = loc_Current.getLatitude(); //위도
        Double cur_lon = loc_Current.getLongitude(); //경도

        mRootDatabaseReference.child("DataSet").addValueEventListener(new ValueEventListener() {
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot child: postSnapshot.getChildren()){
                        if(child.child("Scan").getValue() != null) {
                            user_location user = child.child("Scan").getValue(user_location.class);
                            String laitu = String.valueOf(user.latitude);
                            String longit = String.valueOf(user.longitude);
                            Log.w("db위도", laitu);
                            Log.w("db경도", longit);

                            if (cur_lat - 0.01 < user.latitude && user.latitude < cur_lat + 0.01) {
                                if (cur_lon - 0.01 < user.longitude && user.longitude < cur_lon + 0.01) {

                                    iv_Noti_f.setVisibility(View.VISIBLE);
                                    MarkerOptions makerOptions3 = new MarkerOptions();
                                    makerOptions3
                                            .position(new LatLng(user.latitude, user.longitude))
                                            .title("배회자 위치"); // 타이틀.

                                    makerOptions3.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                                    map.addMarker(makerOptions3);
                                    mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.warning);
                                    mediaPlayer.start();
                                    return;
                                } else {
                                    iv_Noti_f.setVisibility(View.INVISIBLE);
                                }
                            } else {
                                iv_Noti_f.setVisibility(View.INVISIBLE);
                            }
                        }

                    }
//                    user_location user = postSnapshot.getValue(user_location.class);
//                    String laitu = String.valueOf(user.latitude);
//                    String longit = String.valueOf(user.longitude);
//
//
//                    if(loc_Current.getLatitude() - 0.01 <user.latitude && user.latitude <loc_Current.getLatitude()+0.01){
//                        if(loc_Current.getLongitude() - 0.01<user.longitude && user.longitude<loc_Current.getLongitude()+0.01){
//                            Log.w("성공", laitu);
//                            iv_Noti_f.setVisibility(View.VISIBLE);
//                        } else{iv_Noti_f.setVisibility(View.INVISIBLE);}
//                    }else{iv_Noti_f.setVisibility(View.INVISIBLE);}
//
//                    MarkerOptions makerOptions3 = new MarkerOptions();
//                    makerOptions3
//                            .position(new LatLng(user.latitude, user.longitude))
//                            .title("배회자 위치"); // 타이틀.
//
//                    makerOptions3.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
//                    map.addMarker(makerOptions3);
//
//                    Log.w("FireBaseSuccess", laitu);
//                    Log.w("FireBaseSuccess", longit);
                }
            }


            @Override
            public void onCancelled (@NonNull DatabaseError databaseError){
                // Getting Post failed, log a message
                Log.w("FireBaseData", "loadPost:onCancelled", databaseError.toException());
            }

        });

    }

    private void init() {
        iv_Noti = findViewById(R.id.iv_Noti);
        iv_Noti_f = findViewById(R.id.iv_Noti_fire);

        handler = new Handler();
        mScanning = true;


        polylineoptions=new PolylineOptions();
        for(LatLng l: latlng_array)
            polylineoptions.add(l);

    }
    //현재위치를 좌표로 받아오는 함수
    @SuppressLint("MissingPermission")
    private void getNowLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 18));
                            String laitu = String.valueOf(location.getLatitude());
                            String longit = String.valueOf(location.getLongitude());
                            Log.w("lat_long", laitu);
                            Log.w("lat_long", longit);
                        }
                    }
                });
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(getApplicationContext(), "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(getApplicationContext(), "Current location:\n" + location, Toast.LENGTH_LONG).show();

    }

    public void getAddress() {
        List<Address> addressList = null;
        String str = editText.getText().toString();

        // editText에 입력한 텍스트(주소, 지역, 장소 등)을 지오 코딩을 이용해 변환
        try {
            addressList = geocoder.getFromLocationName(
                    str, // 주소
                    10); // 최대 검색 결과 개수
            if (addressList.size() == 0) {   //edittext를 잘 못 입력한 경우
                Toast.makeText(getApplicationContext(), "다시 입력하세요", Toast.LENGTH_SHORT).show();  //오류 발생 토스트 알림
                throw new Exception();                                                         // 예외처리로 이동
            } else {
                System.out.println(addressList.get(0).toString());
                // 콤마를 기준으로 split
                String[] splitStr = addressList.get(0).toString().split(",");
                String address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1, splitStr[0].length() - 2); // 주소
                System.out.println(address);

                String latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // 위도
                String longitude = splitStr[12].substring(splitStr[12].indexOf("=") + 1); // 경도
                System.out.println(latitude);
                System.out.println(longitude);

                // 좌표(위도, 경도) 생성
                LatLng point = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                // 마커 생성
                MarkerOptions mOptions2 = new MarkerOptions();
                mOptions2.title("search result");
                mOptions2.snippet(address);
                mOptions2.position(point);
                // 마커 추가
                map.addMarker(mOptions2);
                // 해당 좌표로 화면 줌
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 15));
            }
        } catch (Exception e) {  //오류발생시 예외처리
            System.out.println(e);
        }
    }

    //블루투스
    //BLE SupportCheck
    private void bleSupportCheck() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            //Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }



    //BLE_init
    @SuppressLint("MissingPermission")
    private void bleInit() {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        scanner = bluetoothAdapter.getBluetoothLeScanner();
        leScanning = true;
    }

    //BLE_SCAN
    @SuppressLint("MissingPermission")
    private void scanLeDevice(final boolean enable) {
        /*
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @SuppressLint("MissingPermission")
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothAdapter.stopLeScan(leScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            bluetoothAdapter.startLeScan(leScanCallback);
        } else {
            mScanning = false;
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
        bluetoothAdapter.startLeScan(leScanCallback);
         */
        if (mScanning) {
            bluetoothAdapter.startLeScan(leScanCallback);
            mScanning = false;
        } else {
            bluetoothAdapter.stopLeScan(leScanCallback);
            mScanning = true;
        }
    }

    final private BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @SuppressLint("MissingPermission")
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {

                    if (device.getName() != null) {
                        Log.i("BLE_SCAN_UUID", getUUID(scanRecord));  //uuid 출력ㄱ

                        if (!arraylist.contains(device)) {
                            arraylist.add(device);
                        }

                        if (arrUUID.contains(getUUID(scanRecord))) {

                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run () {
                                        Log.i("mac:", device.getAddress().toString());
                                        Log.i("BLE_SCAN_UUID", getUUID(scanRecord).toString());  //uuid 출력ㄱ
                                        iv_Noti.setVisibility(View.VISIBLE);
                                        mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.alarm);
                                        mediaPlayer.start();
                                    }
                                });
                        }else{
                            runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        iv_Noti.setVisibility(View.INVISIBLE);
                                        mediaPlayer.stop();
                                    }
                                });
                        }
                    }

//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            String uuid = getUUID(scanRecord);
//                            if (uuid != null) {
//                                if(device.getAddress().equals("AC:23:3F:32:D4:53")){
//                                    Log.i("mac:",device.getAddress().toString());
//                                }
//
//
//                                handler.postDelayed(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        runOnUiThread(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                if(getUUID(scanRecord).equals(SERVICE_STRING)) {
//                                                    Log.i("mac:", device.getAddress().toString());
//                                                    Log.i("BLE_SCAN_UUID", getUUID(scanRecord).toString());  //uuid 출력ㄱ
//                                                    iv_Noti.setVisibility(View.VISIBLE);
//                                                }
//                                            }
//                                        });
//                                    }
//                                }, 3000);
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        iv_Noti.setVisibility(View.INVISIBLE);
//                                    }
//                                });
//                            }
//
//                        }
//                    });
                }
            };

    private String getUUID(byte[] result) {
        List<ADStructure> structures =
                ADPayloadParser.getInstance().parse(result);

        for (ADStructure structure : structures) {
            if (structure instanceof IBeacon) {
                IBeacon iBeacon = (IBeacon) structure;
                return iBeacon.getUUID().toString();
            }
        }
        return "";
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onDestroy() {
        super.onDestroy();
        //stop scan
        bluetoothAdapter.stopLeScan(leScanCallback);
    }

}