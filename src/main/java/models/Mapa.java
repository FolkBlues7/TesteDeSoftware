package models;

import common.JavaRandomGenerator;
import common.RandomGenerator;

import java.util.*;

public class Mapa {
	private final int colunas;
	private final int linhas;
	private final List<Ponto> trajeto;
	private final List<Ponto> moedas;
	private final List<Ponto> obstaculos;
	private List<Ponto> espacosVazios;
	private Ponto alcapao;
	private Ponto itemEspecial;
	private Ponto posicaoOriginalItemEspecial;
	private int moedasColetadas;
	private RandomGenerator random;

	public Mapa(int linhas, int colunas) {
		this.colunas = linhas;
		this.linhas = colunas;
		this.moedas = new ArrayList<>();
		this.obstaculos = new ArrayList<>();
		this.trajeto = new ArrayList<>();
		this.trajeto.add(new Ponto(0, 0));
		this.random = new JavaRandomGenerator();
		this.moedasColetadas = 0;
		this.alcapao = null;
		this.itemEspecial = null;
		this.posicaoOriginalItemEspecial = null;
		atualizarEspacosVazios();
	}

	public void setRandom(RandomGenerator random) {
		this.random = random;
	}

	private void atualizarEspacosVazios() {
		Set<Ponto> ocupados = new HashSet<>();
		ocupados.addAll(obstaculos);
		ocupados.addAll(moedas);
		ocupados.addAll(Arrays.asList(alcapao, itemEspecial, new Ponto(0, 0)));
		ocupados.remove(null);

		espacosVazios = new ArrayList<>();
		for (int x = 0; x < colunas; x++) {
			for (int y = 0; y < linhas; y++) {
				Ponto p = new Ponto(x, y);
				if (!ocupados.contains(p)) espacosVazios.add(p);
			}
		}
	}

	public void gerarCenarioPredefinido(boolean[][] obstaculosMatriz, List<Ponto> moedasList) {
		this.moedas.clear();
		this.obstaculos.clear();
		for (int x = 0; x < colunas; x++) {
			for (int y = 0; y < linhas; y++) {
				if (obstaculosMatriz[x][y]) {
					this.obstaculos.add(new Ponto(x, y));
				}
			}
		}
		this.moedas.addAll(moedasList);
		atualizarEspacosVazios();
	}

	public void gerarCenarioAleatorio(int qtdMoedas) {
		List<Ponto> todasExcetoOrigem = new ArrayList<>();
		for (int x = 0; x < colunas; x++) {
			for (int y = 0; y < linhas; y++) {
				if (x != 0 || y != 0) todasExcetoOrigem.add(new Ponto(x, y));
			}
		}

		for (int i = todasExcetoOrigem.size() - 1; i > 0; i--) {
			int j = random.nextInt(i + 1);
			Ponto temp = todasExcetoOrigem.get(i);
			todasExcetoOrigem.set(i, todasExcetoOrigem.get(j));
			todasExcetoOrigem.set(j, temp);
		}

		moedas.clear();
		for (int i = 0; i < qtdMoedas; i++) {
			moedas.add(todasExcetoOrigem.get(i));
		}

		int idxAlcapao = qtdMoedas;
		int idxCristal = qtdMoedas + 1;
		alcapao = todasExcetoOrigem.get(idxAlcapao);
		itemEspecial = todasExcetoOrigem.get(idxCristal);
		posicaoOriginalItemEspecial = itemEspecial;

		int totalRestantes = todasExcetoOrigem.size() - (idxCristal + 1);
		int qtdObstaculos = (int) (totalRestantes * 0.20);
		obstaculos.clear();
		for (int i = 0; i < qtdObstaculos; i++) {
			obstaculos.add(todasExcetoOrigem.get(idxCristal + 1 + i));
		}

		atualizarEspacosVazios();
		garantirConectividade();
	}

	private void garantirConectividade() {
		List<Ponto> pontosImportantes = new ArrayList<>(moedas);
		pontosImportantes.add(alcapao);
		pontosImportantes.add(itemEspecial);

		for (Ponto destino : pontosImportantes) {
			while (!existeCaminho(new Ponto(0, 0), destino)) {
				List<Ponto> caminho = encontrarCaminhoSemObstaculos(new Ponto(0, 0), destino);
				for (Ponto p : caminho) {
					obstaculos.remove(p);
				}
				atualizarEspacosVazios();
			}
		}
	}

