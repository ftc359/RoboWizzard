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
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.SeekBar;
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
    private List<View> views;
    private boolean edit;
    private Integer editTextWidth;

    public DeviceLayout(Activity activity, LinearLayout parent, List<DeviceConfiguration> deviceList, ConfigurationType[] typeList, boolean edit) {
        this.activity = activity;
        this.parent = parent;
        this.deviceList = deviceList;
        this.typeList = typeList;
        this.edit = edit;

        views = new ArrayList<>();
        for(int i = 0; i < deviceList.size(); i++) {
            views.add(getEditView(i));
        }
        for(int i = 0; i < deviceList.size(); i++) {
            views.add(getRunView(i));
        }

        this.render();
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
        this.render();
    }

    private void render() {
        ScrollView scrollView = ((ScrollView) parent.getParent());
        int scrollLocation = scrollView.getScrollY();
        parent.removeAllViews();
        for(int i = 0; i < deviceList.size(); i++) {
            if(edit) {
                parent.addView(views.get(i));
            }
            else {
                parent.addView(views.get(i + deviceList.size()));
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

    private static class RunViewHolder {
        private TextView port;
        private TextView name;
        private SeekBar slider;
        private BetterEditText value;
        private Button runButton;
        private boolean eatTextChange, eatSliderChange;
    }

    private View getRunView(int position) {
        View row;
        final DeviceConfiguration device = deviceList.get(position);
        final RunViewHolder runViewHolder = new RunViewHolder();

        if(ConfigurationConstants.partOfGroup(device.getType(), ConfigurationConstants.MOTOR)) {
            final boolean servo = device.getType() == ConfigurationType.SERVO;

            row = activity.getLayoutInflater().inflate(R.layout.item_device_motor_run, parent, false);

            runViewHolder.port = (TextView) row.findViewById(R.id.portNumber);
            runViewHolder.port.setText(String.format(Locale.ENGLISH, "%d", device.getPort()));

            runViewHolder.name = (TextView) row.findViewById(R.id.portName);
            runViewHolder.name.setText(device.getName());

            runViewHolder.slider = (SeekBar) row.findViewById(R.id.slider);

            runViewHolder.value = (BetterEditText) row.findViewById(R.id.value);
            if(editTextWidth == null) {
                runViewHolder.value.setText("-0.00");
                runViewHolder.value.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                editTextWidth = runViewHolder.value.getMeasuredWidth();
            }
            runViewHolder.value.setLayoutParams(new LinearLayout.LayoutParams(editTextWidth, LinearLayout.LayoutParams.WRAP_CONTENT));
            runViewHolder.value.setText(servo?"0.50":"0.00");

            LinearLayout ticks = (LinearLayout) row.findViewById(R.id.ticks);
            for(int count = 0; count < (servo?4:8); count++) {
                View blankSpace = new View(activity);
                blankSpace.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0F));
                ticks.addView(blankSpace);
                ticks.addView(activity.getLayoutInflater().inflate(R.layout.tick, ticks, false));
            }

            ((TextView) row.findViewById(R.id.sliderMin)).setText(servo?"0.00":"-1.00");
            ((TextView) row.findViewById(R.id.sliderZero)).setText(servo?"0.50":"0.00");
            ((TextView) row.findViewById(R.id.sliderMax)).setText("1.00");

            runViewHolder.eatTextChange = false;
            runViewHolder.eatSliderChange = false;
            runViewHolder.slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if(runViewHolder.eatSliderChange) {
                        runViewHolder.eatSliderChange = false;
                        return;
                    }
                    runViewHolder.eatTextChange = true;
                    if(servo) {
                        runViewHolder.value.setText(String.format(Locale.ENGLISH, "%.2f", i/200.0));
                    }
                    else {
                        runViewHolder.value.setText(String.format(Locale.ENGLISH, "%.2f", (i-100)/100.0));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            runViewHolder.value.addTextChangedListener(new TextWatcher() {
                String before;

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    before = runViewHolder.value.getText().toString();
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if(runViewHolder.eatTextChange) {
                        runViewHolder.eatTextChange = false;
                        return;
                    }
                    String editValue = runViewHolder.value.getText().toString();
                    if(editValue.trim().equals("")) {
                        return;
                    }
                    double value;
                    try {
                        value = Double.parseDouble(editValue);
                        if(servo) {
                            if(value > 1.00) {
                                value = 1.00;
                                runViewHolder.eatTextChange = true;
                                runViewHolder.value.setText(String.format(Locale.ENGLISH, "%.2f", value));
                            }
                            else if(value < 0.00) {
                                value = 0.00;
                                runViewHolder.eatTextChange = true;
                                runViewHolder.value.setText(String.format(Locale.ENGLISH, "%.2f", value));
                            }
                            value *= 200;
                        }
                        else {
                            if(value > 1.00) {
                                value = 1.00;
                                runViewHolder.eatTextChange = true;
                                runViewHolder.value.setText(String.format(Locale.ENGLISH, "%.2f", value));
                            }
                            else if(value < -1.00) {
                                value = -1.00;
                                runViewHolder.value.setText(String.format(Locale.ENGLISH, "%.2f", value));
                                runViewHolder.eatTextChange = true;
                            }
                            value += 1.00;
                            value *= 100;
                        }
                        runViewHolder.eatSliderChange = true;
                        runViewHolder.slider.setProgress((int) Math.round(value));
                    }
                    catch(NumberFormatException e) {
                        runViewHolder.eatTextChange = true;
                        runViewHolder.value.setText(before);
                        e.printStackTrace();
                        Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else {
            row = activity.getLayoutInflater().inflate(R.layout.item_dim_options, parent, false);
        }

        return row;
    }
}
