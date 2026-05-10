package models;

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
	private Ponto alcapao;
	private Ponto itemEspecial;
	private Ponto posicaoOriginalItemEspecial;

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
		if (qtdMoedas <= 0 || qtdMoedas > colunas * linhas - 2) {
			throw new IllegalArgumentException("Quantidade de moedas inválida!");
		}

		do {
			moedas = gerarMoedasAleatorias(qtdMoedas);
			obstaculos = gerarObstaculosAleatorios();

			// Gera o Item Especial e o Alçapão em locais vazios
			itemEspecial = gerarPontoLivre();

			// SALVA A POSIÇÃO ORIGINAL PARA O CRISTAL PODER RENASCER DEPOIS
			posicaoOriginalItemEspecial = itemEspecial;

			alcapao = gerarPontoLivre();

		} while (!verificarAcessibilidade(moedas, obstaculos) ||
				!existeCaminho(new Ponto(0,0), itemEspecial, obstaculos) ||
				!existeCaminho(new Ponto(0,0), alcapao, obstaculos));
	}

	// Método auxiliar para achar um lugar vazio (sem moeda, obstáculo, ou posição inicial)
	private Ponto gerarPontoLivre() {
		int x, y;
		Ponto p;
		do {
			x = random.nextInt(colunas);
			y = random.nextInt(linhas);
			p = new Ponto(x, y);
		} while (moedas.contains(p) || obstaculos[x][y] || (x == 0 && y == 0) || p.equals(itemEspecial));
		return p;
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

				if (i == 0 && j == 0) continue;

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

	public void renascerItemEspecial() {
		if (this.posicaoOriginalItemEspecial != null) {
			this.itemEspecial = this.posicaoOriginalItemEspecial;
		}
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

	public Ponto getAlcapao() { return alcapao; }
	public Ponto getItemEspecial() { return itemEspecial; }

	public boolean isAlcapao(int x, int y) {
		return alcapao != null && alcapao.x() == x && alcapao.y() == y;
	}

	public boolean isItemEspecial(int x, int y) {
		return itemEspecial != null && itemEspecial.x() == x && itemEspecial.y() == y;
	}

	public void coletarItemEspecial() {
		this.itemEspecial = null; // Remove do mapa quando coletado
	}

	public void coletarMoeda(Ponto p) {
		this.moedas.remove(p);
	}
}