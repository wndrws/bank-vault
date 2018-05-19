package kspt.bank.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.springframework.stereotype.Component;

@Component
public class MainController {
    @FXML
    private Button helloButton;

    public void hello() {
        helloButton.setText("Hey!");
    }
}
