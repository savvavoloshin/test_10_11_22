## Тест.
Описание задачи в файле description.txt.
Замечания к недостаткам в файле notes.txt.

Запуск программы.
1) компиляция: javac -cp .:lib/* SimpleHttpServer.java
2) запуск сервера: java -cp .:lib/* SimpleHttpServer
3) примеры запросов к серверу в файле curl_test.sh

Протестированы основные логические функции. Без обращений к базе.
4) компиляция тестов: javac -cp .:lib/* TestSimpleHttpServer.java
5) запуск тестов: java -cp .:lib/* org.junit.runner.JUnitCore TestSimpleHttpServer

Замечание.
Наличие зарегистрированных пользователей в базе предполагается. Для иллюстрации можно смотреть прилагаемую sqlite базу или использовать следующих пользователей:
```
[
    {
        "name": "savva.voloshin",
        "password_hash": "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92",
        "user_id": 1
    },
    {
        "name": "savva.voloshin.2",
        "password_hash": "8bb0cf6eb9b17d0f7d22b456f121257dc1254e1f01665370476383ea776df414",
        "user_id": 2
    }
]
```