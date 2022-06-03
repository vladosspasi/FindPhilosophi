import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

public class Main {

    private static final Logger log = Logger.getLogger(Main.class.getName());

    private static ArrayList<String> linksLog = new ArrayList<>();

    public static void main(String[] args) {

        System.out.println("ВСЕ ПУТИ ВЕДУТ К ФИЛОСОФИИ!");
        System.out.println("Поиск числа первых ссылок в статьях Википедии, по которым нужно перейти, " +
                "чтобы с выбранной статьи попасть к статье \"Философия\".");
        System.out.println("-----------------------------------------------------------");
        System.out.println("Введите ссылку на вашу статью:");

        Scanner scanner = new Scanner(System.in);
        String originLink = scanner.nextLine();

        ArrayList<String> pageData = getArticleNameAndNextLink(originLink);
        System.out.println("Начальная статья: \"" + pageData.get(0) + "\".");
        linksLog.add(pageData.get(1));

        int i = 0;

        while (!Objects.equals(pageData.get(0), "Философия")) {

            pageData = getArticleNameAndNextLink(pageData.get(1));
            if (pageData.size() == 0) {
                break;
            }

            i++;
            System.out.println("Переход №" + i + ". Статья \"" + pageData.get(0) + "\".");
            if (linksLog.contains(pageData.get(1))) {
                System.out.println("Эта страница уже встречалась! Произошло зацикливание. Попробуйте другую страницу.");
                break;
            }
            linksLog.add(pageData.get(1));
        }

        if (i != 0) {
            System.out.println("Количество переходов:" + i);
        }

    }

    public static ArrayList<String> getArticleNameAndNextLink(String link) {
        ArrayList<String> result = new ArrayList<>();

        String articleName = "";
        String nextArticleLink = "";
        String maybeStr = "";

        boolean nameFound = false;
        boolean linkFound = false;

        //Попытка чтения документа страницы
        try {
            URL url = new URL(link);

            try {
                LineNumberReader reader = new LineNumberReader(new InputStreamReader(url.openStream()));
                String htmlString = reader.readLine();

                //Пока документ не закончился и пока не найдено имя
                while (htmlString != null && !nameFound) {
                    log.info("Текущая строка: " + htmlString);
                    //Название содержится в первой строке со словом <title>
                    if (htmlString.toLowerCase(Locale.ROOT).contains("<title>")) {
                        articleName = htmlString.substring(7, htmlString.length() - 1).split("—")[0];
                        articleName = articleName.substring(0, articleName.length() - 1); //Обрезка лишнего
                        nameFound = true;
                    }
                    htmlString = reader.readLine();
                }

                //Ищем начало текста - после </table>
                while (htmlString != null) {
                    log.info("Текущая строка: " + htmlString);
                    //Поиск конца секции table
                    if ((htmlString.contains("<p><b>")||htmlString.contains("<p><i><b>"))&&!htmlString.contains("<table")) {
                            break;
                    }else{
                        htmlString = reader.readLine();
                    }
                }
                log.info("Первая строка текста: " + htmlString);



                //Просмотр последующих строк
                while (htmlString!=null){

                    String reg = "(.*</b>[(</i>]* \\(.*)"; //Для случая, если строка начинается с названия и скобок
                    log.info("Имеет скобки: " + htmlString.matches(reg));

                    if (htmlString.matches(reg)) {
                        //Есть скобки после названия
                        //Отделяем по скобкам, и затем объединяем все кроме первой части (Название и скобки)
                        String[] htmlArrayByBrackets;

                        try{
                            htmlArrayByBrackets = htmlString.split("\\)");
                            log.info("Длина деления: " + htmlArrayByBrackets.length);

                            for (int i = 1; i < htmlArrayByBrackets.length; i++) {
                                maybeStr = maybeStr + htmlArrayByBrackets[i];
                            }
                        }catch (Exception e){
                            continue;
                        }

                    } else {
                        maybeStr = htmlString;
                    }
                    log.info("Очищенная строка: " + maybeStr);
                    //Ищем ссылки
                    String[] linksArray;

                    try {
                        linksArray = maybeStr.split("<a");

                        log.info("Длина деления на ссылки: " + linksArray.length);

                        for (int i = 1; i < linksArray.length; i++) {
                            log.info("Текущая ссылка: " + linksArray[i]);
                            //Ссылки квадраты - не нужны
                            if (linksArray[i].contains("[")) continue;
                            nextArticleLink = "https://ru.wikipedia.org/" + linksArray[i].split("\"")[1];
                            linkFound = true;
                            break;
                        }
                    }catch (Exception exception){
                        continue;
                    }

                    if(linkFound) break;
                    htmlString = reader.readLine();
                }

                reader.close();
            } catch (IOException e) {
                System.out.println("Произошла ошибка!");
                e.printStackTrace();
            }
        } catch (
                MalformedURLException ex) {
            ex.printStackTrace();
        }

        if (!nameFound) {
            System.out.println("У страницы нет названия, видимо она пуста.");
            return new ArrayList<>();
        }

        if (!linkFound) {
            System.out.println("У страницы нет ссылок, видимо она тупиковая. Введите другую страницу");
            return new ArrayList<>();
        }

        result.add(articleName);
        result.add(nextArticleLink);

        return result;
    }

}
