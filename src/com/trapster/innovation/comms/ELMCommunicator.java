package com.trapster.innovation.comms;

import android.bluetooth.BluetoothSocket;
import com.trapster.innovation.comms.obd.OBDCommand;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ELMCommunicator
{
    private final static String ERROR_IO = "An I/O Error has occurred";
    private InputStream inputStream;
    private OutputStream outputStream;

    private final static int BUFFER_SIZE = 512;
    private int currentBufferSize = 0;
    private char[] buffer = new char[BUFFER_SIZE];

    private volatile boolean readyForJobs = false;

    private ExecutorService executor = Executors.newFixedThreadPool(1);
    private LinkedList<ObdCommandRunnable> queuedJobs = new LinkedList<ObdCommandRunnable>();

    public ELMCommunicator(BluetoothSocket clientSocket)
    {
        launchSocketHandler(clientSocket);
    }

    private void launchSocketHandler(final BluetoothSocket clientSocket)
    {

        try
        {
            if (clientSocket != null)
            {
                inputStream = clientSocket.getInputStream();
                outputStream = clientSocket.getOutputStream();

                //outputStream.write("AT Z".getBytes());  // Reset state

                readyForJobs = true;

                synchronized (queuedJobs)
                {
                    Iterator<ObdCommandRunnable> i = queuedJobs.iterator();
                    while (i.hasNext())
                    {
                        executor.submit(i.next());
                    }

                    queuedJobs.clear();
                }
            }


        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void queueJob(OBDCommand command)
    {
        if (readyForJobs && command != null)
            executor.submit(new ObdCommandRunnable(command));
        else
        {
            synchronized (queuedJobs)
            {
                queuedJobs.push(new ObdCommandRunnable(command));
            }

        }
    }

    private class ObdCommandRunnable implements Runnable
    {
        private OBDCommand command;

        public ObdCommandRunnable(OBDCommand command)
        {
            this.command = command;
        }

        @Override
        public void run()
        {
            command.run(inputStream, outputStream);
        }
    }
}
