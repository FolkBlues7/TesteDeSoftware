package views;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class LoginView extends VBox {
    private TextField campoNome;
    private PasswordField campoSenha;
    private Button botaoEntrar, botaoCadastrar, botaoExcluir, botaoRanking;
    private Label mensagemAviso;

    public LoginView() {
        this.setSpacing(15);
        this.setAlignment(Pos.CENTER);
        this.setStyle("-fx-background-color: #2c3e50;");

        Label titulo = new Label("MOEDA QUEST");
        titulo.setFont(new Font("Arial", 30));
        titulo.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        campoNome = new TextField();
        campoNome.setPromptText("Login...");
        campoNome.setMaxWidth(250);

        campoSenha = new PasswordField();
        campoSenha.setPromptText("Senha...");
        campoSenha.setMaxWidth(250);

        botaoEntrar = new Button("Entrar");
        botaoCadastrar = new Button("Cadastrar");
        botaoExcluir = new Button("Excluir Usuário");
        botaoRanking = new Button("🏆 Ranking");

        HBox caixaAcao = new HBox(10, botaoEntrar, botaoCadastrar);
        caixaAcao.setAlignment(Pos.CENTER);

        HBox caixaExtra = new HBox(10, botaoRanking, botaoExcluir);
        caixaExtra.setAlignment(Pos.CENTER);

        mensagemAviso = new Label();
        mensagemAviso.setStyle("-fx-text-fill: #f1c40f;");

        this.getChildren().addAll(titulo, campoNome, campoSenha, caixaAcao, caixaExtra, mensagemAviso);
    }

    public String getNomeDigitado() { return campoNome.getText(); }
    public String getSenhaDigitada() { return campoSenha.getText(); }
    public Button getBotaoEntrar() { return botaoEntrar; }
    public Button getBotaoCadastrar() { return botaoCadastrar; }
    public Button getBotaoExcluir() { return botaoExcluir; }
    public Button getBotaoRanking() { return botaoRanking; }
    public void exibirMensagem(String texto) { mensagemAviso.setText(texto); }
}