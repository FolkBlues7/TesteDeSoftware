package controllers;

import javafx.scene.Scene;
import javafx.stage.Stage;
import models.Usuario;
import views.LoginView;
import views.RankingView;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class LoginController {
    private Stage stage;
    private List<Usuario> bancoUsuarios;
    private final String ARQUIVO_PATH = "usuarios.txt";

    public LoginController(Stage stage) {
        this.stage = stage;
        this.bancoUsuarios = new ArrayList<>();
        carregarDadosDoArquivo();

        // Garante que sempre exista um admin se a lista estiver vazia
        if (bancoUsuarios.isEmpty()) {
            bancoUsuarios.add(new Usuario("admin", "123", true));
            salvarDadosNoArquivo();
        }

        // NOVO: Se o jogador fechar a janela no "X", salva os dados!
        stage.setOnCloseRequest(event -> salvarDadosNoArquivo());
    }

    public void exibirLogin() {
        LoginView loginView = new LoginView();

        loginView.getBotaoEntrar().setOnAction(e -> {
            Usuario user = autenticar(loginView.getNomeDigitado(), loginView.getSenhaDigitada());
            if (user != null) {
                user.incrementarSessoes();
                salvarDadosNoArquivo();
                iniciarJogo(user); // ATUALIZADO: Passa o usuário para o novo método
            } else {
                loginView.exibirMensagem("Login ou Senha incorretos!");
            }
        });

        loginView.getBotaoCadastrar().setOnAction(e -> {
            String login = loginView.getNomeDigitado();
            String senha = loginView.getSenhaDigitada();
            if (login.isBlank() || senha.isBlank()) {
                loginView.exibirMensagem("Preencha todos os campos!");
                return;
            }
            if (bancoUsuarios.stream().anyMatch(u -> u.getLogin().equalsIgnoreCase(login))) {
                loginView.exibirMensagem("Usuário já existe!");
            } else {
                bancoUsuarios.add(new Usuario(login, senha, false));
                salvarDadosNoArquivo();
                loginView.exibirMensagem("Cadastrado com sucesso!");
            }
        });

        loginView.getBotaoExcluir().setOnAction(e -> {
            String loginParaDeletar = loginView.getNomeDigitado();
            Usuario admin = autenticar("admin", loginView.getSenhaDigitada());

            if (admin != null && admin.isSuperUsuario()) {
                boolean removido = bancoUsuarios.removeIf(u -> u.getLogin().equalsIgnoreCase(loginParaDeletar) && !u.isSuperUsuario());
                if (removido) {
                    salvarDadosNoArquivo();
                    loginView.exibirMensagem("Usuário removido!");
                } else {
                    loginView.exibirMensagem("Usuário não encontrado ou é admin.");
                }
            } else {
                loginView.exibirMensagem("Apenas o admin pode excluir (digite senha do admin).");
            }
        });

        loginView.getBotaoRanking().setOnAction(e -> {
            stage.getScene().setRoot(new RankingView(bancoUsuarios, this::exibirLogin));
        });

        Scene scene = new Scene(loginView, 600, 600);
        stage.setScene(scene);
        stage.show();
    }

    private Usuario autenticar(String login, String senha) {
        return bancoUsuarios.stream()
                .filter(u -> u.getLogin().equals(login) && u.getSenha().equals(senha))
                .findFirst().orElse(null);
    }

    // ATUALIZADO: O método de iniciar o jogo agora manda o GameController salvar e voltar ao login
    private void iniciarJogo(Usuario usuarioLogado) {
        // Passamos uma ação (Runnable) para o GameController executar quando o jogador apertar ESC
        GameController game = new GameController(stage, usuarioLogado, () -> {
            salvarDadosNoArquivo(); // Salva a pontuação no TXT
            exibirLogin();          // Volta pra tela inicial
        });
        game.iniciarJogo();
    }

    // --- PERSISTÊNCIA ---
    private void salvarDadosNoArquivo() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ARQUIVO_PATH))) {
            for (Usuario u : bancoUsuarios) {
                writer.println(u.getLogin() + ";" + u.getSenha() + ";" +
                        u.getPontuacaoTotal() + ";" + u.getSessoesExecutadas() + ";" + u.isSuperUsuario());
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar arquivo: " + e.getMessage());
        }
    }

    private void carregarDadosDoArquivo() {
        Path path = Paths.get(ARQUIVO_PATH);
        if (!Files.exists(path)) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(ARQUIVO_PATH))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] dados = linha.split(";");
                if (dados.length == 5) {
                    bancoUsuarios.add(new Usuario(
                            dados[0], dados[1],
                            Integer.parseInt(dados[2]),
                            Integer.parseInt(dados[3]),
                            Boolean.parseBoolean(dados[4])
                    ));
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar arquivo: " + e.getMessage());
        }
    }
}