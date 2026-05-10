package views;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class LoginView extends VBox {
    private TextField campoNome;
    private Button botaoEntrar;

    public LoginView() {
        this.setSpacing(15);
        this.setAlignment(Pos.CENTER);
        this.setStyle("-fx-background-color: #2c3e50;");

        Label titulo = new Label("MOEDA QUEST");
        titulo.setFont(new Font("Arial", 30));
        titulo.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        campoNome = new TextField();
        campoNome.setPromptText("Digite seu nome de usuário...");
        campoNome.setMaxWidth(250);
        campoNome.setStyle("-fx-font-size: 14;");

        botaoEntrar = new Button("Iniciar Aventura");
        botaoEntrar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");
        botaoEntrar.setCursor(javafx.scene.Cursor.HAND);

        this.getChildren().addAll(titulo, campoNome, botaoEntrar);
    }

    public String getNomeDigitado() { return campoNome.getText(); }
    public Button getBotaoEntrar() { return botaoEntrar; }
}