package models;

public class Usuario {
    private String nome;
    private int pontuacaoTotal;

    public Usuario(String nome) {
        this.nome = nome;
        this.pontuacaoTotal = 0;
    }

    public String getNome() { return nome; }
    public int getPontuacaoTotal() { return pontuacaoTotal; }

    public void adicionarPontos(int pontos) {
        if (pontos > 0) {
            this.pontuacaoTotal += pontos;
        }
    }
}