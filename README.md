## Тест.
Описание задачи в файле description.txt.
Замечания к недостаткам в файле notes.txt.

Запуск программы.
1) компиляция: javac -cp .:lib/* SimpleHttpServer.java
2) запуск сервера: java -cp .:lib/* SimpleHttpServer
3) примеры запросов к серверу в файле curl_test.sh

Протестированы основные логические функции. В том числе и обращающиеся к базе.
4) компиляция тестов: javac -cp .:lib/* TestSimpleHttpServer.java
5) запуск тестов: java -cp .:lib/* org.junit.runner.JUnitCore TestSimpleHttpServer

Замечание.
Наличие зарегистрированных пользователей в базе предполагается. Для иллюстрации можно смотреть прилагаемую sqlite базу или в тестах create_sample_data.sql