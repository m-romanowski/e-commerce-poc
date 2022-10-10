package dev.marcinromanowski.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Profiles {

    public static final String INTEGRATION = "integration";
    public static final String EXCEPT_INTEGRATION = "!integration";

}
