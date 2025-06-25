package es.ulpgc.dacd.newsapi.infrastructure.adapters.provider.enricher;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class PythonEnricherRunner {

    private static final Logger LOGGER = Logger.getLogger(PythonEnricherRunner.class.getName());
    private static final String PYTHON_EXECUTABLE =
            System.getenv().getOrDefault("PYTHON_EXECUTABLE", "python3");

    public static String extractFullContent(String articleUrl) throws IOException, InterruptedException {
        LOGGER.info("\nEjecutando script Python para extraer contenido completo...\n");

        URL resource = PythonEnricherRunner.class.getResource("/extract_full_content.py");
        if (resource == null) {
            throw new FileNotFoundException("No se encontró extract_full_content.py en recursos");
        }

        File tempScript = File.createTempFile("extract_full_content", ".py");
        try (InputStream in = resource.openStream()) {
            Files.copy(in, tempScript.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        tempScript.setExecutable(true);

        ProcessBuilder pb = new ProcessBuilder(PYTHON_EXECUTABLE, tempScript.getAbsolutePath(), articleUrl);
        pb.redirectErrorStream(false);

        Process process = pb.start();

        StringBuilder stdout = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stdout.append(line).append("\n");
            }
        }

        StringBuilder stderr = new StringBuilder();
        try (BufferedReader readerErr = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String lineErr;
            while ((lineErr = readerErr.readLine()) != null) {
                stderr.append(lineErr).append("\n");
            }
        }

        boolean finished = process.waitFor(15, TimeUnit.SECONDS);
        int exitCode = process.exitValue();

        if (!finished) {
            throw new RuntimeException("El script Python NO finalizó en 15 segundos. Se pasa al siguiente artículo.\n");
        }
        if (exitCode != 0) {
            LOGGER.warning("\nNo se ha podido acceder al contenido entero de la noticia por cookies, se utilizará el contenido breve.\n");
            tempScript.delete();
            return "";
        }

        LOGGER.info("\nScript Python ejecutado correctamente. FullContent abastecido.\n");
        tempScript.delete();
        return stdout.toString().trim();
    }
}
