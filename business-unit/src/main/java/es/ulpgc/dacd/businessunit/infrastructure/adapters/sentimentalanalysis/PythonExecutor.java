package es.ulpgc.dacd.businessunit.infrastructure.adapters.sentimentalanalysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

public class PythonExecutor {
    private static final Logger logger = LoggerFactory.getLogger(PythonExecutor.class);
    private final String pythonExecutable;

    public PythonExecutor() {
        String exe = System.getenv("PYTHON_EXECUTABLE");
        this.pythonExecutable = (exe != null && !exe.isBlank()) ? exe : "python3";
        logger.info("Usando intérprete Python: {}", this.pythonExecutable);
    }

    public String executeScriptWithText(String scriptResourcePath, String text) {
        try {
            File tempScript = createTempScript(scriptResourcePath);
            File tempText = writeTextToTempFile(text);

            ProcessBuilder pb = new ProcessBuilder(
                    pythonExecutable,
                    tempScript.getAbsolutePath(),
                    "--file", tempText.getAbsolutePath()
            );

            Process process = pb.start();
            ProcessOutputCollector output = new ProcessOutputCollector(process.getInputStream());
            ProcessOutputCollector error = new ProcessOutputCollector(process.getErrorStream());

            output.start();
            error.start();

            boolean finished = process.waitFor(15, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("Script Python excedió tiempo máximo de espera.");
            }

            output.join();
            error.join();

            if (process.exitValue() != 0) {
                logger.error("Error en script Python: {}", error.getContent());
                throw new RuntimeException("Script Python falló con código: " + process.exitValue());
            }

            return output.getContent();

        } catch (Exception e) {
            logger.error("Error ejecutando script: {}", e.getMessage(), e);
            throw new RuntimeException("Fallo al ejecutar script", e);
        }
    }

    private File createTempScript(String resourcePath) throws IOException {
        File script = File.createTempFile("label_script", ".py");
        script.deleteOnExit();
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) throw new FileNotFoundException("No se encontró el script en: " + resourcePath);
            Files.copy(in, script.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return script;
    }

    private File writeTextToTempFile(String text) throws IOException {
        File file = File.createTempFile("input_text", ".txt");
        file.deleteOnExit();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(text);
        }
        return file;
    }
}