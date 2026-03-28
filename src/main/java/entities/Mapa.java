package entities;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class Mapa {
	private final int colunas;
	private final int linhas;
	private final List<Ponto> trajeto;
	private boolean[][] obstaculos;
	private List<Ponto> moedas;
	private int moedasColetadas;
	private final Random random;

	// Construtor Padrão (Usa geração aleatória com garantia de caminho)
	public Mapa(int colunas, int linhas) {
		this.colunas = colunas;
		this.linhas = linhas;
		this.trajeto = new ArrayList<>();
		this.moedas = new ArrayList<>();
		this.random = new Random();
		this.moedasColetadas = 0;

		this.trajeto.add(new Ponto(0, 0));
		gerarCenarioAleatorio();
	}

	/**
	 * CONSTRUTOR DE TESTE: Essencial para Rastreabilidade e Testes Sistemáticos.
	 * Permite injetar um cenário fixo para validar bordas e MC/DC.
	 */
	public Mapa(int colunas, int linhas, boolean[][] obstaculosPredefinidos, List<Ponto> moedasPredefinidas) {
		this.colunas = colunas;
		this.linhas = linhas;
		this.obstaculos = obstaculosPredefinidos;
		this.moedas = new ArrayList<>(moedasPredefinidas);
		this.trajeto = new ArrayList<>();
		this.trajeto.add(new Ponto(0, 0));
		this.random = new Random();
		this.moedasColetadas = 0;
	}

	private void gerarCenarioAleatorio() {
		boolean mapaValido = false;
		while (!mapaValido) {
			tentarGerarCenario();
			mapaValido = verificarAcessibilidade();
		}
	}

	private void tentarGerarCenario() {
		this.obstaculos = new boolean[colunas][linhas];
		this.moedas.clear();
		this.moedasColetadas = 0;

		// 1. Gera Obstáculos (aprox. 20% do mapa)
		for (int i = 0; i < colunas; i++) {
			for (int j = 0; j < linhas; j++) {
				if (i == 0 && j == 0) {
					continue;
				}
				if (random.nextDouble() < 0.20) {
					obstaculos[i][j] = true;
				}
			}
		}

		// 2. Gera Moedas (1 a 3)
		int qtdMoedas = random.nextInt(3) + 1;
		while (moedas.size() < qtdMoedas) {
			int x = random.nextInt(colunas);
			int y = random.nextInt(linhas);
			if (!obstaculos[x][y] && !(x == 0 && y == 0)) {
				Ponto novaMoeda = new Ponto(x, y);
				if (!moedas.contains(novaMoeda)) {
					moedas.add(novaMoeda);
				}
			}
		}
	}

	/**
	 * Valida se todas as moedas são alcançáveis a partir da origem (0,0). Utiliza
	 * Algoritmo de Busca em Largura (BFS).
	 */
	private boolean verificarAcessibilidade() {
		for (Ponto moeda : moedas) {
			if (!existeCaminho(new Ponto(0, 0), moeda)) {
				return false;
			}
		}
		return true;
	}

	private boolean existeCaminho(Ponto origem, Ponto destino) {
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

	public boolean podeMover(int x, int y, boolean temItemQuebraObstaculo) {
		boolean dentro = (x >= 0 && x < colunas) && (y >= 0 && y < linhas);
		if (!dentro) {
			return false;
		}

		boolean livre = !obstaculos[x][y] || temItemQuebraObstaculo;
		return livre;
	}

	public void adicionarMovimento(int x, int y) {
		Ponto novoPonto = new Ponto(x, y);
		trajeto.add(novoPonto);

		if (moedas.contains(novoPonto)) {
			moedas.remove(novoPonto);
			moedasColetadas++;
		}
	}

	public boolean faseConcluida() {
		return moedas.isEmpty();
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
}