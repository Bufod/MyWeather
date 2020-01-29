package com.example.myweather.server;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myweather.R;
import com.example.myweather.WebViewActivity;
import com.example.myweather.location.Location;

//активность добавления/редактирования сервера
public class AddServerActivity extends AppCompatActivity implements LocationListener {
    private Button btSave, btCancel, btWebExplorer;
    private EditText serverName, serverUrl;
    private TextView textLocation, serverType;
    private Switch switchGps;
    private long ID;
    private Location location;
    private Server server;
    protected LocationManager locationManager;
    private String latitude, longitude, strServerType;
    private final static int GET_URL = 0, GET_PERMISSION_GPS = 0;
    private boolean gpsPosReceived = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_server);

        switchGps = (Switch) findViewById(R.id.switchGps);
        btWebExplorer = (Button) findViewById(R.id.btWebExplorer);
        serverName = (EditText) findViewById(R.id.editServerName);
        serverUrl = (EditText) findViewById(R.id.editServerUrl);
        serverType = (TextView) findViewById(R.id.textServerType);
        textLocation = (TextView) findViewById(R.id.textLocation);
        btSave = (Button) findViewById(R.id.buttonSave);
        btCancel = (Button) findViewById(R.id.buttonClose);

        if (getIntent().hasExtra("Server")) {
            server = (Server) getIntent().getSerializableExtra("Server");
        }

        if (getIntent().hasExtra("Location")) {
            location = (Location) getIntent().getSerializableExtra("Location");
            if (location != null) {
                textLocation.setText(location.getName());
            }
        }

        if (getIntent().hasExtra("ServerType")) {
            strServerType = getIntent().getStringExtra("ServerType");
            serverType.setText(strServerType);

        }

        if (server != null) {
            strServerType = server.getServerType();
            serverType.setText(strServerType);
            String strServerName = server.getName();
            serverName.setText(strServerName == null ? strServerType : strServerName);
            ID = server.getId();
            serverUrl.setText(server.getUrl());
            switchGps.setChecked(false);
            btWebExplorer.setEnabled(true);
        } else {
            ID = getIntent().getIntExtra("LastInd", -1);
            serverName.setText(strServerType);
            startGpsPosSearch();
        }


        //кнопка получения ссылки через web
        btWebExplorer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddServerActivity.this, WebViewActivity.class);
                if (strServerType.equals("Weather.com"))
                    intent.putExtra("url", "https://weather.com/ru-RU/weather/today");
                else if (strServerType.equals("Яндекс.Погода"))
                    intent.putExtra("url", "https://yandex.ru/pogoda/");
                intent.putExtra("serverType", strServerType);
                startActivityForResult(intent, GET_URL);
            }
        });

        //gps использование
        switchGps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    btWebExplorer.setEnabled(false);
                    startGpsPosSearch();
                } else {
                    btWebExplorer.setEnabled(true);
                    gpsPosProgressStop();
                    gpsPosReceived = true;
                    onPause();
                }

            }
        });

        btSave.setOnClickListener(btSaveOnClick());

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //валидация
    private boolean validateEditText(EditText fieldEdText) {
        if (fieldEdText.getText() == null ||
                (fieldEdText.getText() != null &&
                        fieldEdText.getText().toString().equals(""))) {
            fieldEdText.setBackground(getDrawable(R.drawable.bkg_error_edit_text));
            fieldEdText.setError("Обязательное поле");
            return false;
        }
        return true;
    }

    private View.OnClickListener btSaveOnClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateEditText(serverName) &&
                        validateEditText(serverUrl) &&
                        gpsPosReceived) {
                    Server server = new Server(ID,
                            serverType.getText().toString(),
                            serverName.getText().toString(),
                            serverUrl.getText().toString(),
                            location.getId());
                    Intent intent = getIntent();
                    intent.putExtra("Server", server);
                    setResult(RESULT_OK, intent);
                    finish();
                } else if (!gpsPosReceived){
                    gpsPosShowToast();
                }
            }
        };
    }

    //запуск gps
    private void startGpsPosSearch() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        GET_PERMISSION_GPS);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                gpsPosProgressStart();
            }
        } else if (locationManager != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            gpsPosProgressStart();
        }
    }

    //индикаторная полоса получения данных
    private void gpsPosProgressStart() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        gpsPosReceived = false;
    }

    //завершить работу gps
    private void gpsPosProgressStop() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
    }


    //тост для оповещающий о процессе получения координат
    Toast toast;
    private void gpsPosShowToast() {
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(getApplicationContext(),
                "Происходит получение координат",
                Toast.LENGTH_SHORT);
        toast.show();
    }

    //получение ответа от запроса разрешения
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == GET_PERMISSION_GPS) {
            boolean permissionGranted = true; //все разрешения
            //проверка всех разрешений
            for (int curRes : grantResults)
                if (curRes == PackageManager.PERMISSION_DENIED) {
                    permissionGranted = false;
                    break;
                }
            if (permissionGranted &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                gpsPosProgressStart();
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null)
            locationManager.removeUpdates(this);
    }

    //получение координат
    @Override
    public void onLocationChanged(android.location.Location location) {
        latitude = Double.toString(location.getLatitude());
        longitude = Double.toString(location.getLongitude());
        gpsPosReceived = true;
        gpsPosProgressStop();
        if (strServerType != null &&
                strServerType.equals("Weather.com")) {
            serverUrl.setText(
                    String.format("https://weather.com/ru-RU/weather/tenday/l/%s,%s",
                            latitude, longitude));
        } else if (strServerType != null &&
                strServerType.equals("Яндекс.Погода")) {
            serverUrl.setText(
                    String.format("https://yandex.ru/pogoda/details?lat=%s&lon=%s&via=ms",
                            latitude, longitude));
        }
        onPause();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    //ответ от web активности выбора ссылки
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == GET_URL &&
                resultCode == RESULT_OK &&
                data != null &&
                data.hasExtra("url")) {
            serverUrl.setText(data.getStringExtra("url"));
        }
    }
}
