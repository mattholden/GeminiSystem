package com.darkenedsky.gemini.service;

public final class ServletResponse { 
	private String response, contentType;
	public ServletResponse(String resp, String type) { 
		response = resp;
		contentType = type;
	}
	public String getResponse() { return response; } 
	public String getContentType() { return contentType; } 		
}
