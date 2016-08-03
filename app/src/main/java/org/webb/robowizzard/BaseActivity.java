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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.qualcomm.hardware.HardwareDeviceManager;
import com.qualcomm.robotcore.eventloop.EventLoopManager;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.DeviceManager;
import com.qualcomm.robotcore.hardware.configuration.ControllerConfiguration;
import com.qualcomm.robotcore.hardware.configuration.DeviceConfiguration;
import com.qualcomm.robotcore.hardware.configuration.Utility;
import com.qualcomm.robotcore.hardware.configuration.WriteXMLFileHandler;
import com.qualcomm.robotcore.util.SerialNumber;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class BaseActivity extends AppCompatActivity {
    public static Point screenSize;
    static LayoutFile current, saved;
    static ArrayList<ControllerConfiguration> currentSetup;
    static boolean running;
    private static List<MenuItem> toggleCallback;
    DialogInterface.OnClickListener dummyListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int button) {
        }
    };
    Utility util;


    final static String DEFAULT_SERIAL_NUMBER = "SERIAL_NUMBER";
    final static String RUN_ID = "NULL_LAYOUT";
    final static int DEVICE_CONFIG = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        util = new Utility(this);
        if(getClass().getCanonicalName().equals(MainActivity.class.getCanonicalName())) {
            toggleCallback = new ArrayList<MenuItem>();
            screenSize = new Point();
        }
        ((WindowManager) this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(screenSize);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.run_button:
                run();
                break;
            case R.id.about_button:
                startActivity(new Intent(this, AboutActivity.class));
                overridePendingTransition(R.anim.slide_in_vertical, R.anim.fade_out);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void run(){
        running = !running;
        for(MenuItem item : toggleCallback) {
            toggleIcon(item);
        }
    }

    public void showHelp(String title, String tip){

        AlertDialog.Builder builder = util.buildBuilder(title, tip);
        builder.setPositiveButton("Ok", dummyListener);
        AlertDialog alert = builder.create();
        alert.show();
        TextView mTextView = (TextView)alert.findViewById(android.R.id.message);
        mTextView.setTextSize(14.0F);
    }

    public void makeToast(Context context, String message){
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    public boolean launchedBy(Class activity) {
        if(getCallingActivity() != null) {
            if(getCallingActivity().getClassName().equals(activity.getCanonicalName())) {
                return true;
            }
        }
        return false;
    }

    public void save() {
        WriteXMLFileHandler handler = new WriteXMLFileHandler(this);
        if(saved.equals(current)) {
            return;
        }
        try {
            handler.writeToFile(handler.writeXml(current.getLayoutList()), Utility.CONFIG_FILES_DIR, current.getFilename());
        }
        catch (RobotCoreException e) {
            util.complainToast(e.getMessage(), this);
        }
        catch (IOException e) {
            AlertDialog.Builder builder = util.buildBuilder("File Not Saved", "Please change duplicate controller names.");
            builder.setNeutralButton("Ok", dummyListener);
            builder.show();
        }
        saved.setFilename(current.getFilename());
        saved.setLayout(current.getLayoutList());
        util.confirmSave();
    }

    public boolean scan() {
        final LayoutFile temp = new LayoutFile();
        try {
            HardwareDeviceManager scanner = new HardwareDeviceManager(this, (EventLoopManager) null);
            Iterator deviceIterator = (scanner.scanForUsbDevices().entrySet()).iterator();

            this.util.resetCount();
            while(deviceIterator.hasNext()) {
                Map.Entry entry = (Map.Entry) deviceIterator.next();
                ControllerConfiguration controller = (ControllerConfiguration) entry.getValue();
                SerialNumber serialNumber = (SerialNumber) entry.getKey();
                if(current.contains(controller)) {
                    temp.add(controller);
                }
                else {
                    switch((DeviceManager.DeviceType) entry.getValue()) {
                        case MODERN_ROBOTICS_USB_DC_MOTOR_CONTROLLER:
                            temp.add(this.util.buildMotorController(serialNumber));
                            break;
                        case MODERN_ROBOTICS_USB_SERVO_CONTROLLER:
                            temp.add(this.util.buildServoController(serialNumber));
                            break;
                        case MODERN_ROBOTICS_USB_LEGACY_MODULE:
                            temp.add(this.util.buildLegacyModule(serialNumber));
                            break;
                        case MODERN_ROBOTICS_USB_DEVICE_INTERFACE_MODULE:
                            temp.add(this.util.buildDeviceInterfaceModule(serialNumber));
                            break;
                    }
                }
            }
        }
        catch(RobotCoreException e){
            temp.clear();
        }
        if(temp.size() == 0) {
            Toast.makeText(this, "No Devices Found", Toast.LENGTH_SHORT).show();
            return false;
        }
        else {
            Toast.makeText(this, "Scan Complete", Toast.LENGTH_SHORT).show();
            if(current.size() != 0 && !current.equals(temp)) {
                AlertDialog.Builder builder = this.util.buildBuilder("Add or Overwrite", "Pressing \'Add\' will combine the current layout with the scanned one. Pressing \'Overwrite\' will replace the current layout with the scanned one.");
                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        current.combine(temp.getLayoutList());
                    }
                });
                builder.setNeutralButton("Overwrite", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        current.setLayout(temp.getLayoutList());
                    }
                });
                builder.setNegativeButton("Cancel", dummyListener);
            }
            else {
                current.setLayout(temp.getLayoutList());
            }
        }
        return true;
    }

    public void clearFocus(){
        View focusedView = getCurrentFocus();
        if(focusedView != null) {
            focusedView.clearFocus();
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem runButton = menu.findItem(R.id.run_button);
        toggleIcon(runButton);
        if(!toggleCallback.contains(runButton)) {
            toggleCallback.add(runButton);
        }
        return true;
    }

    private void toggleIcon(MenuItem item) {
        item.setIcon((running) ? R.drawable.ic_stop : R.drawable.ic_start);
        item.setTitle((running) ? R.string.toolbar_stop : R.string.toolbar_run);
    }

    public static File getFile(String filename) {
        filename = Utility.CONFIG_FILES_DIR + filename + Utility.FILE_EXT;
        filename = filename.trim();
        return new File(filename);
    }
}
