package entities;

import java.util.ArrayList;
import java.util.List;

public class Mapa {
	private final int colunas;
	private final int linhas;
	private final List<Ponto> trajeto;
	private boolean[][] obstaculos;

	public Mapa(int colunas, int linhas) {
		this.colunas = colunas;
		this.linhas = linhas;
		this.trajeto = new ArrayList<>();
		this.obstaculos = new boolean[colunas][linhas];
		this.trajeto.add(new Ponto(0, 0)); // Início
	}

	// Lógica para MC/DC: (Dentro do Limite) AND (Não é obstáculo OR Tem Item)
	public boolean podeMover(int x, int y, boolean temItem) {
		boolean dentro = (x >= 0 && x < colunas) && (y >= 0 && y < linhas);
		if (!dentro) {
			return false;
		}

		boolean livre = !obstaculos[x][y] || temItem;
		return livre;
	}

	public void adicionarMovimento(int x, int y) {
		trajeto.add(new Ponto(x, y));
	}

	public List<Ponto> getTrajeto() {
		return trajeto;
	}

	public int getColunas() {
		return colunas;
	}

	public int getLinhas() {
		return linhas;
	}

	public void setObstaculo(int x, int y) {
		obstaculos[x][y] = true;
	}
}
