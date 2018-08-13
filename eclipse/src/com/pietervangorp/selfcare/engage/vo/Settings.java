package com.pietervangorp.selfcare.engage.vo;

import com.pietervangorp.selfcare.engage.exceptions.NoSuchSensorException;
import com.pietervangorp.selfcare.engage.vo.requesthelpers.ApiSensortypesResponse;

import lombok.Data;

/**
 * Created by git@pietervangorp.com on 8/6/2018.
 */

@Data  
public class Settings {
     
    private String selfcareApiBaseURL;
    private boolean ignoreSslCertificateValidity= false;
    
    private String selfcareApiUser;
    private String selfcareApiPassword;
    
    private String selfcareAppKey; // Can be set using API user/pass but also in advance to avoid exposing those API credentials. Note that app keys may expire so using user/pass securely is more robust.
    
    private String selfcareAccessToken;
    
    @Data    
    public class Consent {
        private boolean approved= false; // can be hacked to true but will fail then harmlessly
        private String consentURL;
        
        public Consent(boolean approved, String url) {
            this.approved= approved;
            this.consentURL= url;
        }
    }
    
    private Consent consent;    
    
    public void setConsent(boolean approved, String url) {
        consent= new Consent(approved, url);
    }
    
    // private String gameDataFileLocation; // pointing for example to RockPapperScissors-Data.json or to /home/pvgorp/RockPapperScissors-Data.json
    /**
     * Sensor types for which values will show in Selfcare graphs
     */
    private ApiSensortypesResponse[] basicSensorTypesOfApp;
    
    public String getSensorTypeIdByName(String name) throws NoSuchSensorException {
    	// first looking in basic sensor types
    	for (ApiSensortypesResponse sensorType: basicSensorTypesOfApp) {
    		if (name.equalsIgnoreCase(sensorType.getName())) {
    			return sensorType.getSensortypeid();
    		}
    	}
    	// then considering complex ones
    	for (ApiSensortypesResponse sensorType: complexSensorTypesOfApp) {
    		if (name.equalsIgnoreCase(sensorType.getName())) {
    			return sensorType.getSensortypeid();
    		}
    	}
    	throw new NoSuchSensorException();
    }
    
    /**
     * Sensor types for which values will <b>not</b> show in Selfcare graphs. Instead, data will only be stored in the backend for developer and researcher retrieval
     */
    private ApiSensortypesResponse[] complexSensorTypesOfApp;
}


