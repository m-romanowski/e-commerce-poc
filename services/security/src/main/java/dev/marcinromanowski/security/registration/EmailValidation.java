package dev.marcinromanowski.security.registration;

import lombok.RequiredArgsConstructor;

import java.util.regex.Pattern;

@RequiredArgsConstructor
class EmailValidation implements ValidationRule {

    // Source: https://owasp.org/www-community/OWASP_Validation_Regex_Repository
    private static final Pattern EMAIL_REGEX = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,15}$");

    private final String email;

    @Override
    public boolean validate() {
        return email != null && EMAIL_REGEX.matcher(email).matches();
    }

}
