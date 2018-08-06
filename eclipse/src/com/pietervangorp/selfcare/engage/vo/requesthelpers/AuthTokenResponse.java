package com.pietervangorp.selfcare.engage.vo.requesthelpers;

import lombok.Data;

@Data
public class AuthTokenResponse {
    private String access_token;
    private String token_type= "bearer";
    private String expires_in; 
    private String refresh_token;
}
