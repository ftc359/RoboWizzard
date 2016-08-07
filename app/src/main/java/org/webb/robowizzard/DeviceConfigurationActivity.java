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
import android.view.inputmethod.EditorInfo;
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
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_configuration);
        setSupportActionBar((android.support.v7.widget.Toolbar) findViewById(R.id.toolbar));
        listView = (ListView) findViewById(R.id.deviceList);
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
                serialNumber.setHint(R.string.device_name);
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
                    listView.setAdapter(new DeviceAdapter(this, controller.getDevices(), Constants.MOTOR));
                    break;
                case LEGACY_MODULE_CONTROLLER:
                    listView.setAdapter(new DeviceAdapter(this, controller.getDevices(), Constants.LEGACY));
                    break;
                case DEVICE_INTERFACE_MODULE:
                    isDIM = true;
                    String deviceType = getIntent().getStringExtra("DEVICE_TYPE");
                    DeviceInterfaceModuleConfiguration controllerDIM = (DeviceInterfaceModuleConfiguration) controller;
                    if(deviceType != null) {
                        switch (deviceType) {
                            case DIMOptionAdapter.PWM_DEVICES:
                                listView.setAdapter(new DeviceAdapter(this, controllerDIM.getPwmDevices(), Constants.MOTOR));
                                break;
                            case DIMOptionAdapter.I2C_DEVICES:
                                listView.setAdapter(new DeviceAdapter(this, controllerDIM.getI2cDevices(), Constants.I2C_DEVICE));
                                break;
                            case DIMOptionAdapter.ANALOG_INPUT_DEVICES:
                                listView.setAdapter(new DeviceAdapter(this, controllerDIM.getAnalogInputDevices(), Constants.ANALOG_INPUT));
                                break;
                            case DIMOptionAdapter.DIGITAL_DEVICES:
                                listView.setAdapter(new DeviceAdapter(this, controllerDIM.getDigitalDevices(), Constants.DIGITAL_DEVICE));
                                break;
                            case DIMOptionAdapter.ANALOG_OUTPUT_DEVICES:
                                listView.setAdapter(new DeviceAdapter(this, controllerDIM.getAnalogOutputDevices(), Constants.ANALOG_OUTPUT));
                                break;
                            default:
                                break;
                        }
                    }
                    else {
                        parent.removeView(findViewById(R.id.categories));
                        listView.setAdapter(new DIMOptionAdapter(this, (DeviceInterfaceModuleConfiguration) controller, controllerIndex));
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
