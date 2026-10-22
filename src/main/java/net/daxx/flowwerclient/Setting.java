package net.daxx.flowwerclient;

public abstract class Setting {
    public final String name;

    protected Setting(String name) {
        this.name = name;
    }

    public abstract int height();

    public abstract String serialize();

    public abstract void deserialize(String value);
}