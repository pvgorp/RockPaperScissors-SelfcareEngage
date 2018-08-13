package com.pietervangorp.selfcare.engage.vo.requesthelpers;

import lombok.Data;

@Data  
public class AuthTokenBody {
    private String username;
    private String password;
    private String grant_type="password";
}
