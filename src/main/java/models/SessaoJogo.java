package models;

import java.util.ArrayList;
import java.util.List;

public class SessaoJogo {
    private int nivelAtual;
    private boolean temItemEspecial;
    private final List<Mapa> historicoMapas; // Guarda os níveis já visitados

    public SessaoJogo() {
        this.nivelAtual = 1;
        this.temItemEspecial = false;
        this.historicoMapas = new ArrayList<>();
    }

    public int getNivelAtual() { return nivelAtual; }
    public boolean isTemItemEspecial() { return temItemEspecial; }
    public void setTemItemEspecial(boolean temItemEspecial) { this.temItemEspecial = temItemEspecial; }

    public void avancarNivel() {
        this.nivelAtual++;
        this.temItemEspecial = false; // Perde o item ao usar no alçapão
    }

    public void voltarNivel() {
        if (this.nivelAtual > 1) {
            this.nivelAtual--;
        }
        this.temItemEspecial = false; // Perde o item (se tiver) ao cair no alçapão
    }

    // --- Métodos para gerenciar a memória dos mapas ---

    public Mapa getMapaDoNivelAtual() {
        // Como o nível começa em 1 e o índice da lista em 0, subtraímos 1
        int indice = nivelAtual - 1;
        if (indice < historicoMapas.size()) {
            return historicoMapas.get(indice);
        }
        return null; // Retorna nulo se o mapa ainda não foi gerado e salvo
    }

    public void salvarMapa(Mapa mapa) {
        // Adiciona o mapa recém-gerado ao histórico da sessão
        this.historicoMapas.add(mapa);
    }
}