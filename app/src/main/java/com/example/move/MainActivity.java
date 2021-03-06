package com.example.move;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.kongqw.rockerlibrary.view.RockerView;
import com.rabtman.wsmanager.WsManager;
import com.rabtman.wsmanager.listener.WsStatusListener;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.mapsdk.raster.model.BitmapDescriptorFactory;
import com.tencent.mapsdk.raster.model.LatLng;
import com.tencent.mapsdk.raster.model.Marker;
import com.tencent.mapsdk.raster.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.map.MapView;
import com.tencent.tencentmap.mapsdk.map.TencentMap;
import com.tencent.tencentmap.mapsdk.map.UiSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import okio.ByteString;


public class MainActivity extends AppCompatActivity implements TencentLocationListener {
    private double pi = 3.1415926535897932384626;
    public double a = 6378245.0;
    public double ee = 0.00669342162296594323;
    private float clickButtonAlpha = 0.8f;
    private float unClickButtonAlpha = 0.3f;
    private float clickHeadAlpha = 1.0f;
    private float unclickHeadAlpha = 0.3f;
    // ???????????????????????????????????????????????????
    private double latSpan =  0.01369;
    private double lonSpan = 0.01786;
    private DisplayMetrics metrics;
    // ????????????
    MapView mapView = null;
    TencentMap tencentMap = null;
    private Marker marker;
    private boolean isMarker = false;
    private boolean firstLocation = true;
    // ????????????
    TencentLocationManager tencentLocationManager;
    TencentLocationRequest tencentLocationRequest;
    private String appid = "wx19376645db21af08";
    private String openid = "";
    private String gwgo_token = "";

    private Random random;
    // ????????????
    private LocationManager locationManager;
    // ??????????????????????????????
    private Thread thread;
    // ????????????????????????
    private int isRun = 0;
    private int isPatrol = 0;
    private double patrolSpeed = 0.00000005;
    // ??????????????????????????????
    private double longtitude = 0;
    private double latitude = 0;
    // ???????????????????????????
    private double altitude;
    // ???????????????????????????
    private float accuracy;
    // ??????????????????
    private int direct = 0;
    // ??????????????????
    private double baseSpeed = 0.0000002;
    // ????????????
    private double speed = 0.0000002;
    // ???????????????????????????
    private SeekBar speedSeekBar;
    private Button stopButton;
    private Button backButton;
    private Button filterButton;
    private Button mapButton;
    private Button nextButton;
    private Button patrolButton;
    // ????????????
    private Button autoButton;
    private int autoCount = 0;
    // ????????????????????????????????????
    private int isBack = 0;
    private RockerView rockerView;
    private double angle = 0;
    private int count = 1;
    private int loop = 10000;
    private int step = 5000;
    // ???????????????
    private View floatMapView;
    private WindowManager.LayoutParams floatMapViewParams;
    private Button floatMapCloseButton;
    private MapView floatTencentMapView;
    private TencentMap floatTencentMap;

    // ?????????????????????
    private WindowManager windowManager;
    private WindowManager.LayoutParams controllerLayoutParams;
    // ???????????????
    private View controllerView;
    // ???????????????
    private View filterView;
    private WindowManager.LayoutParams filterLayoutParams;
    private LinearLayout headLinearLayout;
    private LayoutParams headLayoutParams;
    // ????????????????????????
    private String[] headImages;
    // ????????????assets??????????????????
    private AssetManager assetManager;
    // ???????????????????????????
    private Map<Integer, Bitmap> headBitmaps = new HashMap<>();
    private Set<Integer> allPetSet = new LinkedHashSet<>();
    private Set<Integer> selectedPetSet = new HashSet<>();
    private Set<String> petSet;
    private String drums;
    private List<Double> drumLocations = new ArrayList<>();
    private int drumIndex = 0;
    private String battlefields;
    private List<Double> battlefieldLocations = new ArrayList<>();
    private int battlefieldIndex = 0;
    private String stones;
    private List<Double> stoneLocations = new ArrayList<>();
    private int stoneIndex = 0;
    private SharedPreferences petSharedPreferences;
    private SharedPreferences.Editor editor;
    // ????????????????????????????????????????????????websocket
    private String wssHost = "wss://publicld.gwgo.qq.com?account_value=0&account_type=1&appid=0&token=0";
    // websocket??????????????????????????????
    private WsManager wsManager;
    // ?????????websocket????????????
    private JSONObject jsonObject;
    // ??????websocket?????????????????????
    private JSONArray jsonArray;
    private JSONArray petJsonArray = new JSONArray();
    // ?????????????????????jsonArray??????????????????
    private int currentIndex = 0;
    // ??????????????????requestId????????????????????????????????????Id????????????????????????requestId????????????????????????????????????
    private long requestId;
    private boolean requestSuccess = false;
    private long checkRequestId;
    private Toast toast;
    // ?????????
    private Button saveButton;
    private EditText openidText;
    private EditText tokenText;
    private int saveStatus = 1;
    // ??????
    private int modType = 0;
    private Button modButton;
    // ?????????
    private ExecutorService fixedThreadPool = new ThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        saveButton = this.findViewById(R.id.saveButton);
        openidText = this.findViewById(R.id.openidText);
        openidText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                openid = openidText.getText().toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        tokenText = this.findViewById(R.id.tokenText);
        tokenText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                gwgo_token = tokenText.getText().toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        init(savedInstanceState);
        continueLocation2();
        showController();
    }

    private void init(Bundle savedInstanceState) {
        initPermission();
        // ?????????????????????
        initPets();
        // ???????????????????????????
        initWindowManager(savedInstanceState);
        initWebsocket();
        initMoveManager();
        initLocation();
        getToken();
    }

    private void initWindowManager(Bundle savedInstanceState) {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);

        // ??????????????????activity??????
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        // ?????????????????????
        controllerView = layoutInflater.inflate(R.layout.activity_controller, null);
        initController();
        // ???????????????
        filterView = layoutInflater.inflate(R.layout.activity_filter, null);
        initFilter();
        showPets();
        // ???????????????
        floatMapView = layoutInflater.inflate(R.layout.activity_map, null);
        initMap(savedInstanceState);
    }

    private void initPermission() {
        // ????????????
        // ??????????????????
        // ???????????????
        // ??????????????????????????????
    }

    // ????????????????????????????????????
    private void initMap(Bundle savedInstanceState) {
        mapView = (MapView) floatMapView.findViewById(R.id.floatmapview);
        mapView.onCreate(savedInstanceState);
        tencentMap = mapView.getMap();
        tencentMap.setZoom(15);
        UiSettings uiSettings = mapView.getUiSettings();
        // ????????????
        // uiSettings.setZoomGesturesEnabled(false);
        // ????????????
        // uiSettings.setScrollGesturesEnabled(false);
        // ????????????marker

        // ??????????????????????????????
        tencentMap.setOnMapClickListener(new TencentMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {
                if (modType == 0) {
                    getPets(latLng.getLatitude(), latLng.getLongitude());
                } else {
                    handleMapClick(latLng.getLatitude(), latLng.getLongitude());
                }
            }
        });
        // ????????????marker???????????????
        tencentMap.setOnMarkerClickListener(new TencentMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LatLng pos =  marker.getPosition();
                handleMapClick(pos.getLatitude(), pos.getLongitude());
                return false;
            }
        });
    }

    // ????????????????????????
    private void handleMapClick(final double lat, final double lon) {
        moveTo(lat, lon);
    }

    // ????????????????????????????????????
    private void initLocation() {
        tencentLocationManager = TencentLocationManager.getInstance(this);
        tencentLocationManager.setCoordinateType(TencentLocationManager.COORDINATE_TYPE_WGS84);
        tencentLocationRequest = TencentLocationRequest.create();
        tencentLocationRequest.setInterval(100);
        tencentLocationManager.requestLocationUpdates(tencentLocationRequest, this);
    }

    // ????????????????????????????????????
    private void initMoveManager() {
        if (Build.VERSION.SDK_INT < 23) {
            if (Settings.Secure.getInt(this.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION, 0) == 0) {
                simulateLocationPermission();
            }
        }
        random = new Random();
        try {
            locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            locationManager.addTestProvider(LocationManager.GPS_PROVIDER, false,
                    true, false, false, true,
                    true, true, 0, 5);
            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
        } catch (SecurityException e) {
            simulateLocationPermission();
        }
    }

    // ?????????websocket
    private void initWebsocket() {
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .pingInterval(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        wsManager = new WsManager.Builder(this)
                .wsUrl(wssHost)
                .needReconnect(true)
                .client(okHttpClient)
                .build();
        wsManager.setWsStatusListener(new WsStatusListener() {
            @Override
            public void onOpen(Response response) {
                super.onOpen(response);
            }

            @Override
            public void onMessage(String text) {
                super.onMessage(text);
            }

            @Override
            public void onMessage(ByteString bytes) {
                requestSuccess = true;
                super.onMessage(bytes);
                byte[] bs = bytes.toByteArray();
                byte[] buffer = new byte[bs.length-4];
                System.arraycopy(bytes.toByteArray(), 4, buffer, 0, bs.length-4);
                String j = new String(buffer);
                try {
                    JSONObject json = new JSONObject(j);
                    int retcode = json.getInt("retcode");
                    if (retcode == 0) {
                        if (saveStatus == 2) {
                            setToken();
                        }
                    } else {
                        getToken();
                    }
                    saveStatus = 1;
                    jsonArray = json.getJSONArray("sprite_list");
                    formatPets(jsonArray);
                    //onClickNext();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onReconnect() {
                super.onReconnect();
            }

            @Override
            public void onClosing(int code, String reason) {
                super.onClosing(code, reason);
                onReconnect();
            }

            @Override
            public void onClosed(int code, String reason) {
                super.onClosed(code, reason);
                onReconnect();
            }

            @Override
            public void onFailure(Throwable t, Response response) {
                super.onFailure(t, response);
                onReconnect();
            }
        });
        wsManager.startConnect();
    }

    private void onClickMod() {
        modType += 1;
        modType = modType % 4;
        switch(modType) {
            case 0:
                modButton.setText("???");
                autoButton.setText("???");
                break;
            case 1:
                modButton.setText("???");
                autoButton.setText("???");
                break;
            case 2:
                modButton.setText("???");
                autoButton.setText("???");
                break;
            case 3:
                modButton.setText("???");
                autoButton.setText("???");
                break;
        }
    }

    private void onClickSave() {
        new AlertDialog.Builder(this).setTitle("????????????????")
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveStatus = 2;
                        getPets(latSpan, longtitude);
                        //setToken();
                    }
                })
                .setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    private void setToken() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://gt.buxingxing.com/api/v1/token");
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setReadTimeout(5000);
                    conn.setConnectTimeout(5000);
                    conn.setUseCaches(false);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.connect();
                    DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                    JSONObject body = new JSONObject();
                    body.put("openid", openid);
                    body.put("token", gwgo_token);
                    String json = java.net.URLEncoder.encode(body.toString(), "utf-8");
                    out.writeBytes(json);
                    out.flush();
                    out.close();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String lines;
                    StringBuffer sb = new StringBuffer("");
                    while((lines = reader.readLine()) != null) {
                        lines = URLDecoder.decode(lines, "utf-8");
                        sb.append(lines);
                    }
                    reader.close();
                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        fixedThreadPool.execute(runnable);
    }

    private void getToken() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //Looper.prepare();
                try {
                    URL url = new URL("http://gt.buxingxing.com/api/v1/token");
                    //URL url = new URL("http://api.eiiku.com/zhuoyaoleida/api.php");
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setReadTimeout(5000);
                    conn.setConnectTimeout(5000);
                    conn.setRequestMethod("GET");
                    conn.connect();
                    InputStream inputStream = null;
                    BufferedReader reader = null;
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        inputStream = conn.getInputStream();
                        reader = new BufferedReader(new InputStreamReader(inputStream));
                        String result = reader.readLine();
                        Log.i("token_result", result);
                        JSONObject data = new JSONObject(result);
                        Log.i("gwgo_token", data.toString());
                        gwgo_token = data.getJSONObject("data").get("token").toString();
                        openid = data.getJSONObject("data").get("openid").toString();
                        openidText.setText(openid);
                        tokenText.setText(gwgo_token);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        fixedThreadPool.execute(runnable);
    }

    private void formatPets(JSONArray ja) {
        JSONArray tmpJsonArray = new JSONArray();
        for (int i = 0; i < ja.length(); i ++) {
            try {
                JSONObject pet = ja.getJSONObject(i);
                int petId = pet.getInt("sprite_id");
                if (!allPetSet.contains(petId)) {
                    Log.i("NNNN:", String.valueOf(petId));
                }
                if (selectedPetSet.contains(petId)) {
                    double tmpNextLatitude = (double)pet.getInt("latitude") / (1000 * 1000);
                    double tmpNextLongtitude = (double)pet.getInt("longtitude") / (1000 * 1000);
                    GPS gps = gcj2gps84(tmpNextLatitude, tmpNextLongtitude);
                    pet.put("latitude", gps.getLat());
                    pet.put("longtitude", gps.getLon());
                    int gentime = pet.getInt("gentime");
                    int lifetime = pet.getInt("lifetime");
                    pet.put("endtime", gentime + lifetime);
                    tmpJsonArray.put(pet);
                    showPetToMap(petId, gps.getLat(), gps.getLon());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // ???petJsonArray????????????
        petJsonArray = tmpJsonArray;
        currentIndex = petJsonArray.length() - 1;
        if (petJsonArray.length() == 0) {
            toast = Toast.makeText(getApplicationContext(), "????????????????????????????????????????????????", Toast.LENGTH_LONG);
        } else {
            toast = Toast.makeText(getApplicationContext(), "????????????", Toast.LENGTH_SHORT);
        }
        toast.show();
    }

    // ????????????????????????
    private void showPetToMap(int petId, double lat, double lon) {
        tencentMap.addMarker(new MarkerOptions()
                .anchor(0.5f, 0.5f)
                .position(new LatLng(lat, lon))
                .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(headBitmaps.get(petId), dpi2pix(30), dpi2pix(30), true)))
                .draggable(true));
    }

    private void clearPetsFromMap() {
        tencentMap.clearAllOverlays();
        isMarker = false;
    }

    private void getPets(double lat, double lon) {
        clearPetsFromMap();
        toast = Toast.makeText(getApplicationContext(), "?????????...", Toast.LENGTH_SHORT);
        toast.show();
        try {
            jsonObject = new JSONObject();
            jsonObject.put("request_type", "1001");
            jsonObject.put("latitude", (int)(lat*1000*1000));
            jsonObject.put("longtitude", (int)(lon*1000*1000));
            jsonObject.put("platform", 0);
            jsonObject.put("appid", appid);
            jsonObject.put("openid", openid);
            jsonObject.put("gwgo_token", gwgo_token);
            // ??????requestId???????????????
            requestId = System.currentTimeMillis() % (10 * 1000 * 1000);
            jsonObject.put("requestid", requestId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String json = jsonObject.toString();
        int length = json.length();
        byte[] jsonByte = json.getBytes();
        byte[] buffer = new byte[4+length];
        length += 4;
        buffer[0] = (byte)(length & 0xFF000000);
        buffer[1] = (byte)(length & 0xFF0000);
        buffer[2] = (byte)(length & 0xFF00);
        buffer[3] = (byte)(length & 0xFF);
        System.arraycopy(jsonByte, 0, buffer, 4, jsonByte.length);
        final ByteString bytes = ByteString.of(buffer);
        // if (!wsManager.isWsConnected()) {
        //     wsManager.startConnect();
        // }
        // wsManager.sendMessage(bytes);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                requestSuccess = false;
                int try_count = 5;
                while (try_count > 0) {
                    if (requestSuccess) {
                        break;
                    }
                    wsManager.sendMessage(bytes);
                    try_count --;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        fixedThreadPool.execute(runnable);
    }

    // ????????????????????????
    private void initController() {
        // ???????????????
        controllerLayoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            controllerLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            controllerLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        controllerLayoutParams.format = PixelFormat.RGBA_8888;
        controllerLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        controllerLayoutParams.gravity = Gravity.START | Gravity.TOP;
        // ????????????????????????
        controllerLayoutParams.width = dpi2pix(100);
        controllerLayoutParams.height = dpi2pix(340);
        // ????????????????????????????????????
        controllerLayoutParams.x = metrics.widthPixels;
        controllerLayoutParams.y = metrics.heightPixels / 2 - dpi2pix(240) / 2;

        // ?????????????????????
        stopButton = controllerView.findViewById(R.id.stopButton);
        autoButton = controllerView.findViewById(R.id.autoButton);
        backButton = controllerView.findViewById(R.id.backButton);
        filterButton = controllerView.findViewById(R.id.filterButton);
        mapButton = controllerView.findViewById(R.id.mapButton);
        nextButton = controllerView.findViewById(R.id.nextButton);
        patrolButton = controllerView.findViewById(R.id.patrolButton);
        modButton = controllerView.findViewById(R.id.modButton);
        // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
        // ???????????????????????????layout??????????????????????????????LayoutInflater??????????????????layout?????????
        speedSeekBar = (SeekBar) controllerView.findViewById(R.id.speedSeekBar);
        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                speed = baseSpeed + baseSpeed * (i / 30);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        // ?????????????????????????????????????????????????????????????????????
        rockerView = controllerView.findViewById(R.id.rockerView);
        rockerView.setCallBackMode(RockerView.CallBackMode.CALL_BACK_MODE_STATE_CHANGE);
        rockerView.setOnAngleChangeListener(new RockerView.OnAngleChangeListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void angle(double v) {
                angle = v;
            }

            @Override
            public void onFinish() {

            }
        });
    }

    // ?????????????????????
    private void showController() {
        if (Settings.canDrawOverlays(this)) {
            // ??????????????????activity_controller????????????????????????????????????
            windowManager.addView(controllerView, controllerLayoutParams);
        }
    }

    // ???????????????
    private void initFilter() {
        headLinearLayout = (LinearLayout) filterView.findViewById(R.id.headLinearLayout);
        // ?????????????????????
        filterLayoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            filterLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            filterLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        filterLayoutParams.format = PixelFormat.RGBA_8888;
        filterLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        filterLayoutParams.gravity = Gravity.START | Gravity.TOP;
        // ????????????????????????
        filterLayoutParams.width = metrics.widthPixels;
        filterLayoutParams.height = metrics.heightPixels;
        // ????????????????????????????????????
        int width = (metrics.widthPixels - 110) / 10;
        int height = width;
        headLayoutParams = new LayoutParams(width, height);
        headLayoutParams.setMargins(5, 5, 5, 5);

        // ??????assets??????????????????
        assetManager = this.getResources().getAssets();
        try {
            // ????????????????????????assets/heads??????????????????????????????
            headImages = assetManager.list("heads");
        } catch(IOException e) {
            e.printStackTrace();
        }
        int col = (int)Math.ceil(headImages.length / 10);
        filterLayoutParams.x = 5;
        filterLayoutParams.y = (metrics.heightPixels - col * 10 - width * col) / 2 - 150;
        filterLayoutParams.height = col * 10 + width * col + 300;
        // ??????????????????????????????????????????id???key???false????????????true?????????
        petSharedPreferences = getSharedPreferences("pet", this.MODE_PRIVATE);
        editor = petSharedPreferences.edit();
        petSet = new HashSet<String>(petSharedPreferences.getStringSet("selected", new HashSet<String>()));
        for (String str : petSet) {
            selectedPetSet.add(Integer.valueOf(str));
        }

        drums = petSharedPreferences.getString("drum", "");
        String [] tmpDrums = drums.split(",");
        Log.i("tmptmp", String.valueOf(tmpDrums.length));
        if (tmpDrums.length > 1) {
            for (int i = 0; i < tmpDrums.length; i += 2) {
                drumLocations.add(Double.valueOf(tmpDrums[i]));
                drumLocations.add(Double.valueOf(tmpDrums[i+1]));
            }
        }

        battlefields = petSharedPreferences.getString("battlefield", "");
        String [] tmpBattlefields = battlefields.split(",");
        if (tmpBattlefields.length > 1) {
            for (int i = 0; i < tmpBattlefields.length; i += 2) {
                battlefieldLocations.add(Double.valueOf(tmpBattlefields[i]));
                battlefieldLocations.add(Double.valueOf(tmpBattlefields[i+1]));
            }
        }

        stones = petSharedPreferences.getString("stone", "");
        String [] tmpStones = stones.split(",");
        if (tmpStones.length > 1) {
            for (int i = 0; i < tmpStones.length; i += 2) {
                stoneLocations.add(Double.valueOf(tmpStones[i]));
                stoneLocations.add(Double.valueOf(tmpStones[i+1]));
            }
        }
    }

    // ??????????????????
    private void showFilter() {
        if (Build.VERSION.SDK_INT > 22) {
            if (Settings.canDrawOverlays(this)) {
                windowManager.addView(filterView, filterLayoutParams);
            }
        }
        //showPets();
    }
    private void initFloatMap() {
        floatMapViewParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            floatMapViewParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            floatMapViewParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        floatMapViewParams.format = PixelFormat.RGBA_8888;
        floatMapViewParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        floatMapViewParams.gravity = Gravity.START | Gravity.TOP;
        floatMapViewParams.width = metrics.widthPixels - 40;
        floatMapViewParams.x = 40 / 2;
        floatMapViewParams.height = metrics.heightPixels - 40 * (metrics.heightPixels / metrics.widthPixels);
        floatMapViewParams.y = (metrics.heightPixels - floatMapViewParams.height) / 2;
        LinearLayout floatMapLinearLayout = (LinearLayout)floatMapView.findViewById(R.id.floatmaplinearlayout);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)floatMapLinearLayout.getLayoutParams();
        params.width = floatMapViewParams.width;
        params.height = floatMapViewParams.height - 200;
        floatMapLinearLayout.setLayoutParams(params);
        floatMapCloseButton = floatMapView.findViewById(R.id.closefloatmapbutton);
        showFloatMap();
    }

    //?????????????????????
    private void showFloatMap() {
        if (Build.VERSION.SDK_INT > 22) {
            if (Settings.canDrawOverlays(this)) {
                windowManager.addView(floatMapView, floatMapViewParams);
            }
        }
    }
    //?????????????????????
    private void removeFloatMap() {
        windowManager.removeView(floatMapView);
    }

    private void initPets() {
        // ??????assets??????????????????
        assetManager = this.getResources().getAssets();
        try {
            // ????????????????????????assets/heads??????????????????????????????
            headImages = assetManager.list("heads");
        } catch(IOException e) {
            e.printStackTrace();
        }
        InputStream input = null;
        for (int i = 0; i < headImages.length; i ++) {
            int petId = Integer.valueOf(headImages[i].substring(0, headImages[i].indexOf(".")));
            allPetSet.add(petId);
            try {
                input = assetManager.open(headImages[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            headBitmaps.put(petId, BitmapFactory.decodeStream(input));
        }
    }

    // ??????????????????
    // ??????????????????assets???????????????????????????10???
    private void showPets() {
        InputStream input = null;
        LinearLayout imageLinearLayout = null;
        int count = 0;
        for (int petId : allPetSet) {
            // ??????????????????
            if (count % 10 == 0) {
                imageLinearLayout = new LinearLayout(this);
                imageLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                headLinearLayout.addView(imageLinearLayout);
            }
            count ++;
            final ImageView imgView = new ImageView(this);
            imgView.setLayoutParams(headLayoutParams);
            imgView.setImageBitmap(headBitmaps.get(petId));
            imgView.setId(petId);
            if (selectedPetSet.contains(petId)) {
                imgView.setAlpha(clickHeadAlpha);
            } else {
                imgView.setAlpha(unclickHeadAlpha);
            }
            // ?????????????????????????????????????????????????????????
            imgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int imageId = view.getId();
                    if (selectedPetSet.contains(imageId)) {
                        petSet.remove(String.valueOf(imageId));
                        selectedPetSet.remove(imageId);
                        view.setAlpha(unclickHeadAlpha);
                    } else {
                        petSet.add(String.valueOf(imageId));
                        selectedPetSet.add(imageId);
                        view.setAlpha(clickHeadAlpha);
                    }
                    // ???????????????????????????????????????SharedPreferences???????????????????????????
                    editor.putStringSet("selected", petSet);
                    editor.commit();
                }
            });
            imageLinearLayout.addView(imgView);
        }
    }
    // ??????????????????
    private void removeFilter() {
        windowManager.removeView(filterView);
    }

    public void onClick(View view) {
        setButtonColor((Button)view);
        switch (view.getId()) {
            case R.id.stopButton:
                onClickOnOff();
                break;
            case R.id.backButton:
                onClickBack();
                break;
            case R.id.filterButton:
                onClickFilter();
                break;
            case R.id.autoButton:
                onClickAuto();
                break;
            case R.id.closeFilterButton:
                removeFilter();
                break;
            case R.id.mapButton:
                onClickFloatMap();
                break;
            case R.id.closefloatmapbutton:
                removeFloatMap();
                break;
            case R.id.nextButton:
                onClickNext();
                break;
            case R.id.patrolButton:
                //onPatrolClick();
                //setToken();
                getToken();
                break;
            case R.id.saveButton:
                onClickSave();
                //setToken();
                break;
            case R.id.modButton:
                onClickMod();
                break;
        }
    }
    // dpi???pix
    private int dpi2pix(float dpi) {
        return (int)(dpi * metrics.density + 0.5f);
    }

    // ????????????????????????????????????????????????backButton??????
    // ??????????????????????????????????????????????????????????????????
    public void onClickBack() {
        isBack += 1;
        if (isBack % 2 == 1) {
            // controllerLayoutParams.width = 260 / 2 - 10;
            controllerLayoutParams.width = dpi2pix(50);
            // controllerLayoutParams.height = (260 / 2 - 10) * 2;
            controllerLayoutParams.height = dpi2pix(150);
            windowManager.updateViewLayout(controllerView, controllerLayoutParams);
            backButton.setText("???");
        } else {
            controllerLayoutParams.width = dpi2pix(100);
            controllerLayoutParams.height = dpi2pix(340);
            windowManager.updateViewLayout(controllerView, controllerLayoutParams);
            backButton.setText("???");
        }
    }
    // ?????????????????????
    public void onClickFilter() {
        showFilter();
    }
    private void onClickFloatMap() {
        initFloatMap();
    }
    private void onClickAuto() {
        if (modType == 0) {
            getPets(latitude, longtitude);
        } else {
            saveLocation();
        }
    }
    private void saveLocation() {
        if (latitude < 1 || longtitude < 1) {
            return;
        }
        switch (modType) {
            case 1:
                if (drums == "") {
                    drums = latitude + ","+ longtitude;
                } else {
                    drums = drums + "," + latitude + ","+ longtitude;
                }
                drumLocations.add(latitude);
                drumLocations.add(longtitude);
                editor.putString("drum", drums);
                editor.commit();
                break;
            case 2:
                if (battlefields == "") {
                    battlefields = latitude + ","+ longtitude;
                } else {
                    battlefields = battlefields + "," + latitude + ","+ longtitude;
                }
                battlefieldLocations.add(latitude);
                battlefieldLocations.add(longtitude);
                editor.putString("battlefield", battlefields);
                editor.commit();
                break;
            case 3:
                if (stones == "") {
                    stones = latitude + ","+ longtitude;
                } else {
                    stones = stones + "," + latitude + ","+ longtitude;
                }
                stoneLocations.add(latitude);
                stoneLocations.add(longtitude);
                editor.putString("stone", stones);
                editor.commit();
                break;
        }
        Toast toast = Toast.makeText(getApplicationContext(), "????????????", Toast.LENGTH_SHORT);
        toast.show();
    }
    public void onClickNext() {
        if (modType == 0) {
            goToNextPet();
        } else if (modType == 1){
            goToNextDrum();
        } else if (modType == 2) {
            goToNextBattlefield();
        } else if(modType == 3) {
            goToNextStone();
        }
    }
    private void goToNextStone() {
        if (stoneLocations.size() > 0) {
            moveTo(stoneLocations.get(stoneIndex), stoneLocations.get(stoneIndex+1));
            stoneIndex += 2;
            stoneIndex = stoneIndex % stoneLocations.size();
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "???????????????", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
    private void goToNextDrum() {
        if (drumLocations.size() > 0) {
            moveTo(drumLocations.get(drumIndex), drumLocations.get(drumIndex+1));
            drumIndex += 2;
            drumIndex = drumIndex % drumLocations.size();
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "???????????????", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
    private void goToNextBattlefield() {
        if (battlefieldLocations.size() > 0) {
            moveTo(battlefieldLocations.get(battlefieldIndex), battlefieldLocations.get(battlefieldIndex+1));
            battlefieldIndex += 2;
            battlefieldIndex = battlefieldIndex % battlefieldLocations.size();
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "???????????????", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
    // ?????????????????????
    public void goToNextPet() {
        // ??????????????????????????????????????????????????????????????????
        double nextLatitude = 0;
        double nextLongtitude = 0;
        if (petJsonArray != null && petJsonArray.length() > 0 && currentIndex >= 0) {
            for (; currentIndex >= 0; currentIndex --) {
                try {
                    JSONObject currentPet = petJsonArray.getJSONObject(currentIndex);
                    nextLatitude = currentPet.getDouble("latitude");
                    nextLongtitude = currentPet.getDouble("longtitude");
                    final int sprite_id = currentPet.getInt("sprite_id");
                    int endtime = currentPet.getInt("endtime");
                    long currentTime = System.currentTimeMillis() / 1000;
                    if (selectedPetSet.contains(sprite_id) && endtime > (currentTime + 2)) {
                        break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            // ????????????????????????currentIndex????????????0????????????break?????????
            if (currentIndex >= 0) {
                moveTo(nextLatitude, nextLongtitude);
                currentIndex --;
            } else {
                getPets(latitude, longtitude);
            }
        } else {
            getPets(latitude, longtitude);
        }
    }
    private void moveTo(final double lat, final double lon) {
        Runnable runnable =  new Runnable() {
            @Override
            public void run() {
                final double latitudeStep = (lat - latitude) / 3000;
                final double longtitudeStep = (lon - longtitude) / 3000;
                // ????????????????????????3????????????????????????
                for (int i = 1; i < 3000; i ++) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    latitude += latitudeStep;
                    longtitude += longtitudeStep;
                    //setLocation(latitude, longtitude);
                }
                // ?????????????????????????????????????????????????????????????????????
                latitude = lat;
                longtitude = lon;
            }
        };
        fixedThreadPool.execute(runnable);
    }
    // ??????????????????????????????stopButton??????
    public void onClickOnOff() {
        isRun += 1;
        if (isRun % 2 == 0) {
            stopButton.setText("???");
        } else {
            stopButton.setText("???");
        }
    }

    // ??????tencent????????????????????????????????????????????????????????????????????????
    // ???????????????????????????????????????????????????????????????
    @Override
    public void onLocationChanged(TencentLocation location, int error, String reason) {
        if (latitude == 0 && longtitude == 0) {
            latitude = location.getLatitude();
            longtitude = location.getLongitude();
            Toast toast = Toast.makeText(getApplicationContext(), "????????????", Toast.LENGTH_SHORT);
            toast.show();
        }
        // ????????????????????????
        // int x = metrics.widthPixels / 2;
        // int y = metrics.heightPixels / 2 - 50 * 3 / 2;
        // String[] order = {"input", "tap", "" + x, "" + y};
        // try {
        //     new ProcessBuilder(order).start();
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }


        if (firstLocation && latitude != 0 && longtitude != 0) {
            firstLocation = false;
            tencentMap.setCenter(new LatLng(latitude, longtitude));
        }
        if (!isMarker) {
            marker = tencentMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker())
                    .draggable(true));
            isMarker = true;
        }
        marker.setPosition(new LatLng(latitude, longtitude));
    }

    @Override
    public void onStatusUpdate(String name, int status, String desc) {

    }

    private void setButtonColor(Button btn) {
        int alpha = genInt(0x8D, 0xEF);
        btn.setBackgroundColor((alpha<<24) | random.nextInt(0x00FFFFFF));
    }

    // ??????????????????????????????
    private double genDouble(final double min, final double max) {
        return min + ((max - min) * random.nextDouble());
    }

    // ???????????????????????????
    private int genInt(final int min, final int max) {
        return random.nextInt(max) % (max-min+1) + min;
    }

    private void setLocation(double longtitude, double latitude) {

        // ????????????????????????????????????????????????????????????????????????????????????????????????
        altitude = genDouble(38.0, 50.5);
        // ?????????????????????GPS??????????????????15??????????????????1???15???????????????
        accuracy = (float)genDouble(1.0, 15.0);

        // ??????????????????????????????????????????
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setTime(System.currentTimeMillis());
        location.setLatitude(latitude);
        location.setLongitude(longtitude);
        // ???????????????????????????????????????
        location.setAltitude(altitude);
        // GPS?????????????????????????????????
        location.setAccuracy(accuracy);
        if (Build.VERSION.SDK_INT > 16) {
            location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        try {
            locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location);
        } catch (SecurityException e) {
            simulateLocationPermission();
        }
    }

    public void continueLocation2() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                while (true) {
                    try {
                        // ??????????????????????????????????????????????????????????????????????????????????????????????????????
                        // ?????????????????????????????????????????????????????????, ???????????????????????????????????????
                        // ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                        // ????????????????????????????????????????????????????????????1??????
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // isRun?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    if (isRun % 2 == 1) {
                        // ?????????????????????????????????0?????????1?????????2?????????3
                        // ????????????????????????????????????????????????
                        if (isPatrol % 2 == 0) {
                            if ( angle >= 0 && angle < 90) {
                                double radinas = Math.toRadians(angle);
                                latitude -= Math.sin(radinas) * speed;
                                longtitude += Math.cos(radinas) * speed;
                            } else if (angle >= 90 && angle < 180) {
                                double radinas = Math.toRadians(180 - angle);
                                latitude -= Math.sin(radinas) * speed;
                                longtitude -= Math.cos(radinas) * speed;
                            } else if (angle >= 180 && angle < 270) {
                                double radinas = Math.toRadians(angle - 180);
                                latitude += Math.sin(radinas) * speed;
                                longtitude -= Math.cos(radinas) * speed;
                            } else {
                                double radinas = Math.toRadians(360 - angle);
                                latitude += Math.sin(radinas) * speed;
                                longtitude += Math.cos(radinas) * speed;
                            }
                        } else {
                            // ???????????????????????????
                            if (count % loop == 0) {
                                angle += 90;
                                angle %= 360;
                                if (angle == 270) {
                                    loop += step;
                                    count = 1;
                                }
                            }
                            count += 1;
                            if (angle == 0) {
                                longtitude += patrolSpeed;
                            } else if (angle == 90) {
                                latitude -= patrolSpeed;
                            } else if (angle == 180) {
                                longtitude -= patrolSpeed;
                            } else if (angle == 270) {
                                latitude += patrolSpeed;
                            }
                        }
                    }
                    setLocation(longtitude, latitude);
                }
            }
        };
        fixedThreadPool.execute(runnable);
    }

    // ????????????
    public void continueLocation() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                while (true) {
                    try {
                        // ??????????????????????????????????????????????????????????????????????????????????????????????????????
                        // ?????????????????????????????????????????????????????????, ???????????????????????????????????????
                        // ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                        // ????????????????????????????????????????????????????????????1??????
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // isRun?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    if (isRun % 2 == 1) {
                        // ?????????????????????????????????0?????????1?????????2?????????3
                        // ????????????????????????????????????????????????
                        if (isPatrol % 2 == 0) {
                            if ( angle >= 0 && angle < 90) {
                                double radinas = Math.toRadians(angle);
                                latitude -= Math.sin(radinas) * speed;
                                longtitude += Math.cos(radinas) * speed;
                            } else if (angle >= 90 && angle < 180) {
                                double radinas = Math.toRadians(180 - angle);
                                latitude -= Math.sin(radinas) * speed;
                                longtitude -= Math.cos(radinas) * speed;
                            } else if (angle >= 180 && angle < 270) {
                                double radinas = Math.toRadians(angle - 180);
                                latitude += Math.sin(radinas) * speed;
                                longtitude -= Math.cos(radinas) * speed;
                            } else {
                                double radinas = Math.toRadians(360 - angle);
                                latitude += Math.sin(radinas) * speed;
                                longtitude += Math.cos(radinas) * speed;
                            }
                        } else {
                            // ???????????????????????????
                            if (count % loop == 0) {
                                angle += 90;
                                angle %= 360;
                                if (angle == 270) {
                                    loop += step;
                                    count = 1;
                                }
                            }
                            count += 1;
                            if (angle == 0) {
                                longtitude += patrolSpeed;
                            } else if (angle == 90) {
                                latitude -= patrolSpeed;
                            } else if (angle == 180) {
                                longtitude -= patrolSpeed;
                            } else if (angle == 270) {
                                latitude += patrolSpeed;
                            }
                        }
                    }
                    setLocation(longtitude, latitude);
                }
            }
        });
        thread.start();
    }

    // ??????
    private void onPatrolClick() {
        isRun += 1;
        isPatrol += 1;
        angle = 0;
        count = 1;
        loop = 10000;
    }

    private class GPS {
        private double lat;
        private double lon;
        public GPS(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
        public double getLat() {
            return lat;
        }
        public double getLon() {
            return lon;
        }
        public void setLat(double lat) {
            this.lat = lat;
        }
        public void setLon(double lon) {
            this.lon = lon;
        }
    }

    // GCJ02?????????GPS84
    private GPS gcj2gps84(double lat, double lon) {
        double dLat = transLat(lon - 105.0, lat - 35.0);
        double dLon = transLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double mgLat = lat + dLat;
        double mgLon = lon + dLon;
        mgLat = lat * 2 - mgLat;
        mgLon = lon * 2 - mgLon;
        return new GPS(mgLat, mgLon);
    }
    private double transLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
        return ret;
    }
    private double transLon(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0 * pi)) * 2.0 / 3.0;
        return ret;
    }

    // ???????????????
    private void floatWindowPermission() {
        new AlertDialog.Builder(this)
                .setTitle("???????????????")
                .setMessage(("?????????????????????"))
                .setPositiveButton("??????",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                                    startActivity(intent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                .setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .show();
    }

    // ?????????????????????
    private void simulateLocationPermission() {
        new AlertDialog.Builder(this)
                .setTitle("??????????????????")
                .setMessage("??????????????????????????????????????????????????????")
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .show();
    }

    // ????????????????????????
    private void getNextLocation() {

    }
}
