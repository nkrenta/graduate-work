# Дипломный проект -- Сервис объявлений

Spring Boot REST API для сервиса объявлений с авторизацией, комментариями и загрузкой изображений.

## Технологии

- **Backend:** Spring Boot 2.7, Spring Security, Spring Data JPA, Liquibase
- **База данных:** PostgreSQL 18
- **Маппинг:** MapStruct
- **Документация:** Swagger (springdoc-openapi)
- **Frontend:** React (Docker)

## Быстрый старт

### Предварительные требования

- Java 21+
- PostgreSQL 14+
- Maven 3.8+
- Docker (для фронтенда)

### Запуск

1. Создайте базу данных:
```sql
CREATE DATABASE backend_avito;
CREATE USER admin WITH PASSWORD 'admin_password';
GRANT ALL PRIVILEGES ON DATABASE backend_avito TO admin;
```

2. Запустите фронтенд (Docker):
```bash
docker run -p 3000:3000 --rm ghcr.io/dmitry-bizin/front-react-avito:v1.21
```

3. Запустите бэкенд:
```bash
./mvnw spring-boot:run
```

4. Приложение доступно:
- Backend: http://localhost:8080
- Frontend: http://localhost:3000

## Структура проекта

```
src/main/java/ru/skypro/homework/
├── HomeworkApplication.java
├── config/
│   └── WebSecurityConfig.java
├── controller/
│   ├── AuthController.java
│   ├── UserController.java
│   ├── AdsController.java
│   ├── CommentController.java
│   ├── ImageController.java
│   └── RestExceptionHandler.java
├── dto/
├── entity/
├── filter/
│   └── BasicAuthCorsFilter.java
├── mapper/
├── repository/
└── service/
    ├── AuthService.java, UserService.java, AdsService.java, CommentService.java
    └── impl/
```

## База данных

### Схема

- `users` -- пользователи (id, email, password, first_name, last_name, phone, role, image)
- `ads` -- объявления (id, title, price, description, image, author_id FK)
- `comments` -- комментарии (id, text, created_at, ad_id FK, author_id FK)

### Liquibase

Миграции разбиты на отдельные SQL-файлы:
```
db/changelog/
├── 001-create-users-table.sql
├── 002-create-ads-table.sql
├── 003-create-comments-table.sql
└── 004-add-foreign-keys.sql
```

## Безопасность

- **Аутентификация:** Basic Auth (Base64(email:password))
- **Авторизация:** `@PreAuthorize` с проверкой владельца через БД
- **Пароли:** BCrypt шифрование
- **Файлы:** Валидация типа (PNG/JPEG/GIF) и размера (max 5MB)
- **Обработка ошибок:** Глобальный `@ControllerAdvice` с единым JSON-форматом

## Тесты

Проект содержит **44 интеграционных теста** с реальной PostgreSQL.

### Запуск

```bash
.\mvnw.cmd test
```

### Структура тестов

| Класс | Тестов | Что проверяет |
|-------|--------|---------------|
| `AuthControllerIntegrationTest` | 5 | Регистрация, вход, дубликат, неверный пароль |
| `AdsControllerIntegrationTest` | 12 | CRUD объявлений, @PreAuthorize (403), валидация файлов |
| `CommentControllerIntegrationTest` | 8 | CRUD комментариев, @PreAuthorize (403, 204) |
| `UserControllerIntegrationTest` | 11 | Профиль, смена пароля, аватар, валидация |
| `ErrorHandlerIntegrationTest` | 7 | 404, 403, 400, 401 с JSON-телом ошибки |
| `HomeworkApplicationTests` | 1 | Запуск контекста приложения |

### Подход

- **Тип:** Интеграционные (`@SpringBootTest` + `@AutoConfigureMockMvc`)
- **БД:** Реальная PostgreSQL (профиль `test`)
- **Изоляция:** `@Transactional` — данные откатываются после каждого теста
- **Покрытие:** Авторизация, CRUD, безопасность, валидация, обработка ошибок

### Конфигурация тестов

`src/test/resources/application.properties` — подключение к PostgreSQL с теми же настройками, что и в продакшене.

## Документация

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI spec: `openapi.yaml`
