package dev.marcinromanowski.security.registration;

import lombok.RequiredArgsConstructor;

import java.nio.CharBuffer;
import java.util.regex.Pattern;

@RequiredArgsConstructor
class PasswordValidation implements ValidationRule {

    // Source: https://owasp.org/www-community/OWASP_Validation_Regex_Repository
    private static final Pattern PASSWORD_REGEX = Pattern.compile("^(?:(?=.*\\d)(?=.*[A-Z])(?=.*[a-z])|(?=.*\\d)(?=.*[^A-Za-z0-9])(?=.*[a-z])|(?=" +
        ".*[^A-Za-z0-9])(?=.*[A-Z])(?=.*[a-z])|(?=.*\\d)(?=.*[A-Z])(?=.*[^A-Za-z0-9]))(?!.*(.)\\1{2,})[A-Za-z0-9!~<>,;:_=?*+#.\"&§%°()" +
        "|\\[\\]\\-$^@/]{12,128}$");

    private final char[] password;

    @Override
    public boolean validate() {
        return password != null && PASSWORD_REGEX.matcher(CharBuffer.wrap(password)).matches();
    }

}
