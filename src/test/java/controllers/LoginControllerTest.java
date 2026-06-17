package controllers;

import models.Usuario;
import net.jqwik.api.*;
import net.jqwik.api.constraints.StringLength;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class LoginControllerTest {

    private LoginController controller;

    @TempDir
    Path diretorioTemporario;

    private Path arquivo;

    @BeforeEach
    void setup() {
        arquivo = diretorioTemporario.resolve("usuarios.txt");
        controller = new LoginController(arquivo);
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
        assertEquals("Preencha todos os campos!", controller.tentarCadastrar(null, "123"));
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
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(arquivo))) {
            writer.println("admin;comum;0;0;false");   // 5 campos, super=false
            writer.println("alvo;pass;0;0;false");
        }

        // Cria o controller que carregará esse arquivo e não recriará o super‑admin
        controller = new LoginController(arquivo);

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
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(arquivo))) {
            writer.println("user;pass;100;5");              // 4 campos, será ignorada
            writer.println("valido;123;200;3;false");      // 5 campos, será carregado
        }
        // Cria novo controller que vai carregar este arquivo
        controller = new LoginController(arquivo);

        // O usuário "valido" deve existir, o "user" não
        assertNotNull(controller.tentarLogin("valido", "123"));
        assertNull(controller.tentarLogin("user", "pass"));
    }

    @Test
    // Teste estrutural (MC/DC) – IOException ao ler o arquivo
    void carregarArquivoComIOExceptionNaoInterrompeConstructor() throws Exception {
        // Remove o arquivo criado pelo LoginController do @BeforeEach
        Files.deleteIfExists(arquivo);

        // Cria um diretório com o nome do arquivo, forçando IOException ao tentar ler
        Files.createDirectory(arquivo);

        // Deve construir normalmente, tratando a exceção internamente
        assertDoesNotThrow(() -> controller = new LoginController(arquivo));

        // Como o arquivo não pôde ser lido, apenas o admin padrão deve existir
        assertNotNull(controller.tentarLogin("admin", "123"));
    }
    @Property
    // Teste de propriedade
    void qualquerCredencialAleatoriaNaoExistenteRetornaNulo(
            @ForAll @StringLength(max = 20) String login,
            @ForAll @StringLength(max = 20) String senha) throws Exception {
        Path arquivoPropriedade = Files.createTempFile("login-property-", ".txt");
        try {
            LoginController ctrl = new LoginController(arquivoPropriedade);
            if (login.equals("admin") && senha.equals("123")) {
                return;
            }
            assertNull(ctrl.tentarLogin(login, senha));
        } finally {
            Files.deleteIfExists(arquivoPropriedade);
        }
    }

    @Test
    // Teste estrutural: valida contratos e decisões restantes.
    void contratosEFluxosAlternativos() {
        assertThrows(AssertionError.class, () -> new LoginController(null));
        assertEquals("Preencha todos os campos!", controller.tentarCadastrar("user", ""));
        assertEquals("Preencha todos os campos!", controller.tentarCadastrar("user", "  "));
        assertEquals("Preencha todos os campos!", controller.tentarExcluir(null, "123"));
        assertEquals("Preencha todos os campos!", controller.tentarExcluir("user", null));
        assertEquals("Usuário não encontrado ou é admin.", controller.tentarExcluir("admin", "123"));
    }

    @Test
    // Teste de segurança: rejeita entradas que poderiam corromper persistência.
    void cadastroRejeitaCaracteresInvalidos() {
        assertEquals("Entrada inválida!", controller.tentarCadastrar("ab;c", "123"));
        assertEquals("Entrada inválida!", controller.tentarCadastrar("ab\nc", "123"));
        assertEquals("Entrada inválida!", controller.tentarCadastrar("ab c", "123"));
        assertEquals("Entrada inválida!", controller.tentarCadastrar("abc", "12;3"));
        assertEquals("Entrada inválida!", controller.tentarCadastrar("abc", "12 3"));
        assertEquals("Entrada inválida!", controller.tentarCadastrar("abc", "12\t3"));
    }

    @Test
    // Teste de domínio: login permite separadores seguros.
    void cadastroAceitaCaracteresSegurosNoLogin() {
        assertEquals("Cadastrado com sucesso!", controller.tentarCadastrar("ab_c", "123"));
        assertEquals("Cadastrado com sucesso!", controller.tentarCadastrar("ab.c", "123"));
        assertEquals("Cadastrado com sucesso!", controller.tentarCadastrar("ab-c", "123"));
    }

    @Test
    // Teste de fronteira: aceita os limites configurados e rejeita estouros.
    void cadastroRespeitaTamanhosLimite() {
        String loginMinimo = "abc";
        String loginMaximo = "a".repeat(LoginController.LOGIN_MAX_LENGTH);
        String loginGrande = "a".repeat(LoginController.LOGIN_MAX_LENGTH + 1);
        String senhaMinima = "123";
        String senhaMaxima = "b".repeat(LoginController.SENHA_MAX_LENGTH);
        String senhaGrande = "b".repeat(LoginController.SENHA_MAX_LENGTH + 1);

        assertEquals("Cadastrado com sucesso!", controller.tentarCadastrar(loginMinimo, senhaMinima));
        assertEquals("Cadastrado com sucesso!", controller.tentarCadastrar(loginMaximo, senhaMaxima));
        assertEquals("Entrada inválida!", controller.tentarCadastrar("ab", senhaMinima));
        assertEquals("Entrada inválida!", controller.tentarCadastrar(loginGrande, senhaMinima));
        assertEquals("Entrada inválida!", controller.tentarCadastrar("limite", "12"));
        assertEquals("Entrada inválida!", controller.tentarCadastrar("limite2", senhaGrande));
    }

    @Test
    // Teste de segurança: login e exclusão falham sem exceção para payloads maliciosos.
    void loginEExclusaoRejeitamEntradasMaliciosas() {
        String payload = "invasor;senha;999;999;true";

        assertNull(controller.tentarLogin(payload, "123"));
        assertNull(controller.tentarLogin("admin", "12\n3"));
        assertEquals("Entrada inválida!", controller.tentarExcluir(payload, "123"));
        assertEquals("Entrada inválida!", controller.tentarExcluir("admin", "12;3"));
    }

    @Test
    // Teste de segurança: payload persistido como input não cria usuário privilegiado.
    void payloadDeCadastroNaoCorrompeArquivoDeUsuarios() throws Exception {
        String payload = "invasor;senha;999;999;true";

        assertEquals("Entrada inválida!", controller.tentarCadastrar(payload, "123"));
        assertFalse(Files.readString(arquivo).contains(payload));

        LoginController recarregado = new LoginController(arquivo);
        assertNull(recarregado.tentarLogin("invasor", "senha"));
        assertEquals(1, recarregado.getBancoUsuarios().size());
    }

    @Test
    // Teste estrutural: arquivo com campos numéricos inválidos não derruba o sistema.
    void carregarArquivoComNumerosInvalidosNaoInterrompeConstructor() throws Exception {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(arquivo))) {
            writer.println("valido;123;abc;0;false");
        }

        assertDoesNotThrow(() -> controller = new LoginController(arquivo));
    }

    @Test
    // Teste estrutural: valida o construtor padrão isoladamente.
    void construtorPadraoUsaCaminhoConfigurado() {
        String propriedade = "app.usuarios.path";
        String valorAnterior = System.getProperty(propriedade);
        System.setProperty(propriedade, arquivo.toString());
        try {
            Files.deleteIfExists(arquivo);
            LoginController padrao = new LoginController();
            assertTrue(Files.exists(arquivo));
            assertNotNull(padrao.tentarLogin("admin", "123"));
        } catch (Exception e) {
            fail(e);
        } finally {
            if (valorAnterior == null) {
                System.clearProperty(propriedade);
            } else {
                System.setProperty(propriedade, valorAnterior);
            }
        }
    }
}
