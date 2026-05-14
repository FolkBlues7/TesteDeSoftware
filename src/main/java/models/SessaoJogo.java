package models;

import java.util.ArrayList;
import java.util.List;

/**
 * Mantém o estado da sessão atual de jogo, incluindo o nível, a posse do item especial
 * e o histórico de mapas já visitados.
 *
 * <p>Invariante: {@code nivelAtual >= 1}.
 */
public class SessaoJogo {
    private int nivelAtual;
    private boolean temItemEspecial;
    private final List<Mapa> historicoMapas;

    public SessaoJogo() {
        this.nivelAtual = 1;
        this.temItemEspecial = false;
        this.historicoMapas = new ArrayList<>();
        checkInvariant();
    }

    public int getNivelAtual() { return nivelAtual; }
    public boolean isTemItemEspecial() { return temItemEspecial; }

    public void setTemItemEspecial(boolean temItemEspecial) {
        this.temItemEspecial = temItemEspecial;
    }

    /**
     * Avança para o próximo nível.
     *
     * <pre>
     * Pós-condição: nivelAtual == nivelAtual_antigo + 1 e temItemEspecial == false.
     * </pre>
     */
    public void avancarNivel() {
        this.nivelAtual++;
        this.temItemEspecial = false;
        checkInvariant();
    }

    /**
     * Volta ao nível anterior, se possível.
     *
     * <pre>
     * Pós-condição:
     *   - Se nivelAtual > 1, nivelAtual == nivelAtual_antigo - 1.
     *   - temItemEspecial == false.
     *   - Se nivelAtual == 1, o nível não é alterado (continua 1).
     * </pre>
     */
    public void voltarNivel() {
        if (this.nivelAtual > 1) {
            this.nivelAtual--;
        }
        this.temItemEspecial = false;
        checkInvariant();
    }

    /**
     * Obtém o mapa correspondente ao nível atual, se já tiver sido gerado e salvo.
     *
     * <pre>
     * Pré-condição: nivelAtual >= 1.
     * Pós-condição: retorna o mapa salvo no índice (nivelAtual - 1) ou null caso não exista.
     * </pre>
     */
    public Mapa getMapaDoNivelAtual() {
        int indice = nivelAtual - 1;
        if (indice < historicoMapas.size()) {
            return historicoMapas.get(indice);
        }
        return null;
    }

    /**
     * Armazena um mapa recém-gerado no histórico da sessão.
     *
     * <pre>
     * Pré-condição: {@code mapa} não nulo.
     * Pós-condição: o mapa é adicionado ao final da lista de histórico.
     * </pre>
     */
    public void salvarMapa(Mapa mapa) {
        assert mapa != null : "Mapa não pode ser nulo";
        this.historicoMapas.add(mapa);
    }

    private void checkInvariant() {
        assert nivelAtual >= 1 : "Nível atual não pode ser menor que 1";
    }
}