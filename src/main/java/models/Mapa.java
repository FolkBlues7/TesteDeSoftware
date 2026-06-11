package models;

import common.JavaRandomGenerator;
import common.RandomGenerator;

import java.util.*;

/**
 * Representa o mapa do jogo, contendo obstáculos, moedas, alçapão e item especial.
 *
 * <p>Invariantes da classe:
 * <ul>
 *   <li>Os conjuntos de obstáculos, moedas, alçapão e item especial são mutuamente disjuntos.</li>
 *   <li>Nenhum desses elementos ocupa a posição (0,0).</li>
 *   <li>A lista de espaços vazios é derivada e mantida consistente com as ocupações atuais.</li>
 * </ul>
 *
 * <p>Os métodos públicos que alteram o estado interno verificam essas invariantes ao final da execução,
 * utilizando asserções que podem ser habilitadas com a opção {@code -ea} da JVM.
 */
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

	/**
	 * Cria um mapa vazio com dimensões dadas e a posição (0,0) como ponto inicial do trajeto.
	 *
	 * @param linhas  número de linhas (altura)
	 * @param colunas número de colunas (largura)
	 */
	public Mapa(int linhas, int colunas) {
		this.colunas = colunas;
		this.linhas = linhas;
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
		checkInvariant();
	}

	/**
	 * Injeta um gerador de números aleatórios para testes determinísticos.
	 */
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

	/**
	 * Configura o mapa com obstáculos e moedas pré-definidos.
	 *
	 * <pre>
	 * Pré-condição:
	 *   - obstaculosMatriz.length == colunas e obstaculosMatriz[0].length == linhas.
	 *   - moedasList não é nula e não contém elementos nulos.
	 *   - Nenhuma moeda coincide com (0,0) ou com um obstáculo informado.
	 * Pós-condição:
	 *   - O mapa reflete exatamente os obstáculos e moedas fornecidos.
	 *   - A invariante da classe é mantida.
	 * </pre>
	 */
	public void gerarCenarioPredefinido(boolean[][] obstaculosMatriz, List<Ponto> moedasList) {
		assert obstaculosMatriz != null && obstaculosMatriz.length == colunas &&
				obstaculosMatriz[0].length == linhas : "Dimensões da matriz de obstáculos inválidas";
		assert moedasList != null && moedasList.stream().allMatch(Objects::nonNull) : "Lista de moedas inválida";

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
		checkInvariant();
	}

	/**
	 * Gera um cenário aleatório com uma quantidade especificada de moedas.
	 *
	 * <pre>
	 * Pré-condição:
	 *   - random != null (deve ter sido configurado).
	 *   - qtdMoedas >= 1 e qtdMoedas + 2 < colunas * linhas.
	 * Pós-condição:
	 *   - Existem exatamente qtdMoedas moedas no mapa.
	 *   - Os elementos alcapao e itemEspecial são criados e não são nulos.
	 *   - Todos os pontos importantes (moedas, alçapão, item especial) são alcançáveis a partir de (0,0).
	 *   - A invariante da classe é mantida.
	 * </pre>
	 */
	public void gerarCenarioAleatorio(int qtdMoedas) {
		assert qtdMoedas >= 1 && qtdMoedas + 2 < colunas * linhas
				: "Quantidade de moedas inválida para o tamanho do mapa";

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
		checkInvariant();
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

	/**
	 * Verifica se o movimento para a coordenada (x, y) é permitido.
	 *
	 * @return {@code true} se a posição está dentro dos limites e não é um obstáculo.
	 */
	public boolean podeMover(int x, int y) {
		return (x >= 0 && x < colunas && y >= 0 && y < linhas) && !obstaculos.contains(new Ponto(x, y));
	}

	/**
	 * Registra um movimento para a posição (x, y). Se a posição não contiver obstáculo,
	 * ela é adicionada ao trajeto. Caso haja uma moeda na posição, ela é coletada.
	 *
	 * <pre>
	 * Pós-condição:
	 *   - Se a célula não é obstáculo, o trajeto inclui o ponto.
	 *   - Se a célula continha uma moeda, ela é removida e {@code moedasColetadas} é incrementado.
	 *   - A invariante da classe é mantida.
	 * </pre>
	 */
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
		checkInvariant();
	}

	/**
	 * Indica se a fase foi concluída (todas as moedas foram coletadas).
	 *
	 * <pre>
	 * Pós-condição: retorna true sse {@code moedas} está vazio.
	 * </pre>
	 */
	public boolean faseConcluida() { return moedas.isEmpty(); }

	/**
	 * Recoloca o item especial na sua posição original, caso tenha sido coletado.
	 *
	 * <pre>
	 * Pós-condição: itemEspecial == posicaoOriginalItemEspecial (não nulo se definido).
	 * </pre>
	 */
	public void renascerItemEspecial() {
		this.itemEspecial = posicaoOriginalItemEspecial;
		atualizarEspacosVazios();
		checkInvariant();
	}

	public List<Ponto> getTrajeto() { return trajeto; }
	public int getColunas() { return colunas; }
	public int getLinhas() { return linhas; }

	/**
	 * Verifica se a célula (x, y) contém um obstáculo.
	 *
	 * <pre>
	 * Pré-condição: (x, y) dentro dos limites do mapa (não verificado internamente para permitir consulta de borda).
	 * </pre>
	 */
	public boolean isObstaculo(int x, int y) { return obstaculos.contains(new Ponto(x, y)); }

	public List<Ponto> getMoedas() { return new ArrayList<>(moedas); }
	public int getMoedasColetadas() { return moedasColetadas; }
	public Ponto getAlcapao() { return alcapao; }
	public Ponto getItemEspecial() { return itemEspecial; }

	/**
	 * Define a posição do alçapão.
	 *
	 * <pre>
	 * Pré-condição: {@code alcapao} não nulo.
	 * Pós-condição: a nova posição é armazenada e os espaços vazios são atualizados.
	 * </pre>
	 */
	public void setAlcapao(Ponto alcapao) {
		this.alcapao = alcapao;
		atualizarEspacosVazios();
		checkInvariant();
	}

	/**
	 * Define a posição do item especial e registra sua posição original.
	 *
	 * <pre>
	 * Pré-condição: {@code itemEspecial} não nulo.
	 * Pós-condição: a posição atual e original são atualizadas.
	 * </pre>
	 */
	public void setItemEspecial(Ponto itemEspecial) {
		this.itemEspecial = itemEspecial;
		this.posicaoOriginalItemEspecial = itemEspecial;
		atualizarEspacosVazios();
		checkInvariant();
	}

	public boolean isAlcapao(int x, int y) { return alcapao != null && alcapao.equals(new Ponto(x, y)); }
	public boolean isItemEspecial(int x, int y) { return itemEspecial != null && itemEspecial.equals(new Ponto(x, y)); }

	/**
	 * Remove o item especial do mapa (coleta).
	 *
	 * <pre>
	 * Pré-condição: existe um item especial (itemEspecial != null).
	 * Pós-condição: itemEspecial torna-se null.
	 * </pre>
	 */
	public void coletarItemEspecial() {
		assert itemEspecial != null : "Nenhum item especial para coletar";
		this.itemEspecial = null;
		atualizarEspacosVazios();
		checkInvariant();
	}

	/**
	 * Remove a moeda indicada do mapa.
	 *
	 * <pre>
	 * Pré-condição: a moeda existe na lista de moedas.
	 * Pós-condição: a moeda é removida e {@code moedasColetadas} incrementado.
	 * </pre>
	 */
	public void coletarMoeda(Ponto p) {
		assert moedas.contains(p) : "Moeda não encontrada na posição " + p;
		moedas.remove(p);
		moedasColetadas++;
		atualizarEspacosVazios();
		checkInvariant();
	}

	/**
	 * Verifica a invariante da classe: os conjuntos de pontos ocupados são disjuntos,
	 * a origem (0,0) nunca está ocupada e a lista de espaços vazios está consistente.
	 */
	private void checkInvariant() {
		Set<Ponto> ocupados = new HashSet<>();
		ocupados.addAll(obstaculos);
		ocupados.addAll(moedas);
		if (alcapao != null) ocupados.add(alcapao);
		if (itemEspecial != null) ocupados.add(itemEspecial);

		int totalElementos = obstaculos.size() + moedas.size()
				+ (alcapao == null ? 0 : 1)
				+ (itemEspecial == null ? 0 : 1);
		assert ocupados.size() == totalElementos : "Sobreposição detectada entre elementos do mapa";
		assert !ocupados.contains(new Ponto(0, 0)) : "A posição (0,0) não pode estar ocupada";

		// =========================================================================
		// ADICIONE ESTA LINHA AQUI:
		// Como (0,0) não é um espaço vazio para spawn, precisamos considerá-lo ocupado
		// para que o cálculo de vazios batam perfeitamente com atualizarEspacosVazios()
		// =========================================================================
		ocupados.add(new Ponto(0, 0));
		// =========================================================================

		Set<Ponto> vaziosEsperados = new HashSet<>();
		for (int x = 0; x < colunas; x++) {
			for (int y = 0; y < linhas; y++) {
				Ponto p = new Ponto(x, y);
				if (!ocupados.contains(p)) vaziosEsperados.add(p);
			}
		}
		assert new HashSet<>(espacosVazios).equals(vaziosEsperados) : "Lista de espaços vazios inconsistente";
	}
}