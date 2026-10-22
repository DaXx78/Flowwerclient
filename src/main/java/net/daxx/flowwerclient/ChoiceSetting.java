package net.daxx.flowwerclient;

public class ChoiceSetting extends Setting {
    public final String[] options;
    public int index;

    public ChoiceSetting(String name, int index, String... options) {
        super(name);
        this.options = options;
        this.index = index;
    }

    public String current() {
        return options[index];
    }

    public void next() {
        index = (index + 1) % options.length;
    }

    @Override
    public int height() {
        return 28;
    }

    @Override
    public String serialize() {
        return String.valueOf(index);
    }

    @Override
    public void deserialize(String text) {
        try {
            int i = Integer.parseInt(text);
            if (i >= 0 && i < options.length) index = i;
        } catch (NumberFormatException ignored) {
        }
    }
}