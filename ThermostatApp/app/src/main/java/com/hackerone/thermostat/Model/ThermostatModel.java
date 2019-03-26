package com.hackerone.thermostat.Model;

import com.android.volley.Response;
import com.hackerone.thermostat.LoginActivity;
import com.hackerone.thermostat.PayloadRequest;
import com.hackerone.thermostat.ThermostatActivity;

import org.json.JSONObject;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ThermostatModel extends ViewModel {
    private MutableLiveData<Integer> targetTemperature;
    private MutableLiveData<Integer> currentTemperature;
    private MutableLiveData<Boolean> isCooling;


    public ThermostatModel() {
        super();
        targetTemperature = new MutableLiveData<>();
        currentTemperature = new MutableLiveData<>();
        isCooling = new MutableLiveData<>();
    }

    public void increaseTemperature(){
        Integer value = this.targetTemperature.getValue();
        if (value != null)
            setTargetTemperature(value + 1);
    }

    public void decreaseTemperature() {
        Integer value = this.targetTemperature.getValue();
        if (value != null)
            setTargetTemperature(value - 1);
    }

    public void setTargetTemperature(Integer targetTemperature){
        this.targetTemperature.setValue(targetTemperature);
        try {
            JSONObject payload = new JSONObject();
            payload.put("username", LoginActivity.username);
            payload.put("password", LoginActivity.password);
            payload.put("cmd", "setTemp");
            payload.put("temp", targetTemperature);
            ThermostatActivity.volleyQueue.add(new PayloadRequest(payload, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {}
            }));
        } catch(Exception e) {
        }
        updateCooling();
    }

    public void setCurrentTemperature(Integer currentTemperature){
        this.currentTemperature.setValue(currentTemperature);

        updateCooling();
    }

    private void updateCooling() {
        if(this.targetTemperature.getValue() == null || this.currentTemperature.getValue() == null)
            return;
        boolean tcool = this.targetTemperature.getValue() < this.currentTemperature.getValue();
        if(this.isCooling.getValue() == null || tcool != this.isCooling.getValue())
            this.isCooling.setValue(tcool);
    }

    public MutableLiveData<Integer> getTargetTemperature() {
        return targetTemperature;
    }

    public MutableLiveData<Integer> getCurrentTemperature() {
        return currentTemperature;
    }

    public MutableLiveData<Boolean> getIsCooling() {
        return isCooling;
    }
}
