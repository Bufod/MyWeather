package com.example.myweather.location;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myweather.R;

//активность для добавления/редактирования локаций
public class AddLocationActivity extends AppCompatActivity {

    Button btCreate;
    Location location = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);
        final EditText locationName = (EditText) findViewById(R.id.locationName);
        final Switch favorite = (Switch) findViewById(R.id.switchFavorite);
        final int lastInd = getIntent().getIntExtra("lastId", -1);

        if (getIntent().hasExtra("Location")) {
            location = (Location) getIntent().getSerializableExtra("Location");
        }

        //если последний индекс 0 или 1 сделать новую локацию главной
        if (lastInd == -1 || lastInd == 0)
            favorite.setChecked(true);

        btCreate = findViewById(R.id.btCreate);

        //если локация существует - заполнить редактируемые поля
        if (location != null) {
            locationName.setText(location.getName());
            favorite.setChecked(location.isFavorite());
        }

        //кнопка сохранить
        btCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                if (validateEditText(locationName)) {
                    Location nLocation = new Location(location != null ? location.getId() : lastInd + 1,
                            locationName.getText().toString(),
                            favorite.isChecked());
                    intent.putExtra("Location", nLocation);
                    setResult(RESULT_OK, intent);
                    finish();
                }
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
}
