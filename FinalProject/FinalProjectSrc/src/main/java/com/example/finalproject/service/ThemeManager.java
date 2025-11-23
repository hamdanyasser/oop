package com.example.finalproject.service;

import javafx.scene.Scene;

import java.io.*;
import java.util.Properties;





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

    


    public void setScene(Scene scene) {
        this.currentScene = scene;
        applyTheme();
    }

    


    public void toggleTheme() {
        isDarkMode = !isDarkMode;
        applyTheme();
        saveThemePreference();
    }

    


    public void setDarkMode(boolean darkMode) {
        if (this.isDarkMode != darkMode) {
            this.isDarkMode = darkMode;
            applyTheme();
            saveThemePreference();
        }
    }

    


    private void applyTheme() {
        if (currentScene == null) return;

        currentScene.getStylesheets().clear();

        String themeUrl = getClass().getResource(isDarkMode ? DARK_THEME : LIGHT_THEME).toExternalForm();
        currentScene.getStylesheets().add(themeUrl);
    }

    


    public boolean isDarkMode() {
        return isDarkMode;
    }

    


    public String getCurrentThemeName() {
        return isDarkMode ? "Dark" : "Light";
    }

    


    private void saveThemePreference() {
        Properties props = new Properties();
        props.setProperty("dark_mode", String.valueOf(isDarkMode));

        try (FileOutputStream out = new FileOutputStream(PREFS_FILE)) {
            props.store(out, "Theme Preferences");
        } catch (IOException e) {
            System.err.println("Failed to save theme preference: " + e.getMessage());
        }
    }

    


    private void loadThemePreference() {
        Properties props = new Properties();

        try (FileInputStream in = new FileInputStream(PREFS_FILE)) {
            props.load(in);
            isDarkMode = Boolean.parseBoolean(props.getProperty("dark_mode", "false"));
        } catch (IOException e) {
            
            isDarkMode = false;
        }
    }
}
