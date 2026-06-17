package integration;

import java.nio.file.Path;
import org.junit.jupiter.api.io.TempDir;

/**
 * Infraestrutura comum para jornadas executadas em ambiente limpo.
 */
public abstract class BaseIntegrationTest {

    @TempDir
    Path diretorioTemporario;

    protected Path arquivoUsuarios() {
        return diretorioTemporario.resolve("usuarios.txt");
    }
}
