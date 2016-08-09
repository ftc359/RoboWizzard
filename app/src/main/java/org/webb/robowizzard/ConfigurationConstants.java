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

import com.qualcomm.robotcore.hardware.configuration.DeviceConfiguration.ConfigurationType;

public class ConfigurationConstants {
    public static ConfigurationType SENSOR[] = {
            ConfigurationType.GYRO,
            ConfigurationType.ACCELEROMETER,
            ConfigurationType.TOUCH_SENSOR,
            ConfigurationType.TOUCH_SENSOR_MULTIPLEXER,
            ConfigurationType.COMPASS,
            ConfigurationType.IR_SEEKER,
            ConfigurationType.IR_SEEKER_V3,
            ConfigurationType.ULTRASONIC_SENSOR,
            ConfigurationType.OPTICAL_DISTANCE_SENSOR,
            ConfigurationType.LIGHT_SENSOR,
            ConfigurationType.COLOR_SENSOR,
            ConfigurationType.ADAFRUIT_COLOR_SENSOR,
            ConfigurationType.LED,
            ConfigurationType.DIGITAL_DEVICE,
            ConfigurationType.ANALOG_INPUT,
            ConfigurationType.ANALOG_OUTPUT,
            ConfigurationType.I2C_DEVICE,
            ConfigurationType.NOTHING
    };

    public static ConfigurationType MOTOR[] = {
            ConfigurationType.PULSE_WIDTH_DEVICE,
            ConfigurationType.MOTOR,
            ConfigurationType.SERVO
    };

    public static ConfigurationType CONTROLLER[] = {
            ConfigurationType.MOTOR_CONTROLLER,
            ConfigurationType.MATRIX_CONTROLLER,
            ConfigurationType.SERVO_CONTROLLER,
            ConfigurationType.LEGACY_MODULE_CONTROLLER,
            ConfigurationType.DEVICE_INTERFACE_MODULE
    };

    public static ConfigurationType LEGACY[] = {
            ConfigurationType.GYRO,
            ConfigurationType.TOUCH_SENSOR,
            ConfigurationType.COMPASS,
            ConfigurationType.IR_SEEKER,
            ConfigurationType.LIGHT_SENSOR,
            ConfigurationType.ACCELEROMETER,
            ConfigurationType.ULTRASONIC_SENSOR,
            ConfigurationType.MOTOR_CONTROLLER,
            ConfigurationType.SERVO_CONTROLLER,
            ConfigurationType.MATRIX_CONTROLLER,
            ConfigurationType.TOUCH_SENSOR_MULTIPLEXER,
            ConfigurationType.COLOR_SENSOR,
            ConfigurationType.NOTHING
    };

    public static ConfigurationType ANALOG_INPUT[] = {
            ConfigurationType.NOTHING,
            ConfigurationType.OPTICAL_DISTANCE_SENSOR,
            ConfigurationType.ANALOG_INPUT
    };

    public static ConfigurationType ANALOG_OUTPUT[] = {
            ConfigurationType.NOTHING,
            ConfigurationType.ANALOG_OUTPUT
    };

    public static ConfigurationType DIGITAL_DEVICE[] = {
            ConfigurationType.NOTHING,
            ConfigurationType.TOUCH_SENSOR,
            ConfigurationType.LED,
            ConfigurationType.DIGITAL_DEVICE
    };

    public static ConfigurationType I2C_DEVICE[] = {
            ConfigurationType.NOTHING,
            ConfigurationType.IR_SEEKER_V3,
            ConfigurationType.ADAFRUIT_COLOR_SENSOR,
            ConfigurationType.COLOR_SENSOR,
            ConfigurationType.GYRO,
            ConfigurationType.I2C_DEVICE
    };

    public static boolean partOfGroup(ConfigurationType value, ConfigurationType[] group) {
        for(ConfigurationType type : group) {
            if(value == type) return true;
        }
        return false;
    }

    public static int typeIndex(ConfigurationType value, ConfigurationType[] group) {
        for(int index = group.length - 1; index >= 0; index--) {
            if(group[index] == value) return index;
        }
        return -1;
    }
}
