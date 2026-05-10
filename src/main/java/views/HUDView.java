package views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import models.SessaoJogo;
import models.Usuario;

public class HUDView extends HBox {
    private Label lblUsuario;
    private Label lblPontos;
    private Label lblNivel;
    private Button btnSair;

    public HUDView() {
        this.setPadding(new Insets(10));
        this.setSpacing(20);
        this.setAlignment(Pos.CENTER_LEFT);
        // Estilo: fundo escuro com uma borda inferior discreta
        this.setStyle("-fx-background-color: #34495e; -fx-border-color: #2c3e50; -fx-border-width: 0 0 2 0;");

        lblUsuario = new Label();
        lblUsuario.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        lblNivel = new Label();
        lblNivel.setStyle("-fx-text-fill: #f1c40f; -fx-font-weight: bold;");

        lblPontos = new Label();
        lblPontos.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");

        // Isso cria um espaço flexível que empurra o botão para o canto direito
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnSair = new Button("Sair pro Menu");
        btnSair.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        btnSair.setCursor(javafx.scene.Cursor.HAND);

        btnSair.setFocusTraversable(false);

        this.getChildren().addAll(lblUsuario, lblNivel, lblPontos, spacer, btnSair);
    }

    public void atualizar(Usuario usuario, SessaoJogo sessao) {
        lblUsuario.setText("👤 " + usuario.getNome());
        lblNivel.setText("📊 Nível: " + sessao.getNivelAtual());
        lblPontos.setText("💰 Pontos: " + usuario.getPontuacaoTotal());
    }

    public Button getBtnSair() {
        return btnSair;
    }
}