package models;

import java.io.Serializable;

public class Usuario implements Serializable {
    private String login;
    private String senha;
    private int pontuacaoTotal;
    private int sessoesExecutadas;
    private boolean superUsuario;

    public Usuario(String login, String senha, boolean superUsuario) {
        this.login = login;
        this.senha = senha;
        this.superUsuario = superUsuario;
        this.pontuacaoTotal = 0;
        this.sessoesExecutadas = 0;
    }

    // Construtor usado para carregar do arquivo
    public Usuario(String login, String senha, int pontos, int sessoes, boolean superUsuario) {
        this.login = login;
        this.senha = senha;
        this.pontuacaoTotal = pontos;
        this.sessoesExecutadas = sessoes;
        this.superUsuario = superUsuario;
    }

    public String getLogin() { return login; }
    public String getNome() { return login; } // Compatibilidade com HUD
    public String getSenha() { return senha; }
    public int getPontuacaoTotal() { return pontuacaoTotal; }
    public int getSessoesExecutadas() { return sessoesExecutadas; }
    public boolean isSuperUsuario() { return superUsuario; }

    public void adicionarPontos(int pontos) { this.pontuacaoTotal += pontos; }
    public void incrementarSessoes() { this.sessoesExecutadas++; }
}