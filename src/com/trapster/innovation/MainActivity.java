package com.trapster.innovation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import com.trapster.innovation.bluetooth.BluetoothManager;

import java.io.IOException;

public class MainActivity extends Activity
{
    private Handler handler = new Handler(Looper.getMainLooper());

    private TextView textView;
    private TextView statusText;

    private BluetoothManager bluetoothManager;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        textView = (TextView)findViewById(R.id.textView);
        statusText = (TextView)findViewById(R.id.statusText);

        updateText("No data received");

        bluetoothManager = new BluetoothManager(this);

        handler.post(updateConsumptionRunnable);
    }

    @Override
    public void onBackPressed()
    {
        try
        {
            bluetoothManager.closeSocket();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        super.onBackPressed();

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case BluetoothManager.REQUEST_ENABLE_BT:
            {
                handleBluetoothResult(resultCode, data);
                break;
            }
        }
    }

    private void handleBluetoothResult(int resultCode, Intent data)
    {
        switch (resultCode)
        {
            case RESULT_OK:
            {
                updateStatusText("Bluetooth Ready");
                bluetoothManager.findDevices();
                break;
            }

        }
    }

    private void updateStatusText(final String text)
    {
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                statusText.setText(text);
            }
        });
    }

    private void updateText(final String text)
    {
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                textView.append(text);
            }
        });
    }

    private Runnable updateConsumptionRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            updateStatusText(bluetoothManager.getCurrentConsumption());
            handler.postDelayed(this, 1000);
        }
    };
}
