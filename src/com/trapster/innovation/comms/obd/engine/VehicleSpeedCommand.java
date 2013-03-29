package com.trapster.innovation.comms.obd.engine;

import com.trapster.innovation.comms.ELMJobCallback;
import com.trapster.innovation.comms.obd.OBDCommand;

public class VehicleSpeedCommand extends OBDCommand
{
    private double speed = -1;
    public VehicleSpeedCommand(ELMJobCallback callback)
    {
        super("01 0D", callback);
    }

    @Override
    protected void processBuffer()
    {
        speed = singleByteBuffer.get(4);
    }

    @Override
    public String getResult()
    {
        if (speed != -1)
            return "Current speed is " + speed;
        return null;
    }

    @Override
    public double getValue()
    {
        return speed;
    }
}
