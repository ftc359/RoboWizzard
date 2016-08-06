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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.qualcomm.robotcore.hardware.configuration.DeviceConfiguration;

import java.util.List;

public class DeviceAdapter extends BaseAdapter {
    Activity activity;
    List<DeviceConfiguration> deviceList;
    public DeviceAdapter(Activity activity, List<DeviceConfiguration> deviceList) {
        this.activity = activity;
        this.deviceList = deviceList;
    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return deviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    static class ViewHolder {
        TextView port;
        CheckBox enabled;
        BetterEditText name;
        Button test;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final DeviceConfiguration device = (DeviceConfiguration) getItem(position);
        ViewHolder viewHolder = null;
        DeviceConfiguration.ConfigurationType condition = ((DeviceConfiguration) getItem(position)).getType();
        if(convertView == null) {
            viewHolder = new ViewHolder();
            switch(condition) {
                case MOTOR:
                    convertView = activity.getLayoutInflater().inflate(R.layout.item_device_motor_and_servo, parent, false);
                    viewHolder.port = (TextView) convertView.findViewById(R.id.portNumber);
                    viewHolder.enabled = (CheckBox) convertView.findViewById(R.id.portEnabled);
                    viewHolder.enabled.setChecked(device.isEnabled());
                    viewHolder.enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            device.setEnabled(compoundButton.isEnabled() && b);
                        }
                    });
                    viewHolder.name = (BetterEditText) convertView.findViewById(R.id.portName);
                    viewHolder.name.setText(device.getName());
                    viewHolder.name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View view, boolean b) {
                            if(!b) {
                                device.setName(((BetterEditText) view).getText().toString());
                            }
                        }
                    });
                    break;
//                case SERVO:
//                    break;
                default:
                    convertView = activity.getLayoutInflater().inflate(R.layout.item_dim_options, parent, false);
                    viewHolder.test = (Button) convertView.findViewById(R.id.optionButton);
                    break;
            }
            if(viewHolder.name != null && getCount() - 1 == position) {
                viewHolder.name.setImeOptions(EditorInfo.IME_ACTION_DONE);
            }
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        switch(condition) {
            case MOTOR:
                viewHolder.port.setText(""+device.getPort());
                break;
            default:
                viewHolder.test.setText(((DeviceConfiguration) getItem(position)).getName());
                break;
        }

        return convertView;
    }
}
