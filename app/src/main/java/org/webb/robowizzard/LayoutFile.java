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

import android.util.Log;
import android.widget.Toast;

import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.configuration.ControllerConfiguration;
import com.qualcomm.robotcore.hardware.configuration.DeviceConfiguration;
import com.qualcomm.robotcore.hardware.configuration.ReadXMLFileHandler;
import com.qualcomm.robotcore.hardware.configuration.Utility;
import com.qualcomm.robotcore.util.SerialNumber;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class LayoutFile implements  Iterable<ControllerConfiguration>{
    private String filename;
    private ArrayList<ControllerConfiguration> layoutList;
    private HashMap<SerialNumber, ControllerConfiguration> layoutMap;
    private ArrayList<ControllerConfiguration> duplicates;

    public LayoutFile(LayoutFile layoutFile) {
        this.filename = layoutFile.getFilename();
        this.layoutList = new ArrayList<>();
        this.layoutMap = new HashMap<>();
        this.duplicates = new ArrayList<>();
        for(ControllerConfiguration controller : layoutFile.layoutList) {
            this.add(new Controller(controller));
        }
    }

    public LayoutFile() {
        this.filename = "";
        this.layoutList = new ArrayList<>();
        this.layoutMap = new HashMap<>();
        this.duplicates = new ArrayList<>();
    }

    public LayoutFile(String filename) {
        this.filename = filename;

        ArrayList<ControllerConfiguration> layout = new ArrayList<>();
        try {
            layout = (ArrayList<ControllerConfiguration>) new ReadXMLFileHandler(null).parse(new FileInputStream(Utility.CONFIG_FILES_DIR + filename + Utility.FILE_EXT));
        }
        catch (FileNotFoundException | RobotCoreException e) {
            Log.e("LayoutFile", e.getMessage());
        }
        this.layoutList = new ArrayList<>();
        this.layoutMap = new HashMap<>();
        this.duplicates = new ArrayList<>();
        this.addAll(layout);
    }

    public LayoutFile(ArrayList<ControllerConfiguration> layout) {
        this("", layout);
    }

    public LayoutFile(String filename, ArrayList<ControllerConfiguration> layout) {
        this.filename = filename;
        this.layoutList = new ArrayList<>();
        this.layoutMap = new HashMap<>();
        this.duplicates = new ArrayList<>();
        this.addAll(layout);
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public void setLayout(ArrayList<ControllerConfiguration> layout) {
        this.clear();
        this.addAll(layout);
    }

    public ArrayList<ControllerConfiguration> getLayoutList() {
        return layoutList;
    }

    public HashMap<SerialNumber, ControllerConfiguration> getLayoutMap() {
        return layoutMap;
    }

    public void add(int index, ControllerConfiguration controller) {
        layoutList.add(index, controller);
        if(this.contains(controller.getSerialNumber())) {
            ControllerConfiguration overwrittenController = layoutMap.get(controller.getSerialNumber());
            if(!duplicates.contains(overwrittenController)) {
                duplicates.add(overwrittenController);
            }
            duplicates.add(controller);
        }
        layoutMap.put(controller.getSerialNumber(), controller);
    }

    public void add(ControllerConfiguration controller) {
        this.add(this.size(), controller);
    }

    public void addAll(ArrayList<ControllerConfiguration> layout) {
        for(ControllerConfiguration controller : layout) {
            this.add(controller);
        }
    }

    public void combine(ArrayList<ControllerConfiguration> layout) {
        for(ControllerConfiguration controller : layout) {
            if(!this.contains(controller)) {
                this.add(controller);
            }
        }
    }

    public boolean contains(ControllerConfiguration controller) {
        return layoutList.contains(controller);
    }

    public boolean contains(SerialNumber serialNumber) {
        return layoutMap.containsKey(serialNumber);
    }

    public ControllerConfiguration get(int index) {
        return layoutList.get(index);
    }

    public ControllerConfiguration get(SerialNumber serialNumber) {
        return layoutMap.get(serialNumber);
    }

    public int indexOf(ControllerConfiguration controller) {
        return layoutList.indexOf(controller);
    }

    public void set(int index, ControllerConfiguration controller) {
        this.remove(index);
        this.add(index, controller);
    }

    public void set(SerialNumber serialNumber, ControllerConfiguration controller) {
        this.set(this.indexOf(get(serialNumber)), controller);
    }

    public boolean remove(ControllerConfiguration controller) {
        if(!this.contains(controller)) {
            return false;
        }
        layoutList.remove(controller);
        if(duplicates.remove(controller) && layoutMap.get(controller.getSerialNumber()).equals(controller)) { //Don't delete overloaded serial numbers
            layoutMap.remove(controller.getSerialNumber());
        }

        return true;
    }

    public boolean remove(SerialNumber serialNumber) {
        if(!this.contains(serialNumber)) {
            return false;
        }
        ControllerConfiguration controller = layoutMap.get(serialNumber);
        return remove(controller);
    }

    public boolean remove(int index) {
        return this.remove(this.get(index));
    }

    public void removeAll(ArrayList<ControllerConfiguration> layout) {
        for(ControllerConfiguration controller : layout) {
            this.remove(controller);
        }
    }

    public ArrayList<ControllerConfiguration> compareSimilarities(LayoutFile layoutFile) {
        ArrayList<ControllerConfiguration> layout = layoutFile.getLayoutList();
        layout.retainAll(this.layoutList);
        return layout;
    }

    public ArrayList<ControllerConfiguration> compareDifferences(LayoutFile layoutFile) {
        ArrayList<ControllerConfiguration> layout = layoutFile.getLayoutList();
        layout.retainAll(this.layoutList);
        return layout;
    }

    public void clear() {
        layoutList.clear();
        layoutMap.clear();
        duplicates.clear();
    }

    public ArrayList<ControllerConfiguration> getDuplicates() {
        return duplicates;
    }

    public boolean hasDuplicates() {
        return getDuplicates().size() != 0;
    }

    public int size() {
        return layoutList.size();
    }

    public Iterator<ControllerConfiguration> iterator() {
        return layoutList.iterator();
    }

    @Override
    public boolean equals(final Object obj) {
        if(this == obj) return true;
        if(obj == null || !(obj instanceof LayoutFile)) return false;

        LayoutFile layoutFile = (LayoutFile) obj;

        if(this.filename == null || layoutFile.filename == null) return false;
        else if(!this.filename.equals(layoutFile.filename)) return false;

        if(this.layoutList == null || layoutFile.layoutList == null) return false;
        else if(!this.layoutList.equals(layoutFile.layoutList)) return false;

        if(this.layoutMap == null || layoutFile.layoutMap == null) return false;
        else if(!this.layoutMap.equals(layoutFile.layoutMap)) return false;

        if(this.duplicates == null || layoutFile.duplicates == null) return false;
        else if(!this.duplicates.equals(layoutFile.duplicates)) return false;

        return true;
    }

    private class Controller extends ControllerConfiguration {
        public Controller(ControllerConfiguration controller) {
            super(controller.getName(), controller.getSerialNumber(), controller.getType());
            List<DeviceConfiguration> deviceList = new ArrayList<>();

            for(DeviceConfiguration device : controller.getDevices()) {
                deviceList.add(new DeviceConfiguration(device.getPort(), device.getType(), device.getName(), device.isEnabled()));
            }
            this.addDevices(deviceList);
        }

        @Override
        public boolean equals(Object obj) {
            if(this == obj) return true;
            if(obj == null || !(obj instanceof ControllerConfiguration)) return false;

            ControllerConfiguration controller = (ControllerConfiguration) obj;

            if(!this.getName().equals(controller.getName())) return false;

            if(!this.getSerialNumber().equals(controller.getSerialNumber())) return false;

            if(this.getType() != controller.getType()) return false;

            if(this.getDevices().size() != controller.getDevices().size()) return false;

            //Since DeviceConfiguration has no overridden equals method we have to compare it ourselves
            for(int index = 0; index < this.getDevices().size(); index++) {
                DeviceConfiguration myDevice = this.getDevices().get(index);
                DeviceConfiguration otherDevice = controller.getDevices().get(index);

                if(!myDevice.getName().equals(otherDevice.getName())) return false;

                if(myDevice.getPort() != otherDevice.getPort()) return  false;

                if(myDevice.getType() != otherDevice.getType()) return false;

                if(myDevice.isEnabled() != otherDevice.isEnabled()) return false;
            }

            return true;
        }
    }
}
