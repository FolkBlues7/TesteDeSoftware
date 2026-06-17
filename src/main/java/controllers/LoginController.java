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

    private static final String PROPRIEDADE_ARQUIVO = "app.usuarios.path";
    public static final int LOGIN_MIN_LENGTH = 3;
    public static final int LOGIN_MAX_LENGTH = 20;
    public static final int SENHA_MIN_LENGTH = 3;
    public static final int SENHA_MAX_LENGTH = 32;
    private static final String MENSAGEM_CAMPOS_OBRIGATORIOS = "Preencha todos os campos!";
    private static final String MENSAGEM_ENTRADA_INVALIDA = "Entrada inválida!";
    private static LoginController instance;
    private final List<Usuario> bancoUsuarios;
    private final Path arquivoPath;

    public static LoginController getInstance() {
        return instance;
    }

    public LoginController() {
        this(Path.of(System.getProperty(PROPRIEDADE_ARQUIVO, "usuarios.txt")));
    }

    public LoginController(Path arquivoPath) {
        assert arquivoPath != null : "Caminho do arquivo não pode ser nulo";
        this.arquivoPath = arquivoPath;
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
        if (credenciaisValidas(login, senha)) {
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
        if (camposEmBranco(login, senha)) {
            return MENSAGEM_CAMPOS_OBRIGATORIOS;
        }
        if (credenciaisValidas(login, senha)) {
            return MENSAGEM_ENTRADA_INVALIDA;
        }

        if (bancoUsuarios.stream().anyMatch(u -> u.getLogin().equalsIgnoreCase(login))) {
            return "Usuário já existe!";
        }

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
        if (camposEmBranco(loginParaDeletar, senhaAdmin)) {
            return MENSAGEM_CAMPOS_OBRIGATORIOS;
        }
        if (loginValido(loginParaDeletar) || senhaValida(senhaAdmin)) {
            return MENSAGEM_ENTRADA_INVALIDA;
        }
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

    public static boolean loginValido(String login) {
        if (login == null || login.length() < LOGIN_MIN_LENGTH || login.length() > LOGIN_MAX_LENGTH) {
            return true;
        }
        return !login.chars().allMatch(c ->
                Character.isLetterOrDigit(c) || c == '_' || c == '.' || c == '-');
    }

    public static boolean senhaValida(String senha) {
        if (senha == null || senha.length() < SENHA_MIN_LENGTH || senha.length() > SENHA_MAX_LENGTH) {
            return true;
        }
        return senha.chars().anyMatch(c ->
                Character.isWhitespace(c) || Character.isISOControl(c) || c == ';');
    }

    private static boolean credenciaisValidas(String login, String senha) {
        return loginValido(login) || senhaValida(senha);
    }

    private static boolean camposEmBranco(String login, String senha) {
        return login == null || senha == null || login.isBlank() || senha.isBlank();
    }

    public void salvarDadosNoArquivo() {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(arquivoPath))) {
            for (Usuario u : bancoUsuarios) {
                writer.println(u.getLogin() + ";" + u.getSenha() + ";" +
                        u.getPontuacaoTotal() + ";" + u.getSessoesExecutadas() + ";" + u.isSuperUsuario());
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar arquivo: " + e.getMessage());
        }
    }

    private void carregarDadosDoArquivo() {
        if (!Files.exists(arquivoPath)) return;

        try (BufferedReader reader = Files.newBufferedReader(arquivoPath)) {
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
        } catch (IOException | NumberFormatException e) {
            System.err.println("Erro ao carregar arquivo: " + e.getMessage());
        }
    }
}
