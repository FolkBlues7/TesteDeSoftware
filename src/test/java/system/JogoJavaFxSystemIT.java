package system;

import controllers.LoginController;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import models.Mapa;
import models.Ponto;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import run.MainApp;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
class JogoJavaFxSystemIT {

    @TempDir
    Path diretorioTemporario;

    @Start
    void start(Stage stage) {
        LoginController loginController = new LoginController(diretorioTemporario.resolve("usuarios.txt"));
        MainApp app = new MainApp(() -> loginController, this::mapaDoNivel);
        app.start(stage);
        stage.toFront();
    }

    @Test
    void jogadorExecutaJornadaCompletaPelaInterface(FxRobot robot) {
        LoginPage login = new LoginPage(robot);

        login.preencher("mario", "senha123").entrar();
        assertThat(login.mensagem()).isEqualTo("Login ou Senha incorretos!");

        login.preencher("mario", "senha123").cadastrar();
        assertThat(login.mensagem()).isEqualTo("Cadastrado com sucesso!");

        GamePage jogo = login.preencher("mario", "senha123").entrarComSucesso();
        assertThat(jogo.usuarioLogado()).isEqualTo("mario");
        assertThat(jogo.nivelAtual()).isEqualTo(1);
        assertThat(jogo.pontuacao()).isEqualTo(0);

        jogo.teclar(KeyCode.RIGHT);
        assertThat(jogo.pontuacao()).isEqualTo(10);

        jogo.teclar(KeyCode.LEFT);
        jogo.teclar(KeyCode.LEFT);
        assertThat(jogo.pontuacao()).isEqualTo(10);

        jogo.teclar(KeyCode.RIGHT);
        jogo.teclar(KeyCode.RIGHT);
        jogo.teclar(KeyCode.RIGHT);
        assertThat(jogo.nivelAtual()).isEqualTo(2);
        assertThat(jogo.pontuacao()).isEqualTo(10);

        jogo.teclar(KeyCode.RIGHT);
        assertThat(jogo.nivelAtual()).isEqualTo(1);

        jogo.teclar(KeyCode.R);
        assertThat(jogo.nivelAtual()).isEqualTo(1);

        jogo.teclar(KeyCode.ESCAPE);
        login.aguardar();

        jogo = login.preencher("mario", "senha123").entrarComSucesso();
        jogo.sairPeloBotao();
        login.aguardar();

        RankingPage ranking = login.abrirRanking();
        RankingEntry mario = ranking.entry("mario");
        assertThat(mario.login()).isEqualTo("mario");
        assertThat(mario.pontos()).isEqualTo(10);
        assertThat(mario.sessoes()).isEqualTo(2);
        ranking.voltar();
        login.aguardar();

        login.preencher("mario", "123").excluir();
        assertThat(login.mensagem()).isEqualTo("Usuário removido!");
    }

    private Mapa mapaDoNivel(int nivel) {
        if (nivel == 1) {
            Mapa mapa = new Mapa(4, 4);
            mapa.gerarCenarioPredefinido(new boolean[4][4], List.of(new Ponto(1, 0)));
            mapa.setItemEspecial(new Ponto(2, 0));
            mapa.setAlcapao(new Ponto(3, 0));
            return mapa;
        }

        Mapa mapa = new Mapa(4, 4);
        mapa.gerarCenarioPredefinido(new boolean[4][4], List.of());
        mapa.setAlcapao(new Ponto(1, 0));
        return mapa;
    }

    private static final class LoginPage {
        private final FxRobot robot;

        private LoginPage(FxRobot robot) {
            this.robot = robot;
            aguardar();
        }

        private void aguardar() {
            robot.lookup("#campo-login").query();
        }

        private LoginPage preencher(String login, String senha) {
            robot.interact(() -> {
                ((TextInputControl) robot.lookup("#campo-login").query()).setText(login);
                ((TextInputControl) robot.lookup("#campo-senha").query()).setText(senha);
            });
            return this;
        }

