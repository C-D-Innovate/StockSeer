package consumer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class EventFileWriter {

    private final String baseDir = "eventstore";
    public void writeEvent(String topic, String sourceSystem, String dateString, String messageJson) {
        try {
            File dir = new File(baseDir + "/" + topic + "/" + sourceSystem);
            if (!dir.exists()) {
                dir.mkdirs();
            }


            File file = new File(dir, dateString + ".events");


            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                writer.write(messageJson);
                writer.newLine();
            }

        } catch (IOException e) {
            System.err.println("Error writing event: " + e.getMessage());
        }
    }
}
