package com.fundingsocieties.common;

import com.opencsv.CSVWriter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

@Slf4j
public final class FileHelper {

    private FileHelper() {
    }

    public static void createFolder(final String folderPath) {
        new File(folderPath).mkdirs();
    }

    @SneakyThrows
    public static void writeCsvFile(final String filePath, final String[] header, final List<String[]> lines) {
        final File file = new File(filePath);
        final FileWriter outputFile = new FileWriter(file);
        final CSVWriter writer = new CSVWriter(outputFile);

        writer.writeNext(header);
        for (final String[] line : lines) {
            writer.writeNext(line);
        }
        writer.close();
    }
}
