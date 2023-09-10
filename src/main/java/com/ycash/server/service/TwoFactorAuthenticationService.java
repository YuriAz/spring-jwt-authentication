package com.ycash.server.service;

public interface TwoFactorAuthenticationService {

    String generateNewSecret();

    String generateQrCodeImageUri(String secret);

    boolean isOtpValid(String secret, String code);

    boolean isOtpNotValid(String secret, String code);
}
