package com.trapster.innovation;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import com.trapster.innovation.comms.ELMCommunicator;
import com.trapster.innovation.comms.ELMJobCallback;
import com.trapster.innovation.comms.obd.OBDCommand;
import com.trapster.innovation.comms.obd.protocol.SelectAutoProtocolCommand;
import com.trapster.innovation.monitor.FuelConsumptionMonitor;
import com.trapster.innovation.monitor.OBDMonitor;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity
{

    private BluetoothAdapter adapter;
    private final static int REQUEST_ENABLE_BT = 1;

    private boolean readyToUse = false;
    private BluetoothSocket clientSocket;
    private Handler handler = new Handler(Looper.getMainLooper());

    private TextView textView;
    private TextView statusText;

    private ELMCommunicator communicator;
    private FuelConsumptionMonitor fuelConsumptionMonitor;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter();
        textView = (TextView)findViewById(R.id.textView);
        statusText = (TextView)findViewById(R.id.statusText);

        updateText("No data received");

        checkBluetooth();
    }

    @Override
    public void onBackPressed()
    {
        try
        {
            clientSocket.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        super.onBackPressed();

    }

    public boolean supportBluetooth()
    {
        return adapter != null;
    }

    private void checkBluetooth()
    {
        if (supportBluetooth())
        {
            if (adapter.isEnabled())
            {
                findDevices();
            }
            else
            {
                updateStatusText("Checking for bluetooth");
                startActivityForResult(new Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);

            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case REQUEST_ENABLE_BT:
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
                readyToUse = true;
                findDevices();
                break;
            }

        }
    }

    private void findDevices()
    {
        updateStatusText("Finding Devices");
        Set<BluetoothDevice> devices = adapter.getBondedDevices();
        Iterator<BluetoothDevice> i = devices.iterator();
        while (i.hasNext())
        {
            BluetoothDevice device = i.next();
            try
            {
                clientSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                if (clientSocket != null)
                    setupClientSocket();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void setupClientSocket()
    {
        new AsyncTask<Void, Void, Void>()
        {

            @Override
            protected Void doInBackground(Void... voids)
            {
                try
                {
                    updateStatusText("Setting up ELM Communicator");
                    clientSocket.connect();
                    communicator = new ELMCommunicator(clientSocket);
                    communicator.queueJob(new SelectAutoProtocolCommand(new ELMJobCallback()
                    {
                        @Override
                        public void onError(String error)
                        {
                            updateStatusText(error);
                        }

                        @Override
                        public void onComplete(OBDCommand command)
                        {
                            updateText(command.getResult());
                        }

                        @Override
                        public void onProgressUpdate(String progress)
                        {
                        }
                    }));

                    fuelConsumptionMonitor = new FuelConsumptionMonitor(communicator, new OBDMonitor.OBDMonitorCallback()
                    {
                        @Override
                        public void onResult(final double result)
                        {
                            handler.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    updateStatusText("Current consumption is: " + result);
                                }
                            });
                        }

                        @Override
                        public void onError(String error)
                        {
                            updateStatusText(error);
                        }


                    });
                    updateStatusText("Communicator is ready");

                    while (!isFinishing())
                    {
                        fuelConsumptionMonitor.run();
                        Thread.sleep(1000);
                    }
                }
                catch (IOException e)
                {
                    updateStatusText("Socket error");
                    e.printStackTrace();
                }
                catch (InterruptedException e)
                {
                    updateStatusText("Interrupted Exception");
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(null, null);
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
}
