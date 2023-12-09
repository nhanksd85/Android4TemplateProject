package bku.iot.dashboard;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

//https://github.com/singhangadin/android-toggle
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    MQTTHelper mqttHelper;
    int status = 0;
    int counter = 0;
    TextView txtChatGPT;
    Button btnUserName;
    EditText txtUserName;
    String username = "";
    ImageButton btnVoice;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtChatGPT = findViewById(R.id.txtConsole);
        txtChatGPT.setMovementMethod(new ScrollingMovementMethod());

        btnUserName = findViewById(R.id.btnUserName);
        btnUserName.setOnClickListener(this);
        txtUserName = findViewById(R.id.txtUserName);


        username = loadKey(this, "USERNAME");
        txtUserName.setText(username + "");


        btnVoice = findViewById(R.id.btnVoice);
        btnVoice.setOnClickListener(this);


        niceTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int initStatus) {
                if (initStatus == TextToSpeech.SUCCESS) {
                    niceTTS.setLanguage(Locale.forLanguageTag("VI"));
                    talkToMe("Xin chào các bạn, tôi là hệ thống trợ lý ảo nhân tạo dựa trên Chat gi pi ti");
                    txtChatGPT.setText("");
                }else{
                    Log.d("ChatGPT", "Init fail");
                }
            }
        });
        if (username.length() > 0)
            startMQTT(username);
    }

    public void startMQTT(String username){
        mqttHelper = new MQTTHelper(this, username, getRandomString(50));
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d("ChatGPT", topic + "  : " + message.toString());
                if(topic.contains(mqttHelper.username) == false)
                    return;


                if(topic.contains("/feeds/V8")){
                    talkToMe(message.toString());
                }else if(topic.contains("/feeds/V9")){
                    String msg = message.toString().toLowerCase();
                    getChatGPTAnswer(msg);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }


    public void saveKey(Activity activity, String key, String value) {
        if (key.isEmpty()) return;
        SharedPreferences settings = activity.getSharedPreferences("GPT", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String loadKey(Activity activity, String key) {
        SharedPreferences settings = activity.getSharedPreferences("GPT", Context.MODE_PRIVATE);
        return settings.getString(key, getRandomString(8));
    }

    private int DATA_CHECKING = 0;
    private TextToSpeech niceTTS;



    public void talkToMe(final String sentence) {

        txtChatGPT.setText(sentence);
        Log.d("mqtt", "Talk to me " + sentence);
        String speakWords = sentence;
        niceTTS.speak(speakWords, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btnUserName){
            saveKey(this,"USERNAME", txtUserName.getText().toString());
            startMQTT(txtUserName.getText().toString());
        }else if(view.getId() == R.id.btnVoice){
            startVoiceInput2();
        }
    }

    OkHttpClient client = new OkHttpClient();
    private void getChatGPTAnswer(String question){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtChatGPT.setText("Đang xử lý...");
                Log.d("ChatGPT", "Đang xử lý...");
            }
        });
        final Request request = new Request.Builder()
                .url("http://lpnserver.net:51087/test?c=" + question)
                .build();
        try {
            //Response response = client.newCall(request).execute();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {

                }

                @Override
                public void onResponse(Response response) throws IOException {
                    String msg = response.body().string();
                    Log.d("ChatGPT", msg);
                    talkToMe(msg);
                }
            });
        }catch (Exception e){}
    }



    private static final int REQ_CODE_SPEECH_INPUT = 100;
    public void startVoiceInput2() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,"vi-VN");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Xin mời nói...");
        //intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
//        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
//                "iot.bku.bkiot.chatgpt");
        try {
            //isProcessingSearch = true;
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
            //layoutHeader.setVisibility(View.INVISIBLE);
        } catch (ActivityNotFoundException a) {
            //isProcessingSearch = false;
            //layoutHeader.setVisibility(View.VISIBLE);
        }

    }
    public void startVoiceInput(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                "bku.iot.dashboard");

        SpeechRecognizer recognizer = SpeechRecognizer
                .createSpeechRecognizer(this.getApplicationContext());
        RecognitionListener listener = new RecognitionListener() {
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> voiceResults = results
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (voiceResults == null) {
                    Log.d("ChatGPT","No voice results");
                } else {
                    Log.d("ChatGPT","Printing matches: ");
                    for (String match : voiceResults) {
                        Log.d("ChatGPT", match);
                    }
                }
            }

            @Override
            public void onReadyForSpeech(Bundle params) {
                System.out.println("Ready for speech");
            }

            /**
             *  ERROR_NETWORK_TIMEOUT = 1;
             *  ERROR_NETWORK = 2;
             *  ERROR_AUDIO = 3;
             *  ERROR_SERVER = 4;
             *  ERROR_CLIENT = 5;
             *  ERROR_SPEECH_TIMEOUT = 6;
             *  ERROR_NO_MATCH = 7;
             *  ERROR_RECOGNIZER_BUSY = 8;
             *  ERROR_INSUFFICIENT_PERMISSIONS = 9;
             *
             * @param error code is defined in SpeechRecognizer
             */
            @Override
            public void onError(int error) {
                System.err.println("Error listening for speech: " + error);
            }

            @Override
            public void onBeginningOfSpeech() {
                System.out.println("Speech starting");
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onEndOfSpeech() {
                // TODO Auto-generated method stub

            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // TODO Auto-generated method stub

            }
        };
        recognizer.setRecognitionListener(listener);
        recognizer.startListening(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //do they have the data
        if(requestCode == REQ_CODE_SPEECH_INPUT){
            Log.d("mqtt", requestCode + "****" +resultCode );
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                Log.d("mqtt", result.get(0));

                if (result.size() > 0) {
                    String msg = result.get(0).toLowerCase().trim();
                    processV9(msg);
                }
            }else{
                //isProcessingSearch = false;
                Log.d("mqtt", "You are here");
                if(data != null){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Log.d("mqtt", result.get(0));
                }
            }
        }
    }
    public void processV9(String input){
        getChatGPTAnswer(input);
    }


    private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";

    private static String getRandomString(final int sizeOfRandomString)
    {
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(sizeOfRandomString);
        sb.append('I');
        sb.append('Y');
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }
}