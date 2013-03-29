package com.trapster.innovation.comms.obd.engine;

import com.trapster.innovation.comms.ELMJobCallback;
import com.trapster.innovation.comms.obd.OBDCommand;

public class RPMCommand extends OBDCommand
{
    private double engineRPM = -1;
    public RPMCommand(ELMJobCallback callback)
    {
        super("01, 0C", callback);
    }

    @Override
    protected void processBuffer()
    {
        // Response should be 41 0C XX

        double firstValue = singleByteBuffer.get(4);
        double secondValue = singleByteBuffer.get(5);

        engineRPM = ((firstValue * 256) + secondValue) / 4.0;
    }

    @Override
    public String getResult()
    {
        if (engineRPM != -1)
            return "Current RPM is " + engineRPM;
        else
            return null;
    }

    @Override
    public double getValue()
    {
        return engineRPM;
    }
}
