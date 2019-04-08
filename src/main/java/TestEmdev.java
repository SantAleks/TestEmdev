import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;

public class TestEmdev {
    // В ОС WINDOWS текст скорее всего будет написан в кодировке "Windows-1251"
    static private String stCharset = (System.getProperty("os.name").startsWith("Windows"))?"Windows-1251":Charset.defaultCharset().name();
    static private String userDir = System.getProperty("user.dir");

    public static void main(String[] args) throws Exception {

        // Читаем input.txt из локальной папки, считаем кол букв и выводим в консоль
        Path path = Paths.get(userDir,"input.txt");
        CountLetter countLetter = new CountLetter(stCharset);
        Map<String, Long> sortedMap  = countLetter.countLetter(path);
        sortedMap.forEach((k,v)->System.out.println(k + " : "+ v));

        // Организуем регулярный запуск задачи мониторинга файлов раз в 10 сек
        System.out.println("TimerTask is begin work");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                countLetter.doWatch(userDir);
            }
        }, 0, 10*1000);
    }
}