	private boolean existeCaminho(Ponto origem, Ponto destino) {
		boolean[][] visitado = new boolean[colunas][linhas];
		Queue<Ponto> fila = new LinkedList<>();
		fila.add(origem);
		visitado[origem.x()][origem.y()] = true;
		while (!fila.isEmpty()) {
			Ponto atual = fila.poll();
			if (atual.equals(destino)) return true;
			for (int[] d : new int[][]{{0,1},{0,-1},{1,0},{-1,0}}) {
				int nx = atual.x() + d[0];
				int ny = atual.y() + d[1];
				if (nx >= 0 && nx < colunas && ny >= 0 && ny < linhas && !visitado[nx][ny] && !obstaculos.contains(new Ponto(nx, ny))) {
					visitado[nx][ny] = true;
					fila.add(new Ponto(nx, ny));
				}
			}
		}
		return false;
	}

	private List<Ponto> encontrarCaminhoSemObstaculos(Ponto origem, Ponto destino) {
		boolean[][] visitado = new boolean[colunas][linhas];
		Ponto[][] anterior = new Ponto[colunas][linhas];
		Queue<Ponto> fila = new LinkedList<>();
		fila.add(origem);
		visitado[origem.x()][origem.y()] = true;
		while (true) {
			Ponto atual = fila.poll();
			if (atual.equals(destino)) break;
			for (int[] d : new int[][]{{0,1},{0,-1},{1,0},{-1,0}}) {
				int nx = atual.x() + d[0];
				int ny = atual.y() + d[1];
				if (nx >= 0 && nx < getColunas() && ny >= 0 && ny < getLinhas() && !visitado[nx][ny]) {
					visitado[nx][ny] = true;
					anterior[nx][ny] = atual;
					fila.add(new Ponto(nx, ny));
				}
			}
		}
		List<Ponto> caminho = new ArrayList<>();
		Ponto p = destino;
		while (p != null) {
			caminho.add(0, p);
			p = anterior[p.x()][p.y()];
		}
		return caminho;
	}

	public boolean podeMover(int x, int y) {
		return (x >= 0 && x < colunas && y >= 0 && y < linhas) && !obstaculos.contains(new Ponto(x, y));
	}

	public void adicionarMovimento(int x, int y) {
		Ponto novoPonto = new Ponto(x, y);
		boolean isObstaculo = obstaculos.contains(novoPonto);
		if (!isObstaculo) trajeto.add(novoPonto);
		boolean temMoeda = moedas.contains(novoPonto);
		if (temMoeda) {
			moedas.remove(novoPonto);
			moedasColetadas++;
			atualizarEspacosVazios();
		}
	}

	public boolean faseConcluida() { return moedas.isEmpty(); }
	public void renascerItemEspecial() { this.itemEspecial = posicaoOriginalItemEspecial; atualizarEspacosVazios(); }

	public List<Ponto> getTrajeto() { return trajeto; }
	public int getColunas() { return colunas; }
	public int getLinhas() { return linhas; }
	public boolean isObstaculo(int x, int y) { return obstaculos.contains(new Ponto(x, y)); }
	public List<Ponto> getMoedas() { return new ArrayList<>(moedas); }
	public int getMoedasColetadas() { return moedasColetadas; }
	public Ponto getAlcapao() { return alcapao; }
	public Ponto getItemEspecial() { return itemEspecial; }
	public void setAlcapao(Ponto alcapao) { this.alcapao = alcapao; atualizarEspacosVazios(); }
	public void setItemEspecial(Ponto itemEspecial) { this.itemEspecial = itemEspecial; this.posicaoOriginalItemEspecial = itemEspecial; atualizarEspacosVazios(); }
	public boolean isAlcapao(int x, int y) { return alcapao != null && alcapao.equals(new Ponto(x, y)); }
	public boolean isItemEspecial(int x, int y) { return itemEspecial != null && itemEspecial.equals(new Ponto(x, y)); }
	public void coletarItemEspecial() { this.itemEspecial = null; atualizarEspacosVazios(); }
	public void coletarMoeda(Ponto p) { moedas.remove(p); moedasColetadas++; atualizarEspacosVazios(); }
}