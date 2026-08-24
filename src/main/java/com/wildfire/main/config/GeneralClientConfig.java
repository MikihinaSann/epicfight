package com.wildfire.main.config;
public class GeneralClientConfig {
    public static GeneralClientConfig INSTANCE = new GeneralClientConfig();
    public Object disableRendering = new Object() { public boolean get() { return false; } };
    public boolean canBreathe() { return true; }
}
