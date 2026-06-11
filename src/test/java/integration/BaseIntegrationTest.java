package integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Superclasse abstrata responsável por gerenciar o ciclo de vida do arquivo TXT
 * e isolar a infraestrutura de I/O comum aos testes de integração.
 */
public abstract class BaseIntegrationTest {

    // Modificador 'protected' permite que as subclasses usem a constante se necessário
    protected final String ARQUIVO_TESTE = "usuarios.txt";

    @BeforeEach
    public void setupBase() throws Exception {
        limparAmbienteDePersistencia();
    }

    @AfterEach
    public void tearDownBase() throws Exception {
        limparAmbienteDePersistencia();
    }

    /**
     * Método utilitário compartilhado para garantir o isolamento dos testes,
     * deletando o arquivo físico e tratando resíduos do sistema de arquivos.
     */
    protected void limparAmbienteDePersistencia() throws Exception {
        // Remove o arquivo usuarios.txt se ele existir
        Files.deleteIfExists(Path.of(ARQUIVO_TESTE));

        // Garantia extra de robustez para deletar diretórios fantasmas
        // caso algum teste simule falhas severas de I/O
        File file = new File(ARQUIVO_TESTE);
        if (file.isDirectory()) {
            file.delete();
        }
    }
}