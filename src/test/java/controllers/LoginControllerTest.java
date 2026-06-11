package controllers;

import models.Usuario;
import net.jqwik.api.*;
import net.jqwik.api.constraints.StringLength;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class LoginControllerTest {

    private LoginController controller;
    private final String ARQUIVO = "usuarios.txt";

    @BeforeEach
    void setup() throws Exception {
        Files.deleteIfExists(Path.of(ARQUIVO));
        controller = new LoginController();
    }

    @AfterEach
    void cleanup() throws Exception {
        Files.deleteIfExists(Path.of(ARQUIVO));
        // Remove diretório caso tenha sido criado no teste de IOException
        File file = new File(ARQUIVO);
        if (file.isDirectory()) {
            file.delete();
        }
    }

    @Test
    // Teste de domínio
    void loginComCredenciaisCorretasRetornaUsuarioEIncrementaSessao() {
        Usuario admin = controller.tentarLogin("admin", "123");
        assertNotNull(admin);
        assertEquals("admin", admin.getLogin());
        assertEquals(1, admin.getSessoesExecutadas());
    }

    @Test
    // Teste de domínio
    void loginComSenhaIncorretaRetornaNulo() {
        assertNull(controller.tentarLogin("admin", "errada"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t"})
    // Teste de fronteira
    void loginComCredenciaisEmBrancoOuNulasRetornaNulo(String valor) {
        assertNull(controller.tentarLogin(valor, "123"));
        assertNull(controller.tentarLogin("admin", valor));
    }

    @Test
    // Teste de domínio
    void cadastroComSucesso() {
        assertEquals("Cadastrado com sucesso!", controller.tentarCadastrar("novo", "pass"));
        assertNotNull(controller.tentarLogin("novo", "pass"));
    }

    @Test
    // Teste de domínio
    void cadastroComUsuarioExistenteFalha() {
        controller.tentarCadastrar("jogador", "123");
        assertEquals("Usuário já existe!", controller.tentarCadastrar("JOGADOR", "456"));
    }

    @Test
    // Teste estrutural (MC/DC)
    void cadastroComLoginNulo() {
        assertThrows(AssertionError.class, () -> controller.tentarCadastrar(null, "123"));
    }

    @Test
    // Teste estrutural (MC/DC)
    void cadastroComLoginVazio() {
        assertEquals("Preencha todos os campos!", controller.tentarCadastrar("", "123"));
    }

    @Test
    // Teste estrutural (MC/DC)
    void cadastroComSenhaNula() {
        assertEquals("Preencha todos os campos!", controller.tentarCadastrar("user", null));
    }


    @Test
    // Teste de domínio
    void excluirUsuarioPorAdminRemoveELograSucesso() {
        controller.tentarCadastrar("alvo", "pass");
        assertEquals("Usuário removido!", controller.tentarExcluir("alvo", "123"));
        assertNull(controller.tentarLogin("alvo", "pass"));
    }

    @Test
    // Teste de domínio
    void excluirUsuarioInexistenteRetornaMensagem() {
        assertEquals("Usuário não encontrado ou é admin.",
                controller.tentarExcluir("fantasma", "123"));
    }

    @Test
    // Teste estrutural (MC/DC) – admin == null
    void excluirComSenhaAdminIncorretaFalha() {
        controller.tentarCadastrar("alvo", "pass");
        assertEquals("Apenas o admin pode excluir (digite senha do admin).",
                controller.tentarExcluir("alvo", "errada"));
    }

    @Test
    // Teste estrutural (MC/DC) – admin != null e isSuperUsuario() == false
    void excluirQuandoAdminNaoESuperUsuarioFalha() throws Exception {
        // Cria um arquivo com um admin comum (super=false) e um usuário alvo
        try (PrintWriter writer = new PrintWriter(new FileWriter(ARQUIVO))) {
            writer.println("admin;comum;0;0;false");   // 5 campos, super=false
            writer.println("alvo;pass;0;0;false");
        }

        // Cria o controller que carregará esse arquivo e não recriará o super‑admin
        controller = new LoginController();

        // Agora "admin" é um usuário comum e não pode excluir
        String msg = controller.tentarExcluir("alvo", "comum");
        assertEquals("Apenas o admin pode excluir (digite senha do admin).", msg);

        // Confirma que o login com o admin comum funciona
        assertNotNull(controller.tentarLogin("admin", "comum"));
    }

    @Test
    // Teste de fronteira
    void loginComStringsMuitoLongasNaoLancaExcecao() {
        String longa = "a".repeat(10_000);
        assertDoesNotThrow(() -> controller.tentarLogin(longa, "123"));
    }

    @Test
    // Teste estrutural (MC/DC) – linha com quantidade errada de campos (length != 5)
    void carregarArquivoIgnoraLinhasMalformadas() throws Exception {
        // Cria um arquivo manualmente com uma linha de 4 campos e outra correta
        try (PrintWriter writer = new PrintWriter(new FileWriter(ARQUIVO))) {
            writer.println("user;pass;100;5");              // 4 campos, será ignorada
            writer.println("valido;123;200;3;false");      // 5 campos, será carregado
        }
        // Cria novo controller que vai carregar este arquivo
        controller = new LoginController();

        // O usuário "valido" deve existir, o "user" não
        assertNotNull(controller.tentarLogin("valido", "123"));
        assertNull(controller.tentarLogin("user", "pass"));
    }

    @Test
    // Teste estrutural (MC/DC) – IOException ao ler o arquivo
    void carregarArquivoComIOExceptionNaoInterrompeConstructor() throws Exception {
        // Remove o arquivo criado pelo LoginController do @BeforeEach
        Files.deleteIfExists(Path.of(ARQUIVO));

        // Cria um diretório com o nome do arquivo, forçando IOException ao tentar ler
        File dir = new File(ARQUIVO);
        Files.createDirectory(dir.toPath());

        // Deve construir normalmente, tratando a exceção internamente
        assertDoesNotThrow(() -> controller = new LoginController());

        // Como o arquivo não pôde ser lido, apenas o admin padrão deve existir
        assertNotNull(controller.tentarLogin("admin", "123"));
    }
    @Property
    // Teste de propriedade
    void qualquerCredencialAleatoriaNaoExistenteRetornaNulo(
            @ForAll @StringLength(max = 20) String login,
            @ForAll @StringLength(max = 20) String senha) {
        LoginController ctrl = new LoginController();
        if (login.equals("admin") && senha.equals("123")) {
            return;
        }
        assertNull(ctrl.tentarLogin(login, senha));
    }
}