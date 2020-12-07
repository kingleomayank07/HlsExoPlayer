package com.naseeb.log.flavors;

import com.naseeb.log.AppLoggerImpl;

public final class FlavorHelper {

    public final static boolean isDev() {
        return (Flavor.dev.name().equals(AppLoggerImpl.getApplicationFlavor()));
    }

    public final static boolean isProd() {
        return (Flavor.prod.name().equals(AppLoggerImpl.getApplicationFlavor()));
    }

    public final static boolean isQA() {
        return (Flavor.qa.name().equals(AppLoggerImpl.getApplicationFlavor()));
    }

    public final static boolean isNonProduction() {
        return !(Flavor.prod.name().equals(AppLoggerImpl.getApplicationFlavor()));
    }
}
