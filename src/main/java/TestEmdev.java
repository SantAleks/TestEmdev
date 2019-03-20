import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestEmdev {
    // В ОС WINDOWS текст скорее всего будет написан в кодировке "Windows-1251"
    static private String strCharset = (System.getProperty("os.name").startsWith("Windows"))?"Windows-1251":Charset.defaultCharset().name();
    static final String userDir = System.getProperty("user.dir");

    public static void main(String[] args) throws Exception {

        // Читаем input.txt из локальной папки, считаем кол букв и выводим в консоль
        Path path = Paths.get(userDir,"input.txt");
        Map sortedMap  = countLiter(path);
        sortedMap.forEach((k,v)->System.out.println(k + " : "+ v));

        // Организуем регулярный запуск задачи мониторинга файлов раз в 10 сек
        System.out.println("TimerTask is begin work");
        final TestEmdev testEmdev = new TestEmdev();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                testEmdev.doWatch();
            }
        }, 0, 10*1000);
    }

    public void doWatch(){

        // Читаем все файлы из дирректории IN
        Path inPathDir = Paths.get(userDir,"IN");
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(inPathDir)) {
            for (Path child : ds) {
                if (!Files.isDirectory(child) && child.toString().toLowerCase().endsWith(".txt")) {
                    Path outPath = Paths.get(userDir,"OUT", child.getFileName().toString());
                    // Если файл не существует, или он директория, или устарел
                    if (Files.notExists(outPath) || (
                            !Files.isDirectory(outPath)
                                    && Files.getLastModifiedTime(outPath).compareTo(Files.getLastModifiedTime(child))<0)
                    ) {
                        System.out.println("Update file: " + child.getFileName());
                        Map sortedInMap  = countLiter(child);
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


    private static Map countLiter(Path path){
        final Map<String, Long> bufMap = new HashMap<>();
        // Для чтения потока используем Scanner.
        try (Scanner scannerIn = new Scanner(Files.newInputStream(path), strCharset)){

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
            final Map<String, Long> sortedMap = new TreeMap<>(new Comparator<String>() {
                @Override
                public int compare(String x, String y) {
                    int result = bufMap.get(y).compareTo(bufMap.get(x));
                    return result == 0 ? x.compareTo(y) : result;
                }
            });
            sortedMap.putAll(bufMap);
            return sortedMap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
