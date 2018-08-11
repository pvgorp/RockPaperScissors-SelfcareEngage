package com.pietervangorp.selfcare.engage.vo;

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
    
    private String gameDataFileLocation; // pointing for example to RockPapperScissors-Data.json or to /home/pvgorp/RockPapperScissors-Data.json
}


