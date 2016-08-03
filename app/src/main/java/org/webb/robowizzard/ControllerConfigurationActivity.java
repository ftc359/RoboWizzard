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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.qualcomm.robotcore.hardware.configuration.ControllerConfiguration;
import com.qualcomm.robotcore.util.SerialNumber;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class ControllerConfigurationActivity extends BaseActivity {
    private BetterEditText filename;

    private Button addButton, scanButton, saveButton;

    private ControllerAdapter controllerAdapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller_configuration);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        filename = (BetterEditText) findViewById(R.id.layoutName);

        addButton = (Button) findViewById(R.id.toolbar_add);
        scanButton = (Button) findViewById(R.id.toolbar_scan);
        saveButton = (Button) findViewById(R.id.toolbar_save);
        listView = (ListView) findViewById(R.id.layoutList);
        if(getIntent().getBooleanExtra(RUN_ID, false)) {
            filename.setVisibility(View.GONE);
            addButton.setVisibility(View.GONE);
            scanButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            if(!scan()) this.onBackPressed();
        }
        else {
            if(!saved.getFilename().equals("")) {
                filename.setText(saved.getFilename());
            }
            else {
                filename.requestFocus();
            }
            filename.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if(!b) { //Lost focus
                        current.setFilename(((BetterEditText) view).getText().toString());
                    }
                }
            });
        }
        controllerAdapter = new ControllerAdapter(this, current);
        listView.setAdapter(controllerAdapter);
    }

    public void help(View v) {
        if(running) {
            showHelp("Controller Configuration", "This layout is currently running. To stop it, tap the stop button located in the action bar.");
        }
        else {
            showHelp("Controller Configuration", "To edit controllers, tap on them. To delete a controller from the layout, swipe left on it. To add a new controller, touch the plus button for controller options. To save this configuration, tap the floppy disk icon. To scan for more controllers, touch the refresh button. Duplicate Serial Numbers will only save the one furthest down the list.");
        }
    }

    public void add(View v) {
        clearFocus();
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.getMenuInflater().inflate(R.menu.popup_new_controller, popupMenu.getMenu());
        final SerialNumber serialNumber = new SerialNumber(DEFAULT_SERIAL_NUMBER);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch(menuItem.getItemId()) {
                    case R.id.newMotorController:
                        controllerAdapter.add(util.buildMotorController(serialNumber));
                        listView.setSelection(controllerAdapter.getCount() - 1);
                        break;
                    case R.id.newServoController:
                        controllerAdapter.add(util.buildServoController(serialNumber));
                        listView.setSelection(controllerAdapter.getCount() - 1);
                        break;
                    case R.id.newLegacyModule:
                        controllerAdapter.add(util.buildLegacyModule(serialNumber));
                        listView.setSelection(controllerAdapter.getCount() - 1);
                        break;
                    case R.id.newDeviceInterfaceModule:
                        controllerAdapter.add(util.buildDeviceInterfaceModule(serialNumber));
                        listView.setSelection(controllerAdapter.getCount() - 1);
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    public void run() {
        if (getIntent().getBooleanExtra(RUN_ID, false)) {
            this.onBackPressed();
            return;
        }

        super.run();

        AlphaAnimation animation = new AlphaAnimation(running?1.0F:0.0F, running?0.0F:1.0F);
        animation.setDuration(1000);
        addButton.startAnimation(animation);
        scanButton.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() { //Placed here so it only activates once
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                int visibility = running?View.INVISIBLE:View.VISIBLE;
                addButton.setVisibility(visibility);
                scanButton.setVisibility(visibility);
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
        filename.setClickable(!running);
        filename.setFocusable(!running);
        filename.setFocusableInTouchMode(!running);
        filename.setCursorVisible(!running);
    }

    public void scan(View v) {
        RotateAnimation animation = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5F, Animation.RELATIVE_TO_SELF, 0.5F);
        animation.setDuration(500);
        v.startAnimation(animation);
        clearFocus();
        scan();
    }

    public void save(View v) {
        clearFocus();
        current.setFilename(filename.getText().toString());
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

    public void onBackPressed() {
        if(running) {
            super.run();
            super.onBackPressed();
            overridePendingTransition(R.anim.fade_in, R.anim.slide_out_horizontal);
        }
        else {
            current.setFilename(filename.getText().toString());

            if(current.contains(new SerialNumber(DEFAULT_SERIAL_NUMBER))) {
                AlertDialog.Builder builder = util.buildBuilder("File Not Saved", "Please change the serial number(s) from the default.");
                builder.setNeutralButton("Ok", dummyListener);
                builder.show();
                return;
            }
            if(current.getFilename().equals("") && !saved.getFilename().equals("")) {
                AlertDialog.Builder builder = util.buildBuilder("File Not Saved", "Please change the layout name from the default.");
                builder.setNeutralButton("Ok", dummyListener);
                builder.show();
                return;
            }
            if(!saved.equals(current)) {
                AlertDialog.Builder builder = util.buildBuilder("Unsaved Changes", "You have unsaved changes. Pressing \'Save\' will save your changes while pressing \'Cancel\' will remove them.");
                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        save();
                        ControllerConfigurationActivity.super.onBackPressed();
                        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_horizontal);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ControllerConfigurationActivity.super.onBackPressed();
                        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_horizontal);
                    }
                });
                builder.show();
            }
            else {
                super.onBackPressed();
                overridePendingTransition(R.anim.fade_in, R.anim.slide_out_horizontal);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == DEVICE_CONFIG) {
            int visibility = running?View.INVISIBLE:View.VISIBLE;
            addButton.setVisibility(visibility);
            scanButton.setVisibility(visibility);
            saveButton.setVisibility(visibility);
            filename.setClickable(!running);
            filename.setFocusable(!running);
            filename.setFocusableInTouchMode(!running);
            filename.setCursorVisible(!running);
        }
    }
}
