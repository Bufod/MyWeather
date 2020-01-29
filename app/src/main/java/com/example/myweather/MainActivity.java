package com.example.myweather;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.myweather.location.AddLocationActivity;
import com.example.myweather.location.Location;
import com.example.myweather.location.ShowLocationActivity;
import com.example.myweather.server.AddServerActivity;
import com.example.myweather.server.Server;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private final String WEATHER_COM = "Weather.com",
            YANDEX_WEATHER = "Яндекс.Погода";
    private final static int ADD_SERVER = 0, EDIT_SERVER = 1, ADD_LOCATION = 2;
    Button bt;
    NavigationView navigationView;
    WeatherComFragment weatherComFragment;
    YandexWeatherFragment yandexWeatherFragment;
    FragmentTransaction fTrans;
    DBServer data;
    DBServer.LocationTable locationTable;
    DBServer.ServerTable serverTable;
    Location favLocation;
    DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //работа с бд
        data = new DBServer(this); // подключение к бд
        locationTable = data.new LocationTable(); // загрузка таблиц с доступной местностью
        serverTable = data.new ServerTable(); // загрузка таблиц с доступными серверами

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //добавление навигации
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        //выбор стартового параметра
        navigationView.getMenu().findItem(R.id.weatherCom).setChecked(true);

        //кнопка смены локации в заголовке меню
        bt = navigationView.getHeaderView(0).findViewById(R.id.locationBt);

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, ShowLocationActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //проверка существующих таблиц локаций
        if (locationTable.isEmpty()) {
            //вывод начального экрана создания локации
            setContentView(R.layout.empty_start_acivity);
            ((Button) findViewById(R.id.btnStart)).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getApplicationContext(), AddLocationActivity.class);
                            intent.putExtra("lastId", data.getLastInd(locationTable));
                            startActivityForResult(intent, ADD_LOCATION);

                        }
                    }
            );
        } else {
            //вывод главного экрана
            setContentView(drawer);

            //получение главной локации
            Location tmpLocation = locationTable.selectFavorite();
            Server sW = null, //weather.com
                    sY = null; //яндекс.погода

            //вывод новых фрагментов если главная локация не задана, но существет
            //или главная локация отличается от заданой
            if (favLocation == null || !favLocation.equals(tmpLocation)) {
                updateFavLocation();

                //создание фргаментов на основе сервера
                sW = serverTable
                        .selectServerByLocationAndType(favLocation, WEATHER_COM);
                weatherComFragment = WeatherComFragment
                        .newInstance(sW);
                weatherComFragment.setListnerBtAddServer(listenerAddServer(WEATHER_COM));

                sY = serverTable
                        .selectServerByLocationAndType(favLocation, YANDEX_WEATHER);
                yandexWeatherFragment = YandexWeatherFragment
                        .newInstance(sY);
                yandexWeatherFragment.setListnerBtAddServer(listenerAddServer(YANDEX_WEATHER));

                fTrans = getFragmentManager().beginTransaction();
                fTrans.replace(R.id.contentContainer, weatherComFragment);
                fTrans.commit();
            }
            if (sW != null)
                navigationView.getMenu().findItem(R.id.weatherCom).setTitle(sW.getName());
            if (sY != null)
                navigationView.getMenu().findItem(R.id.yandexWeather).setTitle(sY.getName());
        }

    }

    //обновление главной локации приложения
    public void updateFavLocation() {
        favLocation = locationTable.selectFavorite();
        if (favLocation != null)
            bt.setText(favLocation.getName());
    }

    //кнопка назад
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer != null &&
                drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //контекстное меню для главной активности
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.context_main_menu, menu);
        return true;
    }

    //реакция на выбор в контекстном меню
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        Server server;
        String serverType = null;
        if (weatherComFragment.isVisible())
            serverType = WEATHER_COM;
        else if (yandexWeatherFragment.isVisible())
            serverType = YANDEX_WEATHER;

        server = serverTable
                .selectServerByLocationAndType(favLocation, serverType);

        if (id == R.id.action_add_server) {
            if (server == null)
                listenerAddServer(serverType).onClick(null);
            else {
                Intent intent = new Intent(MainActivity.this, AddServerActivity.class);
                intent.putExtra("Server", server);
                intent.putExtra("Location", favLocation);
                startActivityForResult(intent, EDIT_SERVER);
            }
        } else if (id == R.id.action_del_server) {
            if (server == null)
                Toast.makeText(getApplicationContext(),
                        "Подключение отсутствует",
                        Toast.LENGTH_SHORT).show();
            else {
                serverTable.delete(server.getId());
                if (weatherComFragment.isVisible())
                    updateFragment(weatherComFragment);
                else if (yandexWeatherFragment.isVisible())
                    updateFragment(yandexWeatherFragment);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    //слушатель для запуска активности редактирования/добаления подключения
    public View.OnClickListener listenerAddServer(final String serverType) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, AddServerActivity.class);
                i.putExtra("Location", favLocation);
                i.putExtra("ServerType", serverType);
                i.putExtra("LastInd", data.getLastInd(serverTable));
                startActivityForResult(i, ADD_SERVER);
            }
        };
    }

    //обобщенный метод обновления фрагментов
    public <T extends Fragment & WeatherFragment> void updateFragment(T weatherFragment) {
        if (weatherFragment instanceof WeatherComFragment) {
            weatherFragment.getArguments().putSerializable("server", serverTable
                    .selectServerByLocationAndType(favLocation, WEATHER_COM));
        } else if (weatherFragment instanceof YandexWeatherFragment) {
            weatherFragment.getArguments().putSerializable("server", serverTable
                    .selectServerByLocationAndType(favLocation, YANDEX_WEATHER));
        }
        fTrans = getFragmentManager().beginTransaction();
        fTrans.detach(weatherFragment);
        fTrans.attach(weatherFragment);
        fTrans.commit();
    }

    //получение результата при возвращении на главную активность
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            //возврат от активности редактирования/создания педключения
            if (requestCode == ADD_SERVER ||
                    requestCode == EDIT_SERVER) {
                Server server = null;
                if (data != null && data.hasExtra("Server")) {
                    server = (Server) data.getExtras().getSerializable("Server");
                }
                if (server != null) {
                    if (requestCode != ADD_SERVER)
                        serverTable.update(server);
                    else
                        serverTable.insert(
                                server.getServerType(),
                                server.getName(),
                                server.getUrl(),
                                server.getIdLocation());
                }
                if (weatherComFragment.isVisible())
                    updateFragment(weatherComFragment);
                else if (yandexWeatherFragment.isVisible())
                    updateFragment(yandexWeatherFragment);
            }
            //возврат от активности добавления локации
            else if (requestCode == ADD_LOCATION) {
                Bundle bundle = null;
                if (data != null) {
                    bundle = data.getExtras();
                }
                Location location = null;
                if (bundle != null)
                    location = (Location) bundle.getSerializable("Location");
                if (location != null) {
                    locationTable.insert(
                            location.getName(),
                            location.isFavorite());
                }

            }
        }
    }

    //реакция на выбор элементов в главном меню
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        fTrans = getFragmentManager().beginTransaction();
        int id = item.getItemId();
        if (id == R.id.weatherCom) {
            fTrans.replace(R.id.contentContainer, weatherComFragment);
        } else if (id == R.id.yandexWeather) {
            fTrans.replace(R.id.contentContainer, yandexWeatherFragment);
        }

        fTrans.commit();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }
}
