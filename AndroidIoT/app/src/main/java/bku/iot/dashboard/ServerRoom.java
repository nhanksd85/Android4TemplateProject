package bku.iot.dashboard;

import androidx.appcompat.app.AppCompatActivity;
import at.grabner.circleprogress.CircleProgressView;

import android.os.Bundle;

public class ServerRoom extends AppCompatActivity {

    CircleProgressView txtTemperature;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_room);

        txtTemperature = (CircleProgressView) findViewById(R.id.txtTemperature);

        txtTemperature.setValue(Float.parseFloat("40.34"));

    }
}