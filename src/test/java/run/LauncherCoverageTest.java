package run;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class LauncherCoverageTest {

    @Test
    void mainAppMainUsaDelegateConfigurado() {
        Consumer<String[]> anterior = MainApp.launchAction;
        AtomicReference<String[]> recebidos = new AtomicReference<>();
        try {
            MainApp.launchAction = recebidos::set;
            String[] args = {"--teste"};

            MainApp.main(args);

            assertThat(recebidos.get()).isSameAs(args);
            assertThat(new MainApp()).isNotNull();
        } finally {
            MainApp.launchAction = anterior;
        }
    }

    @Test
    void launcherMainEncaminhaParaMainApp() {
        Consumer<String[]> anterior = Launcher.mainAppAction;
        AtomicReference<String[]> recebidos = new AtomicReference<>();
        try {
            Launcher.mainAppAction = recebidos::set;
            String[] args = {"--launcher"};

            Launcher.main(args);

            assertThat(recebidos.get()).isSameAs(args);
            assertThat(new Launcher()).isNotNull();
        } finally {
            Launcher.mainAppAction = anterior;
        }
    }
}
