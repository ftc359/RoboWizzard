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
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.qualcomm.robotcore.hardware.configuration.DeviceConfiguration;
import com.qualcomm.robotcore.hardware.configuration.DeviceConfiguration.ConfigurationType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class DeviceAdapter extends BaseAdapter {
    Activity activity;
    List<DeviceConfiguration> deviceList;
    public ConfigurationType[] typeList;
    private int controllerNumber;

    public DeviceAdapter(Activity activity, List<DeviceConfiguration> deviceList, ConfigurationType[] typeList) {
        this.activity = activity;
        this.deviceList = deviceList;
        this.typeList = typeList;
        controllerNumber = 0;
    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public Object getItem(int position) {
        if(typeList != ConfigurationConstants.MOTOR) {
            return deviceList.get(deviceList.size() - position - 1);
        }
        return deviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return ((DeviceConfiguration) getItem(position)).getPort();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private static class ViewHolder {
        TextView port;
        CheckBox enabled;
        BetterEditText name;
        Button editController;
        Spinner spinner;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final DeviceConfiguration device = (DeviceConfiguration) getItem(position);
        final ViewHolder viewHolder;
        if(convertView == null) {
            viewHolder = new ViewHolder();
            if(typeList == ConfigurationConstants.MOTOR) {
                convertView = activity.getLayoutInflater().inflate(R.layout.item_device_motor_edit, parent, false);
            }
            else {
                convertView = activity.getLayoutInflater().inflate(R.layout.item_device_sensor_and_controller_edit, parent, false);
            }

            //GENERAL
            viewHolder.port = (TextView) convertView.findViewById(R.id.portNumber);
            viewHolder.name = (BetterEditText) convertView.findViewById(R.id.portName);
            //END OF GENERAL

            if(typeList == ConfigurationConstants.MOTOR) {
                viewHolder.enabled = (CheckBox) convertView.findViewById(R.id.portEnabled);
            }
            else {
                LinearLayout linearLayout = (LinearLayout) convertView.findViewById(R.id.parent);
                linearLayout.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                viewHolder.editController = (Button) convertView.findViewById(R.id.editControllerButton);
                viewHolder.editController.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, linearLayout.getMeasuredHeight() + 2*linearLayout.getPaddingStart()));

                viewHolder.spinner = (Spinner) convertView.findViewById(R.id.spinner);
            }
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //GENERAL
        viewHolder.port.setText(String.format(Locale.ENGLISH, "%d", device.getPort()));
        viewHolder.name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus) {
                    BetterEditText editText = (BetterEditText) view;
                    String name = editText.getText().toString();
                    device.setName(name);
                    if(name.equals(DeviceConfiguration.DISABLED_DEVICE_NAME)) {
                        editText.setText("");
                    }
                }
            }
        });
        if(!device.getName().equals(DeviceConfiguration.DISABLED_DEVICE_NAME)) {
            viewHolder.name.setText(device.getName());
        }
        else {
            viewHolder.name.setText("");
        }
        viewHolder.name.setEnabled(device.isEnabled() && !ConfigurationConstants.partOfGroup(device.getType(), ConfigurationConstants.CONTROLLER));
        //END OF GENERAL

        if(typeList == ConfigurationConstants.MOTOR) {
            viewHolder.enabled.setChecked(device.isEnabled());
            viewHolder.enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    device.setEnabled(compoundButton.isEnabled() && checked);
                    viewHolder.name.setEnabled(device.isEnabled());
                    if(!device.isEnabled()) {
                        viewHolder.name.setText("");
                        device.setName(DeviceConfiguration.DISABLED_DEVICE_NAME);
                    }
                }
            });
        }
        else {
            viewHolder.editController.setEnabled(device.isEnabled() && ConfigurationConstants.partOfGroup(device.getType(), ConfigurationConstants.CONTROLLER));
            viewHolder.editController.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(activity, "WORKING " + device.getPort(), Toast.LENGTH_SHORT).show();
                    //TODO: Start new DeviceConfigurationActivity
                }
            });

            List<ConfigurationType> options = new ArrayList<ConfigurationType>(Arrays.asList(typeList));
            viewHolder.spinner.setAdapter(new ArrayAdapter<ConfigurationType>(activity, R.layout.support_simple_spinner_dropdown_item, options)); //TODO: make spinner item layout
            viewHolder.spinner.setSelection(ConfigurationConstants.typeIndex(device.getType(), typeList));
            viewHolder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    ConfigurationType type = (ConfigurationType) parent.getItemAtPosition(position);
                    device.setType(type);
                    if(type == ConfigurationType.NOTHING) {
                        device.setName(DeviceConfiguration.DISABLED_DEVICE_NAME);
                        device.setEnabled(false);
                        viewHolder.name.setText("");
                    }
                    else {
                        device.setEnabled(true);
                    }
                    viewHolder.name.setEnabled(device.isEnabled() && !ConfigurationConstants.partOfGroup(type, ConfigurationConstants.CONTROLLER));
                    viewHolder.name.setClickable(viewHolder.name.isEnabled());
                    viewHolder.editController.setEnabled(device.isEnabled() && ConfigurationConstants.partOfGroup(device.getType(), ConfigurationConstants.CONTROLLER));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        if(getCount() - 1 == position) { //Bottommost one
            viewHolder.name.setImeOptions(EditorInfo.IME_ACTION_DONE);
        }
        else {
            viewHolder.name.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        }
        return convertView;
    }
}
