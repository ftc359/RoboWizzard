/*
 * Copyright (c) 2014, 2015 Qualcomm Technologies Inc
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of Qualcomm Technologies Inc nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.webb.robowizzard;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.qualcomm.robotcore.hardware.configuration.ControllerConfiguration;
import com.qualcomm.robotcore.hardware.configuration.DeviceConfiguration;
import com.qualcomm.robotcore.hardware.configuration.DeviceInterfaceModuleConfiguration;
import com.qualcomm.robotcore.util.SerialNumber;

import java.util.List;

public class DeviceConfigurationActivity extends BaseActivity {
    private BetterEditText name, serialNumber;
    private int controllerIndex;
    private ControllerConfiguration controller;
    private List<DeviceConfiguration> deviceList;
    private boolean isDIM;
    private LinearLayout list;
    private DeviceLayout deviceLayout;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_configuration);
        setSupportActionBar((android.support.v7.widget.Toolbar) findViewById(R.id.toolbar));
        saveButton = (Button) findViewById(R.id.toolbar_save);
        list = (LinearLayout) findViewById(R.id.deviceList);
        isDIM = false;

        controllerIndex = getIntent().getIntExtra("CONTROLLER", -1);
        try {
            controller = current.get(controllerIndex);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            controller = null;
        }
        if(controller != null) {
            LinearLayout parent = (LinearLayout) findViewById(R.id.activity_device_configuration);
            if(!launchedBy(DeviceConfigurationActivity.class)) { //NOT a subcontroller of a legacy module
                serialNumber = new BetterEditText(this);
                serialNumber.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                serialNumber.setSingleLine();
                serialNumber.setMaxLines(1);
                serialNumber.setImeOptions(EditorInfo.IME_ACTION_DONE);
                serialNumber.setText(controller.getSerialNumber().toString());
                serialNumber.setHint(R.string.serial_number);
                serialNumber.setTypeface(Typeface.MONOSPACE);
                serialNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean b) {
                        if(!b) {
                            controller.getSerialNumber().setSerialNumber(((BetterEditText) view).getText().toString());
                        }
                    }
                });
                parent.addView(serialNumber, 3);
            }
            name = (BetterEditText) new BetterEditText(this);
            name.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            name.setSingleLine();
            name.setMaxLines(1);
            name.setText(controller.getName());
            name.setHint(R.string.controller_name);
            name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if(!b) {
                        controller.setName(((BetterEditText) view).getText().toString());
                    }
                }
            });
            parent.addView(name, 3);
            switch(controller.getType()) {
                case MOTOR_CONTROLLER:
                case SERVO_CONTROLLER:
                case MATRIX_CONTROLLER:
                    deviceLayout = new DeviceLayout(this, list, controller.getDevices(), ConfigurationConstants.MOTOR, !running);
                    break;
                case LEGACY_MODULE_CONTROLLER:
                    deviceLayout = new DeviceLayout(this, list, controller.getDevices(), ConfigurationConstants.LEGACY, !running);
                    break;
                case DEVICE_INTERFACE_MODULE:
                    isDIM = true;
                    String deviceType = getIntent().getStringExtra("DEVICE_TYPE");
                    DeviceInterfaceModuleConfiguration controllerDIM = (DeviceInterfaceModuleConfiguration) controller;
                    if(deviceType != null) {
                        switch (deviceType) {
                            case DIMOptionAdapter.PWM_DEVICES:
                                deviceLayout = new DeviceLayout(this, list, controllerDIM.getPwmDevices(), ConfigurationConstants.MOTOR, !running);
                                break;
                            case DIMOptionAdapter.I2C_DEVICES:
                                deviceLayout = new DeviceLayout(this, list, controllerDIM.getI2cDevices(), ConfigurationConstants.I2C_DEVICE, !running);
                                break;
                            case DIMOptionAdapter.ANALOG_INPUT_DEVICES:
                                deviceLayout = new DeviceLayout(this, list, controllerDIM.getAnalogInputDevices(), ConfigurationConstants.ANALOG_INPUT, !running);
                                break;
                            case DIMOptionAdapter.DIGITAL_DEVICES:
                                deviceLayout = new DeviceLayout(this, list, controllerDIM.getDigitalDevices(), ConfigurationConstants.DIGITAL_DEVICE, !running);
                                break;
                            case DIMOptionAdapter.ANALOG_OUTPUT_DEVICES:
                                deviceLayout = new DeviceLayout(this, list, controllerDIM.getAnalogOutputDevices(), ConfigurationConstants.ANALOG_OUTPUT, !running);
                                break;
                            default:
                                break;
                        }
                    }
                    else {
                        parent.removeView(findViewById(R.id.categories));
                        parent.removeView(findViewById(R.id.scrollView));
                        ListView listView = new ListView(this);
                        listView.setAdapter(new DIMOptionAdapter(this, (DeviceInterfaceModuleConfiguration) controller, controllerIndex));
                        listView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0F));
                        parent.addView(listView);
                    }
            }
        }
    }

    public void update() {
        if(controller != null) {
            controller.setName(name.getText().toString());
            if(!launchedBy(DeviceConfigurationActivity.class)) controller.getSerialNumber().setSerialNumber(serialNumber.getText().toString());
        }
    }

    public void run() {
        super.run();
        AlphaAnimation animation = new AlphaAnimation(running?1.0F:0.0F, running?0.0F:1.0F);
        animation.setDuration(1000);
        animation.setAnimationListener(new Animation.AnimationListener() { //Placed here so it only activates once
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                int visibility = running?View.INVISIBLE:View.VISIBLE;
                saveButton.setVisibility(visibility);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        saveButton.startAnimation(animation);

        if(running) {
            clearFocus();
        }
        name.setClickable(!running);
        name.setFocusable(!running);
        name.setFocusableInTouchMode(!running);
        name.setCursorVisible(!running);
        serialNumber.setClickable(!running);
        serialNumber.setFocusable(!running);
        serialNumber.setFocusableInTouchMode(!running);
        serialNumber.setCursorVisible(!running);
        deviceLayout.setEdit(!running);
    }

    public void save(View v) {
        clearFocus();
        update();
        if(current.contains(new SerialNumber(DEFAULT_SERIAL_NUMBER))) {
            AlertDialog.Builder builder = util.buildBuilder("File Not Saved", "Please change the serial number(s) from the default.");
            builder.setNeutralButton("Ok", dummyListener);
            builder.show();
            return;
        }
        if(current.getFilename().equals("")) {
            AlertDialog.Builder builder = util.buildBuilder("File Not Saved", "Please change the layout name from the default.");
            builder.setNeutralButton("Ok", dummyListener);
            builder.show();
            return;
        }
        save();
    }

    @Override
    public void onBackPressed() {
        update();
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_horizontal);
    }
}
