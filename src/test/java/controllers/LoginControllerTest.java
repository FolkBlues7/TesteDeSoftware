package controllers;

import javafx.application.Platform;
import javafx.stage.Stage;
import models.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class LoginControllerTest {

    private LoginController controller;
    private Stage stageMock;
    private final String ARQUIVO_TESTE = "usuarios.txt";

    @BeforeAll
    public static void initJFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Ignora se já estiver inicializado
        }
    }

    @BeforeEach
    public void setup() {
        new File(ARQUIVO_TESTE).delete();
        stageMock = Mockito.mock(Stage.class);
        controller = new LoginController(stageMock);
    }

    @AfterEach
    public void tearDown() {
        new File(ARQUIVO_TESTE).delete();
    }

    // ==========================================
    // 1. TESTES DO FLUXO DE LOGIN
    // ==========================================

    @Test
    public void login_Sucesso() {
        Usuario logado = controller.tentarLogin("admin", "123");
        assertNotNull(logado, "O login deveria funcionar com credenciais corretas.");
        assertEquals("admin", logado.getLogin());
        assertEquals(1, logado.getSessoesExecutadas(), "Deveria incrementar a sessão.");
    }

    @Test
    public void login_FalhaSenhaIncorreta() {
        Usuario logado = controller.tentarLogin("admin", "senhaErrada");
        assertNull(logado, "Não deveria logar com senha incorreta.");
    }

    // ==========================================
    // 2. TESTES DO FLUXO DE CADASTRO
    // ==========================================

    @Test
    public void cadastro_Sucesso() {
        String msg = controller.tentarCadastrar("jogador1", "senha1");
        assertEquals("Cadastrado com sucesso!", msg);

        // Verifica se realmente salva e consegue logar logo em seguida
        assertNotNull(controller.tentarLogin("jogador1", "senha1"));
    }

    @Test
    public void cadastro_FalhaCamposVazios() {
        assertEquals("Preencha todos os campos!", controller.tentarCadastrar("", "123"));
        assertEquals("Preencha todos os campos!", controller.tentarCadastrar("joao", "  "));
        assertEquals("Preencha todos os campos!", controller.tentarCadastrar(null, null));
    }

    @Test
    public void cadastro_FalhaUsuarioJaExiste() {
        controller.tentarCadastrar("jogador1", "senha1"); // Cadastra a primeira vez
        String msg = controller.tentarCadastrar("JOGADOR1", "outrasenha"); // Tenta de novo (case insensitive)
        assertEquals("Usuário já existe!", msg);
    }

    // ==========================================
    // 3. TESTES DO FLUXO DE EXCLUSÃO
    // ==========================================

    @Test
    public void excluir_Sucesso() {
        controller.tentarCadastrar("jogador1", "senha1"); // Cria o alvo

        String msg = controller.tentarExcluir("jogador1", "123"); // Deleta com senha do admin
        assertEquals("Usuário removido!", msg);

        // Tenta logar com o deletado para garantir que sumiu
        assertNull(controller.tentarLogin("jogador1", "senha1"));
    }

    @Test
    public void excluir_FalhaSenhaAdminIncorreta() {
        controller.tentarCadastrar("jogador1", "senha1");

        String msg = controller.tentarExcluir("jogador1", "senhaFalsaAdmin");
        assertEquals("Apenas o admin pode excluir (digite senha do admin).", msg);
    }

    @Test
    public void excluir_FalhaExcluirOProprioAdmin() {
        String msg = controller.tentarExcluir("admin", "123");
        assertEquals("Usuário não encontrado ou é admin.", msg);
    }

    @Test
    public void excluir_FalhaUsuarioInexistente() {
        String msg = controller.tentarExcluir("fantasma", "123");
        assertEquals("Usuário não encontrado ou é admin.", msg);
    }
}