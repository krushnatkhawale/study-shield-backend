package com.studyshield.regression.context;

public class AuthContext {

    private String jwtToken;

    public String getJwtToken() { return jwtToken; }
    public void setJwtToken(String jwtToken) { this.jwtToken = jwtToken; }
    public boolean hasToken() { return jwtToken != null && !jwtToken.isEmpty(); }
}
