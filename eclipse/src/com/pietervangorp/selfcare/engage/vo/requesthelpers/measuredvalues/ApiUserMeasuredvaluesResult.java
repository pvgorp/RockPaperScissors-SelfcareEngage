
package com.pietervangorp.selfcare.engage.vo.requesthelpers.measuredvalues;

import java.util.List;

import lombok.Data;

/**
 * DTO for GET /api/user/measuredvalues
 * @author pvgorp
 *
 */
@Data
public class ApiUserMeasuredvaluesResult {
	/**
	 * Attributed names are imposed by the API so they should not be changed
	 */
    public SensorValue[] ValueList = null;
    public SensorValue[] DeletedValueList = null;

}
