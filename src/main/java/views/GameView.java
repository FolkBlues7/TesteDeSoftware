package views;

import controllers.GameController;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class GameView extends BorderPane implements GameController.GameListener {

	private final GameController controller;
	private final Stage stage;
	private CanvasView canvasView;
	private final HUDView hudView;

	public GameView(GameController controller, Stage stage) {
		this.controller = controller;
		this.stage = stage;
		this.hudView = new HUDView();

		setId("game-view");
		setCenter(new Pane());   // placeholder até o primeiro render
		setTop(hudView);
		hudView.getBtnSair().setOnAction(e -> controller.getOnVoltarMenu().run());

		controller.setListener(this);
	}

	public void iniciar() {
		Scene scene = new Scene(this);
		scene.setOnKeyPressed(this::tratarTeclado);
		stage.setScene(scene);
		stage.sizeToScene();
		this.requestFocus();

		// Força a primeira renderização após a cena estar visível,
		// garantindo que o grid e os elementos apareçam imediatamente.
		render();
		atualizarHUD();
	}

	@Override
	public void render() {
		if (canvasView == null) {
			canvasView = new CanvasView(controller.getMapa());
			setCenter(canvasView);
		}
		canvasView.render(controller.getMapa());
	}

	@Override
	public void atualizarHUD() {
		hudView.atualizar(controller.getUsuario(), controller.getSessao());
	}

	private void tratarTeclado(KeyEvent event) {
		int novoX = controller.xAtual;
		int novoY = controller.yAtual;

		switch (event.getCode()) {
			case UP -> novoY--;
			case DOWN -> novoY++;
			case LEFT -> novoX--;
			case RIGHT -> novoX++;
			case R -> {
				controller.carregarNivel();
				return;
			}
			case ESCAPE -> {
				controller.getOnVoltarMenu().run();
				return;
			}
		}

		controller.aplicarRegrasDeMovimento(novoX, novoY);
	}
}
