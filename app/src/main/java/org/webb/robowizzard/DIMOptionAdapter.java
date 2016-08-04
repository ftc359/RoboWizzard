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

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.qualcomm.robotcore.hardware.configuration.DeviceConfiguration;
import com.qualcomm.robotcore.hardware.configuration.DeviceInterfaceModuleConfiguration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DIMOptionAdapter extends BaseAdapter {
    private final static String PWM_DEVICES = "PWM Devices";
    private final static String I2C_DEVICES = "I2C Devices";
    private final static String ANALOG_INPUT_DEVICES = "Analog Input Devices";
    private final static String DIGITAL_DEVICES = "Digital Devices";
    private final static String ANALOG_OUTPUT_DEVICES = "Analog Output Devices";

    private Activity activity;
    private DeviceInterfaceModuleConfiguration controller;
    private List<String> optionList;

    public DIMOptionAdapter(Activity activity, DeviceInterfaceModuleConfiguration controller) {
        this.activity = activity;
        this.controller = controller;
        optionList = new ArrayList<String>();

        optionList.add(PWM_DEVICES);
        optionList.add(I2C_DEVICES);
        optionList.add(ANALOG_INPUT_DEVICES);
        optionList.add(DIGITAL_DEVICES);
        optionList.add(ANALOG_OUTPUT_DEVICES);
    }

    @Override
    public int getCount() {
        return optionList.size();
    }

    @Override
    public Object getItem(int position) {
        return optionList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Button optionButton = null;
        if(convertView == null) {
            convertView = activity.getLayoutInflater().inflate(R.layout.item_dim_options, parent, false);
            optionButton = (Button) convertView.findViewById(R.id.optionButton);
            optionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String option = ((Button) view).getText().toString();

                    switch(option) {
                        case PWM_DEVICES:
                            startOption(controller.getPwmDevices());
                            break;
                        case I2C_DEVICES:
                            startOption(controller.getI2cDevices());
                            break;
                        case ANALOG_INPUT_DEVICES:
                            startOption(controller.getAnalogInputDevices());
                            break;
                        case DIGITAL_DEVICES:
                            startOption(controller.getDigitalDevices());
                            break;
                        case ANALOG_OUTPUT_DEVICES:
                            startOption(controller.getAnalogOutputDevices());
                            break;
                        default:
                            break;
                    }
                }
            });
            convertView.setTag(optionButton);
        }
        else {
            optionButton = (Button) convertView.getTag();
        }

        optionButton.setText((String) getItem(position));

        return convertView;
    }

    private void startOption(List<DeviceConfiguration> deviceList) {
        Intent intent = new Intent(activity, DeviceConfigurationActivity.class);
        intent.putExtra("DEVICE_LIST", (Serializable) deviceList);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_horizontal, R.anim.fade_out);
    }
}
