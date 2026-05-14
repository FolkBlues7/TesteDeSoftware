package controllers;

import models.Usuario;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class LoginController {
    private List<Usuario> bancoUsuarios;
    private final String ARQUIVO_PATH = "usuarios.txt";

    public LoginController() {
        this.bancoUsuarios = new ArrayList<>();
        carregarDadosDoArquivo();

        if (bancoUsuarios.isEmpty()) {
            bancoUsuarios.add(new Usuario("admin", "123", true));
            salvarDadosNoArquivo();
        }
    }

    public Usuario tentarLogin(String login, String senha) {
        Usuario user = autenticar(login, senha);
        if (user != null) {
            user.incrementarSessoes();
            salvarDadosNoArquivo();
            return user;
        }
        return null;
    }

    public String tentarCadastrar(String login, String senha) {
        if (login == null || login.isBlank() || senha == null || senha.isBlank()) {
            return "Preencha todos os campos!";
        }
        if (bancoUsuarios.stream().anyMatch(u -> u.getLogin().equalsIgnoreCase(login))) {
            return "Usuário já existe!";
        }
        bancoUsuarios.add(new Usuario(login, senha, false));
        salvarDadosNoArquivo();
        return "Cadastrado com sucesso!";
    }

    public String tentarExcluir(String loginParaDeletar, String senhaAdmin) {
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