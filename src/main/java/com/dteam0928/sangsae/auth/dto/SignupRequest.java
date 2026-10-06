package com.dteam0928.sangsae.auth.dto;

/** O-01 회원가입 폼 */
public class SignupRequest {

    private String email;
    private String password;
    private String passwordConfirm;

    public SignupRequest() {
    }

    public SignupRequest(String email, String password, String passwordConfirm) {
        this.email = email;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPasswordConfirm() { return passwordConfirm; }
    public void setPasswordConfirm(String passwordConfirm) { this.passwordConfirm = passwordConfirm; }
}
