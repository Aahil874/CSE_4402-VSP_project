package application;

import javafx.application.Application;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        configureRendering();
        Application.launch(GameApplication.class, args);
    }

    static void configureRendering() {
        // Measured on this laptop: D3D VSync ~31ms, unsynchronized ~16ms.
        // Keep the normal JavaFX pipeline and allow -Dprism.vsync=true to override.
        if (System.getProperty("prism.vsync") == null) System.setProperty("prism.vsync", "false");
    }
}
