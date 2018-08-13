
package com.pietervangorp.selfcare.engage.vo.requesthelpers.measuredvalues;

import com.google.gson.annotations.SerializedName;
import com.pietervangorp.selfcare.engage.vo.requesthelpers.ApiUserMeasuredvaluesBody;

import lombok.Data;

@Data 
public class SensorValue {
	@SerializedName(value = "Id", alternate = {"id", "ID"})
    public String id;
	@SerializedName(value = "SensorTypeId", alternate = {"sensorTypeId"})
    public String sensorTypeId;
	@SerializedName(value = "Value", alternate = {"value"})
    public String value; // relaxing Integer for measuredvalues, such that this VO/DTO can be used also for storedvalues
	@SerializedName(value = "Unit", alternate = {"unit"})
    public String unit;
	@SerializedName(value = "Timestamp", alternate = {"timestamp"})
    public String timestamp;
	@SerializedName(value = "Activity", alternate = {"activity"})
    public String activity;

}
