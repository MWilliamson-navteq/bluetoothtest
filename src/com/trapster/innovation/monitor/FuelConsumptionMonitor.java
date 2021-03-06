package com.trapster.innovation.monitor;

import com.trapster.innovation.comms.ELMCommunicator;
import com.trapster.innovation.comms.ELMJobCallback;
import com.trapster.innovation.comms.obd.OBDCommand;
import com.trapster.innovation.comms.obd.engine.MAPCommand;
import com.trapster.innovation.comms.obd.engine.RPMCommand;
import com.trapster.innovation.comms.obd.engine.VehicleSpeedCommand;
import com.trapster.innovation.comms.obd.fuel.MAFAIrFlowRateCommand;

public class FuelConsumptionMonitor extends OBDMonitor
{
    private MAFAIrFlowRateCommand mafaIrFlowRateCommand = new MAFAIrFlowRateCommand(new MAFCommandCallback());
    private VehicleSpeedCommand vehicleSpeedCommand = new VehicleSpeedCommand(new VehicleSpeedCallback());
    private MAPCommand mapCommand = new MAPCommand(new MAPCallback());
    private RPMCommand rpmCommand = new RPMCommand(new RPMCallback());

    private double currentConsumption = -1; // MPG
    private double MAFRate = -1;
    private double vehicleSpeed = -1;
    private double manifoldPressure = -1;
    private double rpm = -1;


    // Constants
    private final static double IDEAL_AIR_FUEL_RATIO = 14.7; // grams of air :: grams of gas
    private final static double DENSITY_OF_GASOLINE = 6.17; // pounds per gallon
    private final static double GRAMS_PER_POUND = 4.54;
    private final static double KPH_TO_MPH_CONVERSION = 0.621371;
    private final static double SECONDS_PER_HOUR = 3600;
    private final static double MAF_GRAMS_PER_SECOND = 100;

    public FuelConsumptionMonitor(ELMCommunicator communicator, OBDMonitorCallback callback)
    {
        super(communicator, callback);
    }

    @Override
    public double getResult()
    {
        return vehicleSpeed;
    }

    @Override
    public void run()
    {
        communicator.queueJob(mafaIrFlowRateCommand);
        communicator.queueJob(vehicleSpeedCommand);
        communicator.queueJob(mapCommand);
    }

    private void runCalculation()
    {
        if (MAFRate > 0)
            currentConsumption = (IDEAL_AIR_FUEL_RATIO * DENSITY_OF_GASOLINE * GRAMS_PER_POUND * vehicleSpeed * KPH_TO_MPH_CONVERSION) / (SECONDS_PER_HOUR * MAFRate / MAF_GRAMS_PER_SECOND);
        else
            currentConsumption = 0.0;
        callback.onResult(getResult());
    }

    private class MAFCommandCallback implements ELMJobCallback
    {
        @Override public void onError(String error) {callback.onError(error);} @Override public void onProgressUpdate(String progress){}

        @Override
        public void onComplete(OBDCommand command)
        {
            MAFRate = command.getValue();
            if (vehicleSpeed != -1)
                runCalculation();
        }



    }

    private class VehicleSpeedCallback implements ELMJobCallback
    {
        @Override public void onError(String error) {callback.onError(error);} @Override public void onProgressUpdate(String progress){}

        @Override
        public void onComplete(OBDCommand command)
        {
            vehicleSpeed = command.getValue();
            if (MAFRate != -1)
                runCalculation();
        }
    }

    private class MAPCallback implements ELMJobCallback
    {
        @Override public void onError(String error) {callback.onError(error);} @Override public void onProgressUpdate(String progress){}

        @Override
        public void onComplete(OBDCommand command)
        {
            manifoldPressure = command.getValue();
            if (manifoldPressure != -1)
                runCalculation();
        }
    }

    private class RPMCallback implements ELMJobCallback
    {
        @Override public void onError(String error) {callback.onError(error);} @Override public void onProgressUpdate(String progress){}

        @Override
        public void onComplete(OBDCommand command)
        {
            rpm = command.getValue();
            if (rpm != -1)
                runCalculation();
        }
    }
}
