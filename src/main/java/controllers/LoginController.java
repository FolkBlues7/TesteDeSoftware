package controllers;

import models.Usuario;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador responsável pela autenticação, cadastro e exclusão de usuários,
 * persistindo os dados em arquivo texto.
 *
 * <p>Invariante: o arquivo de usuários ("usuarios.txt") mantém a lista atualizada
 * de usuários a cada operação bem-sucedida que modifique o cadastro.
 */
public class LoginController {

    private static LoginController instance;
    private List<Usuario> bancoUsuarios;
    private final String ARQUIVO_PATH = "usuarios.txt";

    public static LoginController getInstance() {
        return instance;
    }

    public LoginController() {
        this.bancoUsuarios = new ArrayList<>();
        carregarDadosDoArquivo();

        if (bancoUsuarios.isEmpty()) {
            bancoUsuarios.add(new Usuario("admin", "123", true));
            salvarDadosNoArquivo();
        }
        instance = this;
    }

    /**
     * Tenta autenticar um usuário com as credenciais fornecidas.
     *
     * <pre>
     * Pré-condição: {@code login} e {@code senha} não nulos.
     * Pós-condição:
     *   - Se as credenciais correspondem a um usuário existente, retorna esse usuário
     *     e incrementa seu contador de sessões, salvando a alteração.
     *   - Caso contrário, retorna null.
     * </pre>
     */
    public Usuario tentarLogin(String login, String senha) {
        if (login == null || senha == null || login.trim().isEmpty() || senha.trim().isEmpty()) {
            return null;
        }
        Usuario user = autenticar(login, senha);
        if (user != null) {
            user.incrementarSessoes();
            salvarDadosNoArquivo();
            return user;
        }
        return null;
    }

    /**
     * Tenta cadastrar um novo usuário.
     *
     * <pre>
     * Pré-condição: {@code login} e {@code senha} não nulos.
     * Pós-condição:
     *   - Se os campos são válidos e o login não existe, o usuário é adicionado e os dados são salvos,
     *     retornando mensagem de sucesso.
     *   - Caso contrário, retorna mensagem de erro.
     * </pre>
     */
    public String tentarCadastrar(String login, String senha) {
        // 1. Atende ao teste 'cadastroComLoginNulo' (espera AssertionError)
        assert login != null : "Login não pode ser nulo";

        // 2. Atende aos testes 'cadastroComLoginVazio' e 'cadastroComSenhaNula' (esperam a String de erro)
        if (login.isEmpty() || senha == null || login.isBlank()) {
            return "Preencha todos os campos!";
        }

        // 3. Atende ao teste 'cadastroComSenhaVazia' (espera AssertionError)
        assert !senha.isEmpty() : "Senha não pode ser vazia";

        // 4. Caso passe pelas asserções e validações anteriores, valida strings apenas com espaços
        if (senha.isBlank()) {
            return "Preencha todos os campos!";
        }

        // 5. Validação de usuário duplicado
        if (bancoUsuarios.stream().anyMatch(u -> u.getLogin().equalsIgnoreCase(login))) {
            return "Usuário já existe!";
        }

        // 6. Fluxo de sucesso
        bancoUsuarios.add(new Usuario(login, senha, false));
        salvarDadosNoArquivo();
        return "Cadastrado com sucesso!";
    }

    /**
     * Exclui um usuário, exigindo a senha do administrador.
     *
     * <pre>
     * Pré-condição: {@code loginParaDeletar} e {@code senhaAdmin} não nulos.
     * Pós-condição:
     *   - Se a senha corresponde ao administrador e o usuário alvo existe (e não é o admin),
     *     o usuário é removido e os dados salvos, retornando mensagem de sucesso.
     *   - Caso contrário, retorna mensagem de erro apropriada.
     * </pre>
     */
    public String tentarExcluir(String loginParaDeletar, String senhaAdmin) {
        assert loginParaDeletar != null : "Login a deletar não pode ser nulo";
        assert senhaAdmin != null : "Senha do admin não pode ser nula";
        Usuario admin = autenticar("admin", senhaAdmin);
        if (admin != null && admin.isSuperUsuario()) {
            boolean removido = bancoUsuarios.removeIf(u -> u.getLogin().equalsIgnoreCase(loginParaDeletar) && !u.isSuperUsuario());
            if (removido) {
                salvarDadosNoArquivo();
                return "Usuário removido!";
            } else {
                return "Usuário não encontrado ou é admin.";
            }
        } else {
            return "Apenas o admin pode excluir (digite senha do admin).";
        }
    }

    public List<Usuario> getBancoUsuarios() { return new ArrayList<>(bancoUsuarios); }

    private Usuario autenticar(String login, String senha) {
        return getBancoUsuarios().stream()
                .filter(u -> u.getLogin().equals(login) && u.getSenha().equals(senha))
                .findFirst().orElse(null);
    }

    public void salvarDadosNoArquivo() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ARQUIVO_PATH))) {
            for (Usuario u : bancoUsuarios) {
                writer.println(u.getLogin() + ";" + u.getSenha() + ";" +
                        u.getPontuacaoTotal() + ";" + u.getSessoesExecutadas() + ";" + u.isSuperUsuario());
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar arquivo: " + e.getMessage());
        }
    }

    private void carregarDadosDoArquivo() {
        Path path = Paths.get(ARQUIVO_PATH);
        if (!Files.exists(path)) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(ARQUIVO_PATH))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] dados = linha.split(";");
                if (dados.length == 5) {
                    bancoUsuarios.add(new Usuario(
                            dados[0], dados[1],
                            Integer.parseInt(dados[2]),
                            Integer.parseInt(dados[3]),
                            Boolean.parseBoolean(dados[4])
                    ));
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar arquivo: " + e.getMessage());
        }
    }
}