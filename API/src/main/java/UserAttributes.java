package main.java;

public class UserAttributes {
    private final String clientIpAddress;
    private final String isFromNewLogin;
    private final String mail;
    private final String authenticationDate;

    public UserAttributes(String clientIpAddress, String isFromNewLogin, String mail, String authenticationDate) {
        this.clientIpAddress = clientIpAddress;
        this.isFromNewLogin = isFromNewLogin;
        this.mail = mail;
        this.authenticationDate = authenticationDate;
    }

    public String getClientIpAddress() {
        return clientIpAddress;
    }

    public String getIsFromNewLogin() {
        return isFromNewLogin;
    }

    public String getMail() {
        return mail;
    }

    public String getAuthenticationDate() {
        return authenticationDate;
    }

    @Override
    public String toString() {
        return "\nUser Attributes:\n" +
                "Client IP Address: " + clientIpAddress + "\n" +
                "Is From New Login: " + isFromNewLogin + "\n" +
                "Authentication Date: " + authenticationDate + "\n";
    }
}
