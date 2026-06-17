package views;

import controllers.LoginController;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import models.Usuario;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class LoginView extends VBox {
    private final TextField campoNome;
    private final PasswordField campoSenha;
    private final Button botaoEntrar, botaoCadastrar, botaoExcluir, botaoRanking;
    private final Label mensagemAviso;
    private final LoginController controller;
    private final Consumer<Usuario> onLoginSucesso;

    public LoginView(LoginController controller, Consumer<Usuario> onLoginSucesso) {
        this.controller = controller;
        this.onLoginSucesso = onLoginSucesso;

        this.setSpacing(15);
        this.setAlignment(Pos.CENTER);
        this.setStyle("-fx-background-color: #2c3e50;");

        Label titulo = new Label("MOEDA QUEST");
        titulo.setFont(new Font("Arial", 30));
        titulo.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        campoNome = new TextField();
        campoNome.setPromptText("Login...");
        campoNome.setMaxWidth(250);
        campoNome.setTextFormatter(new TextFormatter<>(filtroLogin()));

        campoSenha = new PasswordField();
        campoSenha.setPromptText("Senha...");
        campoSenha.setMaxWidth(250);
        campoSenha.setTextFormatter(new TextFormatter<>(filtroSenha()));

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

        configurarEventos();
    }

    private void configurarEventos() {
        botaoEntrar.setOnAction(e -> {
            Usuario user = controller.tentarLogin(campoNome.getText(), campoSenha.getText());
            if (user != null) {
                onLoginSucesso.accept(user);
            } else {
                mensagemAviso.setText("Login ou Senha incorretos!");
            }
        });

        botaoCadastrar.setOnAction(e -> {
            String msg = controller.tentarCadastrar(campoNome.getText(), campoSenha.getText());
            mensagemAviso.setText(msg);
        });

        botaoExcluir.setOnAction(e -> {
            String msg = controller.tentarExcluir(campoNome.getText(), campoSenha.getText());
            mensagemAviso.setText(msg);
        });

        botaoRanking.setOnAction(e -> {
            Scene scene = getScene();
            scene.setRoot(new RankingView(controller.getBancoUsuarios(), () -> scene.setRoot(this)));
        });
    }

    private UnaryOperator<TextFormatter.Change> filtroLogin() {
        return change -> {
            String novoTexto = change.getControlNewText();
            if (novoTexto.length() > LoginController.LOGIN_MAX_LENGTH) {
                return null;
            }
            boolean valido = novoTexto.chars().allMatch(c ->
                    Character.isLetterOrDigit(c) || c == '_' || c == '.' || c == '-');
            return valido ? change : null;
        };
    }

    private UnaryOperator<TextFormatter.Change> filtroSenha() {
        return change -> {
            String novoTexto = change.getControlNewText();
            if (novoTexto.length() > LoginController.SENHA_MAX_LENGTH) {
                return null;
            }
            boolean valido = novoTexto.chars().noneMatch(c ->
                    Character.isWhitespace(c) || Character.isISOControl(c) || c == ';');
            return valido ? change : null;
        };
    }
}
