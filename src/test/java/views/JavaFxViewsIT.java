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

    private Stage stage;

    @Start
    void start(Stage stage) {
        this.stage = stage;
        stage.show();
    }

    @Test
    void loginViewFiltraEntradasInvalidas(FxRobot robot) {
        LoginView view = new LoginView(new LoginController(tempFile()), usuario -> {});
        robot.interact(() -> stage.setScene(new Scene(view, 600, 600)));

        replaceText(robot, "#campo-login", "abc_DEF.12-");
        assertThat(robot.lookup("#campo-login").queryTextInputControl().getText()).isEqualTo("abc_DEF.12-");

        appendText(robot, "#campo-login", "; espaco");
        assertThat(robot.lookup("#campo-login").queryTextInputControl().getText()).isEqualTo("abc_DEF.12-");
        appendText(robot, "#campo-login", "espaco");
        assertThat(robot.lookup("#campo-login").queryTextInputControl().getText()).isEqualTo("abc_DEF.12-espaco");

        replaceText(robot, "#campo-login", "a".repeat(LoginController.LOGIN_MAX_LENGTH + 5));
        assertThat(robot.lookup("#campo-login").queryTextInputControl().getText()).isEqualTo("abc_DEF.12-espaco");

        replaceText(robot, "#campo-senha", "senhaValida123");
        appendText(robot, "#campo-senha", " ");
        appendText(robot, "#campo-senha", "\u0001");
        appendText(robot, "#campo-senha", ";");
        assertThat(robot.lookup("#campo-senha").queryTextInputControl().getText()).isEqualTo("senhaValida123");

        replaceText(robot, "#campo-senha", "b".repeat(LoginController.SENHA_MAX_LENGTH + 5));
        assertThat(robot.lookup("#campo-senha").queryTextInputControl().getText()).isEqualTo("senhaValida123");
    }

    @Test
    void gameViewTrataTodasAsTeclas(FxRobot robot) {
        AtomicBoolean voltouAoMenu = new AtomicBoolean(false);
        Mapa mapa = new Mapa(5, 5);
        mapa.gerarCenarioPredefinido(new boolean[5][5], new ArrayList<>());
        GameController controller = new GameController(
                new Usuario("jogador", "123", false),
                new SessaoJogo(),
                mapa,
                () -> voltouAoMenu.set(true)
        );
        controller.xAtual = 1;
        controller.yAtual = 1;

        GameView view = new GameView(controller, stage);
        robot.interact(() -> {
            view.iniciar();
            stage.show();
            view.requestFocus();
        });

        press(view, KeyCode.UP);
        press(view, KeyCode.DOWN);
        press(view, KeyCode.LEFT);
        press(view, KeyCode.RIGHT);
        press(view, KeyCode.A);
        press(view, KeyCode.R);
        press(view, KeyCode.ESCAPE);

        assertThat(voltouAoMenu).isTrue();
    }

    @Test
    void canvasViewRenderizaComESemElementosOpcionais(FxRobot robot) {
        Mapa mapaCompleto = new Mapa(4, 4);
        boolean[][] obstaculos = new boolean[4][4];
        obstaculos[0][1] = true;
        mapaCompleto.gerarCenarioPredefinido(obstaculos, List.of(new Ponto(1, 0)));
        mapaCompleto.setItemEspecial(new Ponto(2, 0));
        mapaCompleto.setAlcapao(new Ponto(3, 0));
        mapaCompleto.adicionarMovimento(1, 0);

        CanvasView canvasView = new CanvasView(mapaCompleto);
        robot.interact(() -> {
            stage.setScene(new Scene(canvasView));
            canvasView.render(mapaCompleto);
        });

        Mapa mapaVazio = new Mapa(2, 2);
        mapaVazio.gerarCenarioPredefinido(new boolean[2][2], new ArrayList<>());
        mapaVazio.getTrajeto().clear();
        robot.interact(() -> canvasView.render(mapaVazio));

        assertThat(canvasView.lookup("#game-canvas")).isNotNull();
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

    private static java.nio.file.Path tempFile() {
        try {
            return java.nio.file.Files.createTempFile("login-view-", ".txt");
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
