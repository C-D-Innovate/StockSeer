package es.ulpgc.dacd.newsapi.infrastructure.adapters.provider;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PythonScriptRunner {

    private static final Logger LOGGER = Logger.getLogger(PythonScriptRunner.class.getName());
    private static final String PYTHON_EXECUTABLE =
            System.getenv().getOrDefault("PYTHON_EXECUTABLE", "python3");

    public static String extractFullContent(String articleUrl) throws IOException, InterruptedException {
        LOGGER.info("[DEBUG] PYTHON_EXECUTABLE constant = " + PYTHON_EXECUTABLE);
        LOGGER.info("[DEBUG] PYTHON_EXECUTABLE env var = " + System.getenv("PYTHON_EXECUTABLE"));

        URL resource = PythonScriptRunner.class.getResource("/extract_full_content.py");
        LOGGER.info("[DEBUG] Resource URL = " + resource);
        if (resource == null) {
            throw new FileNotFoundException("No se encontró extract_full_content.py en resources");
        }

        File tempScript = File.createTempFile("extract_full_content", ".py");
        try (InputStream in = resource.openStream()) {
            Files.copy(in, tempScript.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        tempScript.setExecutable(true);
        LOGGER.info("[DEBUG] Temp script path = " + tempScript.getAbsolutePath());
        LOGGER.info("[DEBUG] Exists = " + tempScript.exists() + ", Executable = " + tempScript.canExecute());

        String cmd = PYTHON_EXECUTABLE + " " + tempScript.getAbsolutePath() + " " + articleUrl;
        LOGGER.info("[DEBUG] About to run: " + cmd);

        ProcessBuilder pb = new ProcessBuilder(PYTHON_EXECUTABLE, tempScript.getAbsolutePath(), articleUrl);
        pb.redirectErrorStream(false);

        Process process = pb.start();
        LOGGER.info("[DEBUG] Process started: " + process);

        StringBuilder stdout = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                LOGGER.info("[PY-OUT] " + line);
                stdout.append(line).append("\n");
            }
        }

        StringBuilder stderr = new StringBuilder();
        try (BufferedReader readerErr = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String lineErr;
            while ((lineErr = readerErr.readLine()) != null) {
                LOGGER.warning("[PY-ERR] " + lineErr);
                stderr.append(lineErr).append("\n");
            }
        }

        boolean finished = process.waitFor(15, TimeUnit.SECONDS);
        int exitCode = process.exitValue();
        LOGGER.info("[DEBUG] Process finished = " + finished + ", exitCode = " + exitCode);

        if (!finished) {
            throw new RuntimeException("El script Python NO finalizó en 60 segundos");
        }
        if (exitCode != 0) {
            throw new RuntimeException("Error al ejecutar el script Python. Código: " + exitCode);
        }

        tempScript.delete();
        return stdout.toString().trim();
    }
}
