package controllers;

import javafx.application.Platform;
import javafx.stage.Stage;
import models.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LoginControllerTest {

    private LoginController controller;
    private Stage stageMock;
    private final String ARQUIVO_TESTE = "usuarios.txt";

    @BeforeAll
    public static void initJFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
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

    @Test
    // Teste de domínio
    public void login_Sucesso() {
        Usuario logado = controller.tentarLogin("admin", "123");
        assertNotNull(logado);
        assertEquals("admin", logado.getLogin());
        assertEquals(1, logado.getSessoesExecutadas());
    }

    @Test
    // Teste de domínio
    public void login_FalhaSenhaIncorreta() {
        Usuario logado = controller.tentarLogin("admin", "senhaErrada");
        assertNull(logado);
    }

    @Test
    // Teste de domínio
    public void cadastro_Sucesso() {
        String msg = controller.tentarCadastrar("jogador1", "senha1");
        assertEquals("Cadastrado com sucesso!", msg);
        assertNotNull(controller.tentarLogin("jogador1", "senha1"));
    }

    @Test
    // Teste de domínio
    public void cadastro_FalhaCamposVazios() {
        assertEquals("Preencha todos os campos!", controller.tentarCadastrar("", "123"));
        assertEquals("Preencha todos os campos!", controller.tentarCadastrar("joao", "  "));
        assertEquals("Preencha todos os campos!", controller.tentarCadastrar(null, null));
    }

    @Test
    // Teste de domínio
    public void cadastro_FalhaUsuarioJaExiste() {
        controller.tentarCadastrar("jogador1", "senha1");
        String msg = controller.tentarCadastrar("JOGADOR1", "outrasenha");
        assertEquals("Usuário já existe!", msg);
    }

    @Test
    // Teste de domínio
    public void excluir_Sucesso() {
        controller.tentarCadastrar("jogador1", "senha1");
        String msg = controller.tentarExcluir("jogador1", "123");
        assertEquals("Usuário removido!", msg);
        assertNull(controller.tentarLogin("jogador1", "senha1"));
    }

    @Test
    // Teste de domínio
    public void excluir_FalhaSenhaAdminIncorreta() {
        controller.tentarCadastrar("jogador1", "senha1");
        String msg = controller.tentarExcluir("jogador1", "senhaFalsaAdmin");
        assertEquals("Apenas o admin pode excluir (digite senha do admin).", msg);
    }

    @Test
    // Teste de domínio
    public void excluir_FalhaExcluirOProprioAdmin() {
        String msg = controller.tentarExcluir("admin", "123");
        assertEquals("Usuário não encontrado ou é admin.", msg);
    }

    @Test
    // Teste de domínio
    public void excluir_FalhaUsuarioInexistente() {
        String msg = controller.tentarExcluir("fantasma", "123");
        assertEquals("Usuário não encontrado ou é admin.", msg);
    }

    @Test
    // Teste de fronteira
    public void fronteira_LoginComStringsMuitoLongas() {
        String longa = "a".repeat(10000);
        Usuario logado = controller.tentarLogin(longa, "123");
        assertNull(logado);
    }

    @Test
    // Teste estrutural (MC/DC)
    public void mcdc_ExcluirAdminNulo_NaoAutorizado() {
        // Para forçar admin == null, usamos senha errada, autenticar retorna null
        String msg = controller.tentarExcluir("jogador1", "senhaErrada");
        assertEquals("Apenas o admin pode excluir (digite senha do admin).", msg);
    }

    @Test
    // Teste estrutural (MC/DC)
    public void mcdc_ExcluirAdminValidoERemovidoTrue_RetornaSucesso() {
        controller.tentarCadastrar("jogador1", "senha1");
        String msg = controller.tentarExcluir("jogador1", "123"); // admin super, removido true
        assertEquals("Usuário removido!", msg);
    }

    @Test
    // Teste estrutural (MC/DC)
    public void mcdc_ExcluirAdminValidoERemovidoFalse_RetornaErro() {
        String msg = controller.tentarExcluir("inexistente", "123"); // removido false
        assertEquals("Usuário não encontrado ou é admin.", msg);
    }

    @Test
    // Dublê de teste
    public void duble_StageRegistraOnCloseRequest() {
        verify(stageMock).setOnCloseRequest(any());
    }
}