module com.example.finalproject {
    requires javafx.controls;
    requires java.sql;

    requires com.auth0.jwt;
    requires jbcrypt;
    requires itextpdf;
    requires java.mail;

    opens com.example.finalproject.model to javafx.base;

    exports com.example.finalproject;
    exports com.example.finalproject.controller;
}
