package com.trapster.innovation.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
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

public class BluetoothManager
{
    private  Activity activity;
    private BluetoothAdapter adapter;
    public final static int REQUEST_ENABLE_BT = 1;

    private boolean readyToUse = false;
    private BluetoothSocket clientSocket;

    private ELMCommunicator communicator;
    private FuelConsumptionMonitor fuelConsumptionMonitor;

    // Shouldn't be here
    private String currentConsumption = "";

    public BluetoothManager(Activity activity)
    {
        this.activity = activity;
        adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter();
        checkBluetooth();
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
                //updateStatusText("Checking for bluetooth");
                activity.startActivityForResult(new Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);

            }

        }
    }

    public void findDevices()
    {
        readyToUse = true;
        //updateStatusText("Finding Devices");
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
                    //updateStatusText("Setting up ELM Communicator");
                    clientSocket.connect();
                    communicator = new ELMCommunicator(clientSocket);
                    communicator.queueJob(new SelectAutoProtocolCommand(new ELMJobCallback()
                    {
                        @Override
                        public void onError(String error)
                        {
                            //updateStatusText(error);
                        }

                        @Override
                        public void onComplete(OBDCommand command)
                        {
                            //updateText(command.getResult());
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
                            currentConsumption = "Current consumption is: " + result;
                        }

                        @Override
                        public void onError(String error)
                        {
                            //updateStatusText(error);
                        }


                    });
                    //updateStatusText("Communicator is ready");

                    while (!activity.isFinishing())
                    {
                        fuelConsumptionMonitor.run();
                        Thread.sleep(1000);
                    }

                    clientSocket.close();
                }
                catch (IOException e)
                {
                    //updateStatusText("Socket error");
                    e.printStackTrace();
                }
                catch (InterruptedException e)
                {
                    //updateStatusText("Interrupted Exception");
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(null, null);
    }

    public boolean supportBluetooth()
    {
        return adapter != null;
    }

    public String getCurrentConsumption()
    {
        return currentConsumption;
    }

    public void closeSocket() throws IOException
    {
        if (clientSocket != null)
            clientSocket.close();
    }
}
