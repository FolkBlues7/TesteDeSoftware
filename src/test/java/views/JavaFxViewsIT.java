package views;

import controllers.GameController;
import controllers.LoginController;
import javafx.scene.Scene;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import models.Mapa;
import models.Ponto;
import models.SessaoJogo;
import models.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
class JavaFxViewsIT {

    private static final String CAMPO_LOGIN = "#campo-login";
    private static final String CAMPO_SENHA = "#campo-senha";
    private static final String GAME_CANVAS = "#game-canvas";

    private Stage stage;

    @Start
    void start(Stage stage) {
        this.stage = stage;
        stage.show();
    }

    @Test
    void loginViewFiltraEntradasInvalidas(FxRobot robot) {
        loginView(robot);

        replaceText(robot, CAMPO_LOGIN, "abc_DEF.12-");
        assertInputText(robot, CAMPO_LOGIN, "abc_DEF.12-");

        appendText(robot, CAMPO_LOGIN, "; espaco");
        assertInputText(robot, CAMPO_LOGIN, "abc_DEF.12-");
        appendText(robot, CAMPO_LOGIN, "espaco");
        assertInputText(robot, CAMPO_LOGIN, "abc_DEF.12-espaco");

        replaceText(robot, CAMPO_LOGIN, "a".repeat(LoginController.LOGIN_MAX_LENGTH + 5));
        assertInputText(robot, CAMPO_LOGIN, "abc_DEF.12-espaco");

        replaceText(robot, CAMPO_SENHA, "senhaValida123");
        appendText(robot, CAMPO_SENHA, " ");
        appendText(robot, CAMPO_SENHA, "\u0001");
        appendText(robot, CAMPO_SENHA, ";");
        assertInputText(robot, CAMPO_SENHA, "senhaValida123");

        replaceText(robot, CAMPO_SENHA, "b".repeat(LoginController.SENHA_MAX_LENGTH + 5));
        assertInputText(robot, CAMPO_SENHA, "senhaValida123");
    }

    @Test
    void gameViewTrataTodasAsTeclas(FxRobot robot) {
        AtomicBoolean voltouAoMenu = new AtomicBoolean(false);
        GameView view = gameViewComMapa(robot, mapaVazio(5), () -> voltouAoMenu.set(true));

        pressAll(view, KeyCode.UP, KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT, KeyCode.A, KeyCode.R, KeyCode.ESCAPE);

        assertThat(voltouAoMenu).isTrue();
    }

    @Test
    void canvasViewRenderizaComESemElementosOpcionais(FxRobot robot) {
        Mapa mapaCompleto = mapaCompletoParaCanvas();
        CanvasView canvasView = new CanvasView(mapaCompleto);
        robot.interact(() -> {
            stage.setScene(new Scene(canvasView));
            canvasView.render(mapaCompleto);
        });

        Mapa mapaVazio = mapaVazio(2);
        mapaVazio.getTrajeto().clear();
        robot.interact(() -> canvasView.render(mapaVazio));

        assertThat(canvasView.lookup(GAME_CANVAS)).isNotNull();
    }

    private LoginView loginView(FxRobot robot) {
        LoginView view = new LoginView(new LoginController(tempFile()), usuario -> {});
        robot.interact(() -> stage.setScene(new Scene(view, 600, 600)));
        return view;
    }

    private GameView gameViewComMapa(FxRobot robot, Mapa mapa, Runnable onVoltarMenu) {
        GameController controller = new GameController(
                new Usuario("jogador", "123", false),
                new SessaoJogo(),
                mapa,
                onVoltarMenu
        );
        controller.xAtual = 1;
        controller.yAtual = 1;

        GameView view = new GameView(controller, stage);
        robot.interact(() -> {
            view.iniciar();
            stage.show();
            view.requestFocus();
        });
        return view;
    }

    private static Mapa mapaVazio(int tamanho) {
        Mapa mapa = new Mapa(tamanho, tamanho);
        mapa.gerarCenarioPredefinido(new boolean[tamanho][tamanho], new ArrayList<>());
        return mapa;
    }

    private static Mapa mapaCompletoParaCanvas() {
        Mapa mapa = new Mapa(4, 4);
        boolean[][] obstaculos = new boolean[4][4];
        obstaculos[0][1] = true;
        mapa.gerarCenarioPredefinido(obstaculos, List.of(new Ponto(1, 0)));
        mapa.setItemEspecial(new Ponto(2, 0));
        mapa.setAlcapao(new Ponto(3, 0));
        mapa.adicionarMovimento(1, 0);
        return mapa;
    }

    private static void pressAll(GameView view, KeyCode... keyCodes) {
        for (KeyCode keyCode : keyCodes) {
            press(view, keyCode);
        }
    }

    private static void press(GameView view, KeyCode keyCode) {
        view.getScene().getOnKeyPressed().handle(new KeyEvent(
                KeyEvent.KEY_PRESSED,
                "",
                "",
                keyCode,
                false,
                false,
                false,
                false
        ));
        WaitForAsyncUtils.waitForFxEvents();
    }

    private static void replaceText(FxRobot robot, String seletor, String texto) {
        robot.interact(() -> {
            TextInputControl input = robot.lookup(seletor).queryAs(TextInputControl.class);
            input.replaceText(0, input.getLength(), texto);
        });
    }

    private static void appendText(FxRobot robot, String seletor, String texto) {
        robot.interact(() -> {
            TextInputControl input = robot.lookup(seletor).queryAs(TextInputControl.class);
            input.appendText(texto);
        });
    }

    private static void assertInputText(FxRobot robot, String seletor, String esperado) {
        assertThat(inputText(robot, seletor)).isEqualTo(esperado);
    }

    private static String inputText(FxRobot robot, String seletor) {
        return robot.lookup(seletor).queryTextInputControl().getText();
    }

    private static java.nio.file.Path tempFile() {
        try {
            return java.nio.file.Files.createTempFile("login-view-", ".txt");
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
