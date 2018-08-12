package com.pietervangorp.selfcare.engage.vo.requesthelpers;

import lombok.Data;

@Data
public class ApiSensortypesResponse {
	private String appid;
	private String sensortypeid;
	private String name;
	private String unit;
	private String isdraft;
}
