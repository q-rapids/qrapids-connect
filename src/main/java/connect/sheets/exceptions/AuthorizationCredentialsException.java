package connect.sheets.exceptions;

public class AuthorizationCredentialsException extends Exception{
    public AuthorizationCredentialsException(String errorMessage) {
        super(errorMessage);
    }
}
