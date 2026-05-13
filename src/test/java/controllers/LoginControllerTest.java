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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LoginControllerTest {

    private LoginController controller;
    private Stage stageMock;
    private final String ARQUIVO_TESTE = "usuarios.txt"; // O mesmo usado na classe original

    // ==========================================
    // INICIALIZAÇÃO DO JAVAFX PARA TESTES
    // ==========================================
    @BeforeAll
    public static void initJFX() {
        // Inicializa o motor invisível do JavaFX para ele parar de dar erro no Mock do Stage
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Se o Toolkit já estiver inicializado, ele lança essa exceção e nós apenas ignoramos
        }
    }

    @BeforeEach
    public void setup() {
        // Limpa qualquer arquivo TXT residual antes de começar, garantindo um teste isolado
        new File(ARQUIVO_TESTE).delete();

        // Criando um Dublê (Mock) da Janela do JavaFX
        // O Mockito fará com que o stage.setOnCloseRequest() não dê erro, mesmo sem interface gráfica
        stageMock = Mockito.mock(Stage.class);

        // Instancia o controller. O construtor já vai tentar carregar o TXT vazio e criar o Admin.
        controller = new LoginController(stageMock);
    }

    @AfterEach
    public void tearDown() {
        // Apaga o arquivo TXT criado pelos testes para não sujar o seu projeto real
        new File(ARQUIVO_TESTE).delete();
    }

    // ==========================================
    // MÉTODOS AUXILIARES (REFLECTION)
    // Como os métodos e a lista são PRIVATE, usamos Reflexão para acessá-los no teste.
    // ==========================================

    private Usuario invocarAutenticar(String login, String senha) throws Exception {
        Method method = LoginController.class.getDeclaredMethod("autenticar", String.class, String.class);
        method.setAccessible(true); // Quebra o bloqueio do 'private'
        return (Usuario) method.invoke(controller, login, senha);
    }

    @SuppressWarnings("unchecked")
    private List<Usuario> getBancoUsuarios() throws Exception {
        Field field = LoginController.class.getDeclaredField("bancoUsuarios");
        field.setAccessible(true); // Quebra o bloqueio do 'private'
        return (List<Usuario>) field.get(controller);
    }

    private void invocarSalvarDados() throws Exception {
        Method method = LoginController.class.getDeclaredMethod("salvarDadosNoArquivo");
        method.setAccessible(true);
        method.invoke(controller);
    }

    // ==========================================
    // 1. TESTES DE ESTADO E DOMÍNIO
    // ==========================================

    /**
     * TIPO: Teste de Estado Inicial (Domínio)
     * O QUE FAZ: Verifica a regra de negócio do construtor.
     * Garante que, se o banco de usuários (TXT) estiver completamente vazio,
     * o sistema deve obrigatoriamente criar o usuário "admin" com a senha "123".
     */
    @Test
    public void dominio_GaranteAdminSempreExisteSeBancoVazio() throws Exception {
        List<Usuario> banco = getBancoUsuarios();

        assertEquals(1, banco.size(), "Deveria ter exatamente 1 usuário no banco.");
        assertEquals("admin", banco.get(0).getLogin());
        assertEquals("123", banco.get(0).getSenha());
        assertTrue(banco.get(0).isSuperUsuario());
    }

    // ==========================================
    // 2. TESTES ESTRUTURAIS (MC/DC)
    // ==========================================

    /**
     * TIPO: Teste Estrutural (Cobertura MC/DC)
     * O QUE FAZ: Testa a condição de SUCESSO do método autenticar.
     * Condição: (Login confere = TRUE) E (Senha confere = TRUE)
     * Garante que retorna o objeto do usuário correspondente.
     */
    @Test
    public void mcdc_Autenticar_Sucesso() throws Exception {
        Usuario autenticado = invocarAutenticar("admin", "123");

        assertNotNull(autenticado);
        assertEquals("admin", autenticado.getLogin());
    }

    /**
     * TIPO: Teste Estrutural (Cobertura MC/DC)
     * O QUE FAZ: Testa a ramificação de FALHA por SENHA ERRADA.
     * Condição: (Login confere = TRUE) E (Senha confere = FALSE)
     * Garante que o método retorna null, impedindo o acesso.
     */
    @Test
    public void mcdc_Autenticar_FalhaSenhaIncorreta() throws Exception {
        Usuario autenticado = invocarAutenticar("admin", "senhaErrada");

        assertNull(autenticado, "Não deveria autenticar com a senha incorreta.");
    }

    /**
     * TIPO: Teste Estrutural (Cobertura MC/DC)
     * O QUE FAZ: Testa a ramificação de FALHA por LOGIN INEXISTENTE.
     * Condição: (Login confere = FALSE)
     * Como o Java avalia da esquerda para a direita (curto-circuito), a senha nem importa.
     * Garante que o método retorna null.
     */
    @Test
    public void mcdc_Autenticar_FalhaUsuarioNaoExiste() throws Exception {
        Usuario autenticado = invocarAutenticar("fantasma", "123");

        assertNull(autenticado, "Não deveria autenticar um usuário que não está na lista.");
    }

    // ==========================================
    // 3. TESTES DE INTEGRAÇÃO (I/O)
    // ==========================================

    /**
     * TIPO: Teste de Integração (Entrada e Saída / File System)
     * O QUE FAZ: Testa a persistência de dados.
     * Adiciona um usuário novo na memória, força a gravação no arquivo TXT,
     * e depois simula a abertura do jogo novamente criando um NOVO controller.
     * Garante que os dados do TXT são lidos corretamente e o usuário novo está lá.
     */
    @Test
    public void integracao_SalvarECarregarArquivoDeUsuarios() throws Exception {
        // 1. Injeta um usuário novo na memória do controller atual
        List<Usuario> bancoOriginal = getBancoUsuarios();
        bancoOriginal.add(new Usuario("jogador1", "senha1", 50, 2, false));

        // 2. Força a salvar no usuarios.txt
        invocarSalvarDados();

        // 3. Cria um NOVO controller simulando o programa sendo reaberto
        LoginController novoController = new LoginController(stageMock);

        // 4. Verifica se o novo controller leu o arquivo corretamente usando reflection
        Field field = LoginController.class.getDeclaredField("bancoUsuarios");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Usuario> bancoCarregado = (List<Usuario>) field.get(novoController);

        // Deverá ter 2 usuários agora: o admin (padrão) e o jogador1
        assertEquals(2, bancoCarregado.size());

        // Verifica se os dados do jogador1 sobreviveram ao salvamento em texto
        Usuario jogador1 = bancoCarregado.get(1);
        assertEquals("jogador1", jogador1.getLogin());
        assertEquals(50, jogador1.getPontuacaoTotal());
        assertEquals(2, jogador1.getSessoesExecutadas());
        assertFalse(jogador1.isSuperUsuario());
    }
}