package models;

import java.io.Serializable;

/**
 * Representa um usuário do sistema com informações de login, pontuação e privilégios.
 *
 * <p>Invariante: {@code sessoesExecutadas >= 0}.
 */
public class Usuario implements Serializable {
    private String login;
    private String senha;
    private int pontuacaoTotal;
    private int sessoesExecutadas;
    private boolean superUsuario;

    /**
     * Construtor para criação de novo usuário.
     *
     * <pre>
     * Pré-condição: {@code login} e {@code senha} não nulos.
     * Pós-condição: pontuacaoTotal == 0, sessoesExecutadas == 0.
     * </pre>
     */
    public Usuario(String login, String senha, boolean superUsuario) {
        assert login != null : "Login não pode ser nulo";
        assert senha != null : "Senha não pode ser nula";
        this.login = login;
        this.senha = senha;
        this.superUsuario = superUsuario;
        this.pontuacaoTotal = 0;
        this.sessoesExecutadas = 0;
    }

    /**
     * Construtor usado para restaurar um usuário a partir do arquivo de dados.
     *
     * <pre>
     * Pré-condição: {@code login} e {@code senha} não nulos, {@code sessoes >= 0}.
     * </pre>
     */
    public Usuario(String login, String senha, int pontos, int sessoes, boolean superUsuario) {
        assert login != null : "Login não pode ser nulo";
        assert senha != null : "Senha não pode ser nula";
        assert sessoes >= 0 : "Sessões executadas não pode ser negativo";
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

    /**
     * Adiciona pontos à pontuação total do usuário.
     *
     * <pre>
     * Pós-condição: pontuacaoTotal == pontuacaoTotal_antiga + pontos.
     * </pre>
     * Nota: Não há restrição sobre o valor de {@code pontos} (pode ser negativo).
     */
    public void adicionarPontos(int pontos) {
        this.pontuacaoTotal += pontos;
    }

    /**
     * Incrementa o contador de sessões executadas.
     *
     * <pre>
     * Pós-condição: sessoesExecutadas == sessoesExecutadas_antiga + 1.
     * </pre>
     */
    public void incrementarSessoes() {
        this.sessoesExecutadas++;
        assert sessoesExecutadas >= 0 : "Sessões executadas tornou-se negativa";
    }
}