package com.javafx.custom.components;

import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

public class LibTooltip {
    private Tooltip tooltip;

    public LibTooltip(Control control, String text) {
        if (control.getTooltip() == null) {
            control.setTooltip(tooltipConstructor(text, 0));
        }
    }

    public LibTooltip(Control control, String text, double delay) {
        if (control.getTooltip() == null) {
            control.setTooltip(tooltipConstructor(text, delay));
        }
    }

    private Tooltip tooltipConstructor(String text, double delay) {
        tooltip = new Tooltip(text);
        tooltip.setShowDelay(Duration.seconds(delay));

        return tooltip;
    }

    public Tooltip getTooltip() {
        return tooltip;
    }
}