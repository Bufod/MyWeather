package com.example.myweather;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebViewActivity extends AppCompatActivity {

    Button btSave;
    WebView webView;
    String url, ansUrl, strServerType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        webView = findViewById(R.id.webView);
        if (getIntent().hasExtra("url")) {
            url = getIntent().getStringExtra("url");
        }

        if (getIntent().hasExtra("serverType")) {
            strServerType = getIntent().getStringExtra("serverType");
        }
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), webView.getUrl(), Toast.LENGTH_SHORT).show();
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);//включение js для использования
                                                         //поиска по открытому сайту

        btSave = (Button) findViewById(R.id.buttonSave);

        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ansUrl = webView.getUrl();
                if (!ansUrl.equals(url)) {
                    Pattern pattern;
                    Matcher matcher;
                    boolean correct = false;
                    if (strServerType.equals("Яндекс.Погода")) {
                        Pattern pattern1 = Pattern.compile("(lat=)([0-9.]*)"),//использование паттернов
                                pattern2 = Pattern.compile("(lon=)([0-9.]*)");//для получения корректных ссылок
                        Matcher matcher1 = pattern1.matcher(ansUrl),
                                matcher2 = pattern2.matcher(ansUrl);
                        if (matcher1.find() && matcher2.find()) {
                            ansUrl = String.format(
                                    "https://yandex.ru/pogoda/details?%s&%s&via=ms",//шаблон замены
                                    ansUrl.substring(matcher1.start(), matcher1.end()),
                                    ansUrl.substring(matcher2.start(), matcher2.end()));
                            correct = true;
                        }
                        else {
                            pattern = Pattern.compile("(pogoda/)([A-z]*)");//второй вид паттерна
                            matcher = pattern.matcher(ansUrl);
                            if (matcher.find()) {
                                ansUrl = "https://yandex.ru/" +
                                        ansUrl.substring(matcher.start(), matcher.end()) +
                                        "/details?via=ms";
                                correct = true;
                            }
                        }
                    } else if (strServerType.equals("Weather.com")) {
                        pattern = Pattern.compile("(l/)([0-9A-z.,]*)");//использование паттернов
                                                                       //для получения корректных ссылок
                        matcher = pattern.matcher(ansUrl);
                        if (matcher.find()) {
                            ansUrl = "https://weather.com/ru-RU/weather/tenday/" +
                                    ansUrl.substring(matcher.start(), matcher.end());
                            correct = true;
                        }
                    }

                    if (correct) {
                        Intent intent = getIntent();
                        intent.putExtra("url", ansUrl);
                        setResult(RESULT_OK, intent);
                        finish();
                        return;
                    }

                }
                Toast.makeText(getApplicationContext(),
                        "Не удалось добавить сервер!",
                        Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED);
                finish();
            }
        });


    }
}
