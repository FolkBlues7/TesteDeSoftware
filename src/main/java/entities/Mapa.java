package entities;

import common.JavaRandomGenerator;
import common.RandomGenerator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Mapa {
	private final int colunas;
	private final int linhas;
	private final List<Ponto> trajeto;
	private boolean[][] obstaculos;
	private List<Ponto> moedas;

	private int moedasColetadas;
	private RandomGenerator random;

	public Mapa(int linhas, int colunas) {
		this.colunas = linhas;
		this.linhas = colunas;
		this.obstaculos = new boolean[linhas][colunas];
		this.moedas = new ArrayList<>();
		this.trajeto = new ArrayList<>();
		this.trajeto.add(new Ponto(0, 0));
		this.random = new JavaRandomGenerator();
		this.moedasColetadas = 0;
	}

	public void setRandom(RandomGenerator random) {
		this.random = random;
	}

	public void gerarCenarioPredefinido(boolean[][] obstaculos, List<Ponto> moedas) {
		this.obstaculos = obstaculos;
		this.moedas = moedas;
	}

	public void gerarCenarioAleatorio(int qtdMoedas) {
		if (qtdMoedas <= 0){
			throw new IllegalArgumentException("O valor de moedas não pode ser menor ou igual a 0!");
		}

		if (qtdMoedas > colunas * linhas) {
			throw new IllegalArgumentException("O valor de moedas não pode ser maior que a quantidade de espaços no tabuleiro!");
		}

		do {
			moedas = gerarMoedasAleatorias(qtdMoedas);
			obstaculos = gerarObstaculosAleatorios();
		} while (!verificarAcessibilidade(moedas, obstaculos));
	}

	public boolean podeMover(int x, int y) {
		boolean dentro = (x >= 0 && x < colunas) && (y >= 0 && y < linhas);
		if (!dentro) {
			return false;
		}

        return !obstaculos[x][y];
	}

	public void adicionarMovimento(int x, int y) {
		Ponto novoPonto = new Ponto(x, y);


		if (isObstaculo(x, y)) return;

		trajeto.add(novoPonto);

		if (moedas.contains(novoPonto)) {
			moedas.remove(novoPonto);
			moedasColetadas++;
		}
	}

	public boolean faseConcluida() {
		return moedas.isEmpty();
	}

	private List<Ponto> gerarMoedasAleatorias(int qtdMoedas) {
		List<Ponto> moedas = new ArrayList<>();
		for (int i = 0; i < qtdMoedas; i++) {
			int x;
			int y;

			do {
				x = random.nextInt(colunas);
				y = random.nextInt(linhas);
			} while (moedas.contains(new Ponto(x, y)));

			moedas.add(new Ponto(x, y));
		}
		return moedas;
	}

	private boolean[][] gerarObstaculosAleatorios() {
		boolean[][] obstaculos = new boolean[colunas][linhas];
		for (int i = 0; i < colunas; i++) {
			for (int j = 0; j < linhas; j++) {
				if (random.nextDouble() < 0.20) {
					obstaculos[i][j] = true;
				}
			}
		}
		return obstaculos;
	}

	/**
	 * Valida se todas as moedas são alcançáveis a partir da origem (0,0). Utiliza
	 * Algoritmo de Busca em Largura (BFS).
	 */
    boolean verificarAcessibilidade(List<Ponto> moedas, boolean[][] obstaculos) {
		for (Ponto moeda : moedas) {
			if (!existeCaminho(new Ponto(0, 0), moeda, obstaculos)) {
				return false;
			}
		}
		return true;
	}

	private boolean existeCaminho(Ponto origem, Ponto destino, boolean[][]obstaculos) {
		boolean[][] visitado = new boolean[colunas][linhas];
		Queue<Ponto> fila = new LinkedList<>();

		fila.add(origem);
		visitado[origem.x()][origem.y()] = true;

		while (!fila.isEmpty()) {
			Ponto atual = fila.poll();
			if (atual.equals(destino)) {
				return true;
			}

			int[][] direcoes = { { 0, 1 }, { 0, -1 }, { 1, 0 }, { -1, 0 } };
			for (int[] d : direcoes) {
				int nx = atual.x() + d[0];
				int ny = atual.y() + d[1];

				if (nx >= 0 && nx < colunas && ny >= 0 && ny < linhas && !obstaculos[nx][ny] && !visitado[nx][ny]) {
					visitado[nx][ny] = true;
					fila.add(new Ponto(nx, ny));
				}
			}
		}
		return false;
	}

	// Getters
	public List<Ponto> getTrajeto() {
		return trajeto;
	}

	public int getColunas() {
		return colunas;
	}

	public int getLinhas() {
		return linhas;
	}

	public boolean isObstaculo(int x, int y) {
		return obstaculos[x][y];
	}

	public List<Ponto> getMoedas() {
		return new ArrayList<>(moedas);
	}

	public int getMoedasColetadas() {
		return moedasColetadas;
	}


	public boolean[][] getObstaculos() {
		return  obstaculos;
	}
}