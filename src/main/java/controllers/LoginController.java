package controllers;

import javafx.scene.Scene;
import javafx.stage.Stage;
import models.Usuario;
import views.LoginView;
import java.util.ArrayList;
import java.util.List;

public class LoginController {
    private Stage stage;
    private List<Usuario> usuariosCadastrados; // Mock de banco de dados

    public LoginController(Stage stage) {
        this.stage = stage;
        this.usuariosCadastrados = new ArrayList<>();
    }

    public void exibirLogin() {
        LoginView loginView = new LoginView();

        loginView.getBotaoEntrar().setOnAction(e -> {
            String nome = loginView.getNomeDigitado();
            if (!nome.isBlank()) {
                Usuario usuario = buscarOuCriarUsuario(nome);
                iniciarJogoParaUsuario(usuario);
            }
        });

        Scene scene = new Scene(loginView, 600, 600);
        stage.setScene(scene);
        stage.setTitle("Login - Moeda Quest");
        stage.show();
    }

    private Usuario buscarOuCriarUsuario(String nome) {
        return usuariosCadastrados.stream()
                .filter(u -> u.getNome().equalsIgnoreCase(nome))
                .findFirst()
                .orElseGet(() -> {
                    Usuario novo = new Usuario(nome);
                    usuariosCadastrados.add(novo);
                    return novo;
                });
    }

    private void iniciarJogoParaUsuario(Usuario usuario) {
        // Passamos o usuário encontrado para o GameController
        GameController gameController = new GameController(stage, usuario);
        gameController.iniciarJogo();
    }
}