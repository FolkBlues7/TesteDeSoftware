package system;

import controllers.LoginController;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputControl;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import run.MainApp;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
class MainAppRealFactoryIT {

    @TempDir
    Path diretorioTemporario;

    @Start
    void start(Stage stage) {
        LoginController loginController = new LoginController(diretorioTemporario.resolve("usuarios.txt"));
        new MainApp(() -> loginController, null).start(stage);
        stage.toFront();
    }

    @Test
    void iniciaJogoUsandoFabricaRealEVoltaAoMenu(FxRobot robot) {
        robot.interact(() -> {
            robot.lookup("#campo-login").queryAs(TextInputControl.class).setText("admin");
            robot.lookup("#campo-senha").queryAs(TextInputControl.class).setText("123");
            robot.lookup("#botao-entrar").queryAs(Button.class).fire();
        });
        WaitForAsyncUtils.waitForFxEvents();

        assertThat((Node) robot.lookup("#game-view").query()).isNotNull();

        robot.interact(() -> robot.lookup("#botao-sair-menu").queryAs(Button.class).fire());
        WaitForAsyncUtils.waitForFxEvents();

        assertThat((Node) robot.lookup("#campo-login").query()).isNotNull();
    }
}
