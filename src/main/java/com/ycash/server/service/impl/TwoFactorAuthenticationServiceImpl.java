package com.ycash.server.service.impl;

import com.ycash.server.service.TwoFactorAuthenticationService;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import dev.samstevens.totp.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TwoFactorAuthenticationServiceImpl implements TwoFactorAuthenticationService {

    @Override
    public String generateNewSecret() {
        return new DefaultSecretGenerator().generate();
    }

    @Override
    public String generateQrCodeImageUri(String secret) {
        QrData data = new QrData.Builder()
                .label("Yuri Coding 2FA example")
                .secret(secret)
                .issuer("YD-Coding")
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        QrGenerator generator = new ZxingPngQrGenerator();
        byte[] imageData = new byte[0];

        try {
            imageData = generator.generate(data);
        } catch (QrGenerationException e) {
            e.printStackTrace();
            log.error("Error while generating QR-CODE.");
        }

        return Utils.getDataUriForImage(imageData, generator.getImageMimeType());
    }

    @Override
    public boolean isOtpValid(String secret, String code) {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        return verifier.isValidCode(secret, code);
    }

    @Override
    public boolean isOtpNotValid(String secret, String code) {
        return !this.isOtpValid(secret, code);
    }
}
