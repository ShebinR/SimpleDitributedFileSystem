package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FileOperations {

    public static List<String> readFileAsString(String path){
        List<String> fileLines = null;
        try {
            fileLines = Files.readAllLines(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileLines;
    }

    public static byte[] readFileAsBytes(String path) {
        byte[] contents = null;
        try {
            contents = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }

}
