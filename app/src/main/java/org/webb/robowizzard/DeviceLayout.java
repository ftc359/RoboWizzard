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
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.qualcomm.robotcore.hardware.configuration.DeviceConfiguration;
import com.qualcomm.robotcore.hardware.configuration.DeviceConfiguration.ConfigurationType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class DeviceLayout {
    private Activity activity;
    private LinearLayout parent;
    private List<DeviceConfiguration> deviceList;
    private ConfigurationType[] typeList;
    private boolean edit;

    public DeviceLayout(Activity activity, LinearLayout parent, List<DeviceConfiguration> deviceList, ConfigurationType[] typeList, boolean edit) {
        this.activity = activity;
        this.parent = parent;
        this.deviceList = deviceList;
        this.typeList = typeList;
        this.edit = edit;

        this.render();
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
        this.render();
    }

    private void render() {
        parent.removeAllViews();
        ScrollView scrollView = ((ScrollView) parent.getParent());
        int scrollLocation = scrollView.getScrollY();
        for(int i = 0; i < deviceList.size(); i++) {
            if(edit) {
                parent.addView(getEditView(i));
            }
            else {
                parent.addView(getEditView(i));
            }
        }
        scrollView.scrollTo(scrollView.getScrollX(), scrollLocation);
    }

    private static class EditViewHolder {
        TextView port;
        CheckBox enabled;
        BetterEditText name;
        Button editController;
        Spinner spinner;
        boolean ignoreFirstSelection;
    }
    
    private View getEditView(int position) {
        View row;
        final DeviceConfiguration device = deviceList.get(position);
        final EditViewHolder editViewHolder = new EditViewHolder();
        if (typeList == ConfigurationConstants.MOTOR) {
            row = activity.getLayoutInflater().inflate(R.layout.item_device_motor_edit, parent, false);
        } else {
            row = activity.getLayoutInflater().inflate(R.layout.item_device_sensor_and_controller_edit, parent, false);
        }

        //GENERAL
        editViewHolder.port = (TextView) row.findViewById(R.id.portNumber);
        editViewHolder.port.setText(String.format(Locale.ENGLISH, "%d", device.getPort()));

        editViewHolder.name = (BetterEditText) row.findViewById(R.id.portName);
        editViewHolder.name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    BetterEditText editText = (BetterEditText) view;
                    String name = editText.getText().toString();
                    device.setName(name);
                    if (name.equals(DeviceConfiguration.DISABLED_DEVICE_NAME)) {
                        editText.setText("");
                    }
                }
            }
        });
        if (!device.getName().equals(DeviceConfiguration.DISABLED_DEVICE_NAME)) {
            editViewHolder.name.setText(device.getName());
        } else {
            editViewHolder.name.setText("");
        }
        editViewHolder.name.setEnabled(device.isEnabled() && !ConfigurationConstants.partOfGroup(device.getType(), ConfigurationConstants.CONTROLLER));
        //END OF GENERAL

        if (typeList == ConfigurationConstants.MOTOR) {
            editViewHolder.enabled = (CheckBox) row.findViewById(R.id.portEnabled);
            editViewHolder.enabled.setChecked(device.isEnabled());
            editViewHolder.enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    device.setEnabled(compoundButton.isEnabled() && checked);
                    editViewHolder.name.setEnabled(device.isEnabled());
                    if (!device.isEnabled()) {
                        editViewHolder.name.setText("");
                        device.setName(DeviceConfiguration.DISABLED_DEVICE_NAME);
                    }
                }
            });
        } else {
            LinearLayout linearLayout = (LinearLayout) row.findViewById(R.id.parent);
            linearLayout.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            editViewHolder.editController = (Button) row.findViewById(R.id.editControllerButton);
            editViewHolder.editController.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, linearLayout.getMeasuredHeight() + 2 * linearLayout.getPaddingStart()));
            editViewHolder.editController.setEnabled(device.isEnabled() && ConfigurationConstants.partOfGroup(device.getType(), ConfigurationConstants.CONTROLLER));
            editViewHolder.editController.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(activity, "WORKING " + device.getPort(), Toast.LENGTH_SHORT).show();
                    //TODO: Start new DeviceConfigurationActivity
                }
            });

            editViewHolder.spinner = (Spinner) row.findViewById(R.id.spinner);
            List<ConfigurationType> options = new ArrayList<ConfigurationType>(Arrays.asList(typeList));
            editViewHolder.spinner.setAdapter(new ArrayAdapter<ConfigurationType>(activity, R.layout.support_simple_spinner_dropdown_item, options)); //TODO: make spinner item layout
            editViewHolder.spinner.setSelection(ConfigurationConstants.typeIndex((device.isEnabled() && !device.getName().equals(DeviceConfiguration.DISABLED_DEVICE_NAME))?device.getType():ConfigurationType.NOTHING, typeList));
            editViewHolder.ignoreFirstSelection = true;
            editViewHolder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(editViewHolder.ignoreFirstSelection) { //Workaround to prevent OnItemSelectedListener from firing when calling setSelection to initialize the spinner's value
                        editViewHolder.ignoreFirstSelection = false;
                        return;
                    }
                    ConfigurationType type = (ConfigurationType) parent.getItemAtPosition(position);
                    Log.d("ASDFASDF", device.getPort() + " " + type);
                    device.setType(type);
                    if (type == ConfigurationType.NOTHING) {
                        device.setName(DeviceConfiguration.DISABLED_DEVICE_NAME);
                        device.setEnabled(false);
                        editViewHolder.name.setText("");
                    } else {
                        device.setEnabled(true);
                    }
                    editViewHolder.name.setEnabled(device.isEnabled() && !ConfigurationConstants.partOfGroup(type, ConfigurationConstants.CONTROLLER));
                    editViewHolder.name.setClickable(editViewHolder.name.isEnabled());
                    editViewHolder.editController.setEnabled(device.isEnabled() && ConfigurationConstants.partOfGroup(device.getType(), ConfigurationConstants.CONTROLLER));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        if (deviceList.size() - 1 == position) { //Bottommost one
            editViewHolder.name.setImeOptions(EditorInfo.IME_ACTION_DONE);
        }
        row.setTag(editViewHolder);
        return row;
    }

    private View getRunView(int position) {
        return null;
    }
}
