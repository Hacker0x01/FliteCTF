package com.hackerone.thermostat;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.hackerone.thermostat.Model.ThermostatModel;
import com.hackerone.thermostat.Vendor.ColorArcProgressBar;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.ViewModelProviders;

public class ThermostatActivity extends AppCompatActivity {


    private LinearLayout thermostatContainer = null;
    private ImageButton minusBtn = null;
    private ImageButton plusBtn = null;
    private DiscreteSeekBar tempSlider = null;
    private ColorArcProgressBar thermostatCooling = null;
    private ColorArcProgressBar thermostatCoolingOff = null;

    public static RequestQueue volleyQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        volleyQueue = Volley.newRequestQueue(this);
        setContentView(R.layout.activity_thermostat);
        final ThermostatModel model = ViewModelProviders.of(this).get(ThermostatModel.class);
        bindViews();
        configureListeners(model);
        observeModel(model);
        try {
            setDefaults(model);
        } catch (Exception e) {
        }
    }

    private void bindViews() {
        minusBtn = findViewById(R.id.button_minus);
        plusBtn = findViewById(R.id.button_plus);
        tempSlider = findViewById(R.id.temp_slider);
        thermostatCooling = findViewById(R.id.thermostat_cooling);
        thermostatCoolingOff = findViewById(R.id.thermostat_cooling_off);
        thermostatContainer = findViewById(R.id.thermostat_container);
    }

    private void configureListeners(ThermostatModel model) {
        minusBtn.setOnClickListener(e -> model.decreaseTemperature());
        plusBtn.setOnClickListener(e -> model.increaseTemperature());


        tempSlider.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {

            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                model.setTargetTemperature(value);
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
            }
        });
    }

    private void observeModel(ThermostatModel model) {
        model.getTargetTemperature().observe(this, temp -> {
            thermostatCooling.setCurrentValues(temp);
            thermostatCoolingOff.setCurrentValues(temp);
            if (tempSlider.getProgress() != temp) {
                tempSlider.setProgress(temp);
            }
        });

        model.getIsCooling().observe(this, isCooling -> {
            if (isCooling) {
                getWindow().getDecorView().setBackgroundResource(R.color.colorPrimary);
                thermostatContainer.setBackgroundResource(R.drawable.cooling_circle);
                thermostatCooling.setVisibility(View.VISIBLE);
                thermostatCoolingOff.setVisibility(View.GONE);
            } else {
                getWindow().getDecorView().setBackgroundResource(R.color.colorPrimaryDark);
                thermostatContainer.setBackgroundResource(R.drawable.cooling_off_circle);
                thermostatCooling.setVisibility(View.GONE);
                thermostatCoolingOff.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setDefaults(ThermostatModel model) throws Exception {
        model.setTargetTemperature(77);
        model.setCurrentTemperature(76);
        JSONObject payload = new JSONObject();
        payload.put("username", LoginActivity.username);
        payload.put("password", LoginActivity.password);
        payload.put("cmd", "getTemp");
        volleyQueue.add(new PayloadRequest(payload, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                model.setTargetTemperature(70);
                model.setCurrentTemperature(73);
            }
        }));
    }
}