        private LoginPage cadastrar() {
            fire("#botao-cadastrar");
            return this;
        }

        private LoginPage entrar() {
            fire("#botao-entrar");
            return this;
        }

        private GamePage entrarComSucesso() {
            fire("#botao-entrar");
            return new GamePage(robot);
        }

        private LoginPage excluir() {
            fire("#botao-excluir");
            return this;
        }

        private RankingPage abrirRanking() {
            fire("#botao-ranking");
            return new RankingPage(robot);
        }

        private String mensagem() {
            return ((Label) robot.lookup("#mensagem-aviso").query()).getText();
        }

        private void fire(String seletor) {
            robot.interact(() -> robot.lookup(seletor).queryAs(Button.class).fire());
            WaitForAsyncUtils.waitForFxEvents();
        }

    }

    private static final class GamePage {
        private static final Pattern NUMERO = Pattern.compile("-?\\d+");
        private final FxRobot robot;

        private GamePage(FxRobot robot) {
            this.robot = robot;
            robot.lookup("#game-view").query();
            focarJogo();
        }

        private void teclar(KeyCode keyCode) {
            focarJogo();
            Node node = robot.lookup("#game-view").query();
            robot.interact(() -> node.getScene().getOnKeyPressed().handle(new KeyEvent(
                    KeyEvent.KEY_PRESSED,
                    "",
                    "",
                    keyCode,
                    false,
                    false,
                    false,
                    false
            )));
            WaitForAsyncUtils.waitForFxEvents();
        }

        private void sairPeloBotao() {
            robot.interact(() -> robot.lookup("#botao-sair-menu").queryAs(Button.class).fire());
            WaitForAsyncUtils.waitForFxEvents();
        }

        private String usuarioLogado() {
            return texto("#hud-usuario").replace("👤", "").trim();
        }

        private int nivelAtual() {
            return primeiroInteiro(texto("#hud-nivel"));
        }

        private int pontuacao() {
            return primeiroInteiro(texto("#hud-pontos"));
        }

        private String texto(String seletor) {
            return ((Label) robot.lookup(seletor).query()).getText();
        }

        private int primeiroInteiro(String texto) {
            Matcher matcher = NUMERO.matcher(texto);
            if (!matcher.find()) {
                throw new AssertionError("Texto sem numero inteiro: " + texto);
            }
            return Integer.parseInt(matcher.group());
        }

        private void focarJogo() {
            Node node = robot.lookup("#game-view").query();
            robot.interact(node::requestFocus);
            WaitForAsyncUtils.waitForFxEvents();
        }
    }

    private static final class RankingPage {
        private final FxRobot robot;

        private RankingPage(FxRobot robot) {
            this.robot = robot;
            robot.lookup("#ranking-view").query();
        }

        private RankingEntry entry(String login) {
            return Arrays.stream(conteudo().split("\\R"))
                    .map(String::trim)
                    .filter(linha -> linha.startsWith(login + " "))
                    .map(this::parseEntry)
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Ranking sem linha para login: " + login));
        }

        private String conteudo() {
            return ((TextArea) robot.lookup("#ranking-lista").query()).getText();
        }

        private RankingEntry parseEntry(String linha) {
            String[] colunas = linha.split("\\s*\\|\\s*");
            if (colunas.length != 3) {
                throw new AssertionError("Linha de ranking invalida: " + linha);
            }
            return new RankingEntry(
                    colunas[0].trim(),
                    Integer.parseInt(colunas[1].trim()),
                    Integer.parseInt(colunas[2].trim())
            );
        }

        private void voltar() {
            robot.interact(() -> robot.lookup("#botao-voltar-ranking").queryAs(Button.class).fire());
            WaitForAsyncUtils.waitForFxEvents();
        }
    }

    private record RankingEntry(String login, int pontos, int sessoes) {
    }
}
