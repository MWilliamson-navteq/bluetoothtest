package com.trapster.innovation.comms.obd;

import com.trapster.innovation.comms.ELMJobCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public abstract class OBDCommand
{
    private String command;
    protected String rawData;
    private ELMJobCallback callback;
    private final static long INPUT_TIMEOUT = 4000;
    protected ArrayList<Integer> doubleByteBuffer = new ArrayList<Integer>();
    protected ArrayList<Integer> singleByteBuffer = new ArrayList<Integer>();

    public OBDCommand(String command, ELMJobCallback callback)
    {
        this.command = command;
        this.callback = callback;
    }

    public void run(InputStream in, OutputStream out)
    {

        try
        {
            sendCommand(out);
            readResult(in);
            processBuffer();
            if (callback != null)
                callback.onComplete(this);
        }
        catch (IOException e)
        {
            if (callback != null)
                callback.onError(e.toString());
        }
        catch (InterruptedException e)
        {
            if (callback != null)
                callback.onError(e.toString());
        }
        catch (Exception e)
        {
            if (callback != null)
                callback.onError(e.toString());
        }

    }

    protected void sendCommand(OutputStream out) throws IOException, InterruptedException
    {
        // add the carriage return char
        command += "\r";

        // write to OutputStream, or in this case a BluetoothSocket
        out.write(command.getBytes());
        out.flush();

        Thread.sleep(200);
    }

    protected void readResult(InputStream in) throws IOException
    {
        byte b = 0;
        StringBuilder res = new StringBuilder();
        long currentTime = System.currentTimeMillis();
        long startTime = currentTime;

        // read until '>' arrives
        while ((startTime + INPUT_TIMEOUT > currentTime) && (char) (b = (byte) in.read()) != '>')
        {
            if ((char) b != ' ')
            {
                res.append((char) b);
            }

            currentTime = System.currentTimeMillis();
        }


        /*
           * Imagine the following response 41 0c 00 0d.
           *
           * ELM sends strings!! So, ELM puts spaces between each "byte". And pay
           * attention to the fact that I've put the word byte in quotes, because
           * 41 is actually TWO bytes (two chars) in the socket. So, we must do
           * some more processing..
           */
        //
        rawData = res.toString().trim();

        // clear buffer
        doubleByteBuffer.clear();

        // Parse out the double-byte strings and decode them
        int begin = 0;
        int end = 2;

        // Parse out the single-byte strings and decode them
        for (int i = 0; i < rawData.length(); i++)
        {
            String temp = "0x" + rawData.charAt(i);
            singleByteBuffer.add(Integer.decode(temp));
        }

        while (end <= rawData.length())
        {
            String temp = "0x" + rawData.substring(begin, end);
            doubleByteBuffer.add(Integer.decode(temp));
            begin = end;
            end += 2;
        }


    }

    protected abstract void processBuffer();
    public abstract String getResult();
    public abstract double getValue();
}
