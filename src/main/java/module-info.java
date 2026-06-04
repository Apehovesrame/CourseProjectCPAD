module ru.pin123.courseprojectcpad {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.slf4j;


    opens ru.pin123.courseprojectcpad to javafx.fxml;
    exports ru.pin123.courseprojectcpad;
    opens ru.pin123.courseprojectcpad.controller to javafx.fxml;
    exports ru.pin123.courseprojectcpad.controller;

    exports ru.pin123.courseprojectcpad.model;
}