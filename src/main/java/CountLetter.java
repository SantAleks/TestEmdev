import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CountLetter {
    String stCharset;
    CountLetter(String stCharset){
        this.stCharset = stCharset;
    }
    public void doWatch(String stDir){
        // Читаем все файлы из дирректории IN
        Path inPathDir = Paths.get(stDir,"IN");
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(inPathDir)) {
            for (Path child : ds) {
                if (!Files.isDirectory(child) && child.toString().toLowerCase().endsWith(".txt")) {
                    Path outPath = Paths.get(stDir,"OUT", child.getFileName().toString());
                    // Если файл не существует, или он директория, или устарел
                    if (Files.notExists(outPath) || (
                            !Files.isDirectory(outPath)
                                    && Files.getLastModifiedTime(outPath).compareTo(Files.getLastModifiedTime(child))<0)
                    ) {
                        System.out.println("Update file: " + child.getFileName());
                        Map<String, Long> sortedInMap  = countLetter(child);
                        List<String> listFile = new ArrayList<>();
                        sortedInMap.forEach((k,v)->listFile.add(k + " : "+ v));
                        Files.write(outPath, listFile, StandardCharsets.UTF_8);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Long> countLetter(Path path){
        Map<String, Long> bufMap = new HashMap<>();
        Map<String, Long> sortedMap = null;
        // Для чтения потока используем Scanner.
        try (Scanner scannerIn = new Scanner(Files.newInputStream(path), stCharset)){

            while (scannerIn.hasNextLine()) {
                // Вырежем все небуквенные символы
                String sRead[] = scannerIn.nextLine().replaceAll("[^\\pL]", "").split("");
                // Игнорируем пустые строки
                if (sRead.length == 1 && sRead[0].length() == 0) continue;

                // Сгруппируем и добавим результат в буферный словарь
                Stream.of(sRead).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                        .forEach((k, v) -> bufMap.merge(k, v, (a, b) -> a + b));
            }

            // Отсортируем сначала по количеству вхождений, потом по алфавиту
            sortedMap = new TreeMap<>(new Comparator<String>() {
                @Override
                public int compare(String x, String y) {
                    int result = bufMap.get(y).compareTo(bufMap.get(x));
                    return result == 0 ? x.compareTo(y) : result;
                }
            });
            sortedMap.putAll(bufMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sortedMap;
    }
}
