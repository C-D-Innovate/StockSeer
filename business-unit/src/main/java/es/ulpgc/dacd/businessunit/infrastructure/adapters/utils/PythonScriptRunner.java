package es.ulpgc.dacd.businessunit.infrastructure.adapters.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

public class PythonScriptRunner {
    private static final String PYTHON_EXECUTABLE =
            System.getenv().getOrDefault("PYTHON_EXECUTABLE", "python3");
    private static final int MAX_TEXT_LENGTH = 10000;

    public String runAnalysisScript(String text) {
        File tempScript = null;
        File tempTextFile = null;
        Process process = null;

        try {
            if (text == null || text.isBlank()) {
                System.out.println("Texto vacío. Se devuelve NEUTRAL.");
                return "NEUTRAL";
            }

            if (text.length() > MAX_TEXT_LENGTH) {
                System.out.println("Texto demasiado largo (" + text.length() + " caracteres). Truncando a " + MAX_TEXT_LENGTH + ".");
                text = text.substring(0, MAX_TEXT_LENGTH);
            }

            tempScript = File.createTempFile("label_news", ".py");
            tempScript.deleteOnExit();
            System.out.println("Copiando script Python a: " + tempScript.getAbsolutePath());
            try (InputStream in = PythonScriptRunner.class.getResourceAsStream("/label_news.py")) {
                if (in == null) throw new FileNotFoundException("No se encontró label_news.py en resources");
                Files.copy(in, tempScript.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            tempTextFile = File.createTempFile("news_text", ".txt");
            tempTextFile.deleteOnExit();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempTextFile))) {
                writer.write(text);
            }
            System.out.println("Archivo temporal de texto: " + tempTextFile.getAbsolutePath());

            ProcessBuilder pb = new ProcessBuilder(
                    PYTHON_EXECUTABLE,
                    tempScript.getAbsolutePath(),
                    "--file", tempTextFile.getAbsolutePath()
            );

            process = pb.start();

            StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());
            StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());

            outputGobbler.start();
            errorGobbler.start();

            boolean finished = process.waitFor(15, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                System.err.println("Script Python excedió el tiempo de espera.");
                throw new RuntimeException("Script Python excedió tiempo máximo de 15s.");
            }

            outputGobbler.join();
            errorGobbler.join();

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                System.err.println("Script Python finalizó con error. Código: " + exitCode);
                System.err.println("Salida de error del script:\n" + errorGobbler.getContent());
                throw new RuntimeException("Error en ejecución del script Python.");
            }

            String label = outputGobbler.getContent().lines().findFirst().orElse("NEUTRAL").trim().toUpperCase();

            if (!label.equals("POSITIVE") && !label.equals("NEGATIVE") && !label.equals("NEUTRAL")) {
                System.out.println("Resultado no reconocido: " + label + ". Se usará NEUTRAL.");
                label = "NEUTRAL";
            }

            System.out.println("Sentiment recibido: " + label);
            return label;

        } catch (IOException | InterruptedException e) {
            System.err.println("Excepción en runAnalysisScript: " + e.getMessage());
            throw new RuntimeException("Error ejecutando script de análisis", e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private static class StreamGobbler extends Thread {
        private final InputStream inputStream;
        private final StringBuilder content = new StringBuilder();

        public StreamGobbler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append(System.lineSeparator());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String getContent() {
            return content.toString();
        }
    }
}
