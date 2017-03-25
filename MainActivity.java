package ag.kirill.ru.yandexapi;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    TextView inputText, translateText, translateInputText;
    TextView changeLanguage;
    LinearLayout translateLayout;
    boolean selectLang;
    String lang;
    YandexAPITask yAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Объявление объектов
        inputText = (TextView) findViewById(R.id.inputTextForTranslate);
        translateText = (TextView) findViewById(R.id.translateText);
        translateInputText = (TextView) findViewById(R.id.translateInputText);
        changeLanguage = (TextView) findViewById(R.id.changeLanguage);
        translateLayout = (LinearLayout) findViewById(R.id.translateLayout);

        lang = "ru";//Значение поумолчанию для параметра lang

        yAPI = new YandexAPITask();
        yAPI.execute();//запускаем поток для отлавливания ввода польхователя

        copyTextInClipboard(translateInputText);
        copyTextInClipboard(translateText);
        copyTextInClipboard(translateLayout);
    }

    //Копирование переведённого текста по клику
    //в буфер обмена
    private void copyTextInClipboard(View v) {
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (translateText.getText().length() > 0) {
                    ClipboardManager clipboard = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("", translateText.getText().toString());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getApplicationContext(), "Перевод скопирован", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    //Смена направления перевода
    public void textClick(View view) {
        String rus = "RUS -> ENG";
        String eng = "ENG -> RUS";

        if (selectLang) {
            lang = "ru";
            changeLanguage.setText(eng);
            selectLang = false;

        } else {
            lang = "en";
            changeLanguage.setText(rus);
            selectLang = true;
        }

    }


    //Поток для перевода текста
    class YandexAPITask extends AsyncTask<Void, String, Void> {

        //Метод для перевода текста
        //Входные параметры:
        //lang - направление перевода ru/en
        //input - текст для превода
        public String translate(String lang, String input) throws IOException {
            String urlStr = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=trnsl.1.1.20170323T024125Z.426dbda43b070650.48edaa13c835c5512a10e05460dbe43bb28b5509";
            URL urlObj = new URL(urlStr);
            HttpsURLConnection connection = (HttpsURLConnection) urlObj.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
            dataOutputStream.writeBytes("text=" + URLEncoder.encode(input, "UTF-8") + "&lang=" + lang+"&options=1");

            InputStream response = connection.getInputStream();
            String json = new java.util.Scanner(response).nextLine();
            int start = json.indexOf("[");
            int end = json.indexOf("]");
            String translated = json.substring(start + 2, end - 1);
            return translated;
        }


        //Вызывается при запуске потока
        //метод execute();
        @Override
        protected Void doInBackground(Void... params) {

            try {
                publishProgress(translate(lang, inputText.getText().toString()));
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(e.getClass().getName(), e.getMessage());
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            translateText.setText(values[0]);
            translateInputText.setText(inputText.getText());
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            yAPI = new YandexAPITask();
            yAPI.execute();
        }
    }

}