package com.example.myweather.location;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myweather.DBServer;
import com.example.myweather.ListAdapter;
import com.example.myweather.R;

//активность со списком доступных локаций
public class ShowLocationActivity extends AppCompatActivity {

    Context mContext;
    DBServer.LocationTable locationTable;
    ListView mListView;
    Button btAdd;
    final int ADD_LOCATION = 0, EDIT_LOCATION = 1;

    //использование адаптера для списка
    final static ListAdapter<Location> myAdapter = new ListAdapter<Location>() {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = getLayoutInflater().inflate(R.layout.item_location, null);

            TextView locationName = (TextView) convertView.findViewById(R.id.locationName);

            Location location = getArrayMyData().get(position);
            locationName.setText(location.getName());
            if (location.isFavorite())
                ((ImageView)(convertView.findViewById(R.id.locationFavorite)))
                        .setVisibility(View.VISIBLE);
            else
                ((ImageView)(convertView.findViewById(R.id.locationFavorite)))
                        .setVisibility(View.INVISIBLE);

            return convertView;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_location);

        mContext = this;
        final DBServer data = new DBServer(this);
        locationTable = data.new LocationTable();
        mListView = (ListView) findViewById(R.id.list);
        myAdapter
                .setContext(this)
                .setArrayMyData(locationTable.selectAll());
        mListView.setAdapter(myAdapter);
        registerForContextMenu(mListView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Location location = locationTable.select(l);
                location.setFavorite(true);
                locationTable.update(location);
                finish();
            }
        });

        btAdd = (Button) findViewById(R.id.buttonAdd);
        btAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mContext, AddLocationActivity.class);
                i.putExtra("lastId", data.getLastInd(locationTable));
                startActivityForResult(i, ADD_LOCATION);
            }
        });
    }

    //обновление списка
    private void updateList() {
        myAdapter.setArrayMyData(locationTable.selectAll());
        myAdapter.notifyDataSetChanged();
    }

    //возврат от активности редактирования/добавления локации
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Bundle bundle = data.getExtras();
            Location location = null;
            if (bundle != null)
                location = (Location) bundle.getSerializable("Location");
            if (location != null) {
                if (requestCode != ADD_LOCATION)
                    locationTable.update(location);
                else
                    locationTable.insert(
                            location.getName(),
                            location.isFavorite());
                updateList();
            }
        }
    }

    //контекстное меню для элементов списка
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu_location, menu);
    }

    //реакция на выбор пункта контекстного меню
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case R.id.edit:
                Intent i = new Intent(mContext, AddLocationActivity.class);
                Location location = locationTable.select(info.id);
                i.putExtra("Location", location);
                startActivityForResult(i, EDIT_LOCATION);
                updateList();
                return true;
            case R.id.delete:
                locationTable.completeDelete(info.id);
                updateList();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

}
