package com.example.finalproject.service;

import javafx.scene.Scene;

import java.io.*;
import java.util.Properties;

/**
 * Manages application theme (light/dark mode)
 * Singleton pattern for global theme management
 */
public class ThemeManager {
    private static ThemeManager instance;
    private static final String LIGHT_THEME = "/com/example/finalproject/view/style.css";
    private static final String DARK_THEME = "/com/example/finalproject/view/dark-theme.css";
    private static final String PREFS_FILE = "theme.properties";

    private boolean isDarkMode = false;
    private Scene currentScene = null;

    private ThemeManager() {
        loadThemePreference();
    }

    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    /**
     * Set the current scene to apply theme to
     */
    public void setScene(Scene scene) {
        this.currentScene = scene;
        applyTheme();
    }

    /**
     * Toggle between light and dark mode
     */
    public void toggleTheme() {
        isDarkMode = !isDarkMode;
        applyTheme();
        saveThemePreference();
    }

    /**
     * Set theme explicitly
     */
    public void setDarkMode(boolean darkMode) {
        if (this.isDarkMode != darkMode) {
            this.isDarkMode = darkMode;
            applyTheme();
            saveThemePreference();
        }
    }

    /**
     * Apply the current theme to the scene
     */
    private void applyTheme() {
        if (currentScene == null) return;

        currentScene.getStylesheets().clear();

        String themeUrl = getClass().getResource(isDarkMode ? DARK_THEME : LIGHT_THEME).toExternalForm();
        currentScene.getStylesheets().add(themeUrl);
    }

    /**
     * Check if dark mode is enabled
     */
    public boolean isDarkMode() {
        return isDarkMode;
    }

    /**
     * Get current theme name
     */
    public String getCurrentThemeName() {
        return isDarkMode ? "Dark" : "Light";
    }

    /**
     * Save theme preference to file
     */
    private void saveThemePreference() {
        Properties props = new Properties();
        props.setProperty("dark_mode", String.valueOf(isDarkMode));

        try (FileOutputStream out = new FileOutputStream(PREFS_FILE)) {
            props.store(out, "Theme Preferences");
        } catch (IOException e) {
            System.err.println("Failed to save theme preference: " + e.getMessage());
        }
    }

    /**
     * Load theme preference from file
     */
    private void loadThemePreference() {
        Properties props = new Properties();

        try (FileInputStream in = new FileInputStream(PREFS_FILE)) {
            props.load(in);
            isDarkMode = Boolean.parseBoolean(props.getProperty("dark_mode", "false"));
        } catch (IOException e) {
            // File doesn't exist or can't be read - use default (light mode)
            isDarkMode = false;
        }
    }
}
