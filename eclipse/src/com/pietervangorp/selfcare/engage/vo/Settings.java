package com.pietervangorp.selfcare.engage.vo;

import lombok.Data;

/**
 * Created by git@pietervangorp.com on 8/6/2018.
 */

@Data
public class Settings {
    private String selfcareApiBaseURL;
    
    private String selfcareApiUser;
    private String selfcareApiPassword;
    
    private String selfcareAppKey; // Can be set using API user/pass but also in advance to avoid exposing those API credentials. Note that app keys may expire so using user/pass securely is more robust.
    
    @Data    
    public class Consent {
        private boolean approved= false; // can be hacked to true but will fail then harmlessly
        private String consentURL;
        
        Consent(boolean approved, String url) {
            this.approved= approved;
            this.consentURL= url;
        }
    }
    
    private Consent consent;
}


