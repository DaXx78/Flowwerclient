package net.daxx.flowwerclient;

public class ToggleSetting extends Setting {
    public boolean value;

    public ToggleSetting(String name, boolean value) {
        super(name);
        this.value = value;
    }

    @Override
    public int height() {
        return 26;
    }

    @Override
    public String serialize() {
        return String.valueOf(value);
    }

    @Override
    public void deserialize(String text) {
        value = Boolean.parseBoolean(text);
    }
}