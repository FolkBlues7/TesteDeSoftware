package run;

import java.util.function.Consumer;

public class Launcher {
    static Consumer<String[]> mainAppAction = MainApp::main;

    public static void main(String[] args) {
        // Ele apenas chama o main da sua classe JavaFX original
        mainAppAction.accept(args);
    }
}
