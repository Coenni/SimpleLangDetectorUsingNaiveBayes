package com.coenni;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {

    public static List<String> getContentFromFile(String fileName) throws URISyntaxException, IOException {
        Path path = Paths.get(FileUtils.class.getClassLoader().getResource(fileName).toURI());
        Stream<String> lines = Files.lines(path);
        return lines.collect(Collectors.toList());
    }
}
