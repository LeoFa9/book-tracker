# API сервер для Трекера книг

FastAPI сервер для хранения данных о книгах с PostgreSQL базой данных.

## Архитектура

- **FastAPI** - веб-фреймворк
- **PostgreSQL** - база данных
- **SQLAlchemy** - ORM для работы с БД
- **Docker Compose** - оркестрация контейнеров

## Запуск через Docker Compose

### Шаг 1: Проверка Docker Desktop

**Важно**: Убедитесь, что Docker Desktop установлен и запущен!

1. Откройте Docker Desktop
2. Дождитесь полной загрузки (иконка в системном трее должна быть активна)
3. Проверьте, что Docker работает:
   ```bash
   docker --version
   docker ps
   ```

### Шаг 2: Запуск сервера и БД

```bash
docker compose up --build
```

При первом запуске:
- Docker соберет образ API (это займет несколько минут)
- Загрузит образ PostgreSQL
- Создаст базу данных и таблицы автоматически
- Сервер будет доступен на `http://localhost:5001`
- PostgreSQL будет доступен на `localhost:5432`

### Шаг 3: Проверка работы

Откройте в браузере: `http://localhost:5001/docs` - должна открыться документация FastAPI

### Остановка сервера

Нажмите `Ctrl+C` в терминале или:
```bash
docker compose down
```

Для полной очистки данных (удаление volumes):
```bash
docker compose down -v
```

### Просмотр логов

Все сервисы:
```bash
docker compose logs -f
```

Только API:
```bash
docker compose logs -f api
```

Только БД:
```bash
docker compose logs -f db
```

## Подключение к БД

Если нужно подключиться к PostgreSQL напрямую:

```bash
docker exec -it book-db psql -U bookuser -d bookdb
```

Параметры подключения:
- **Host**: localhost
- **Port**: 5432
- **Database**: bookdb
- **User**: bookuser
- **Password**: bookpass

## Запуск без Docker (локально)

### Windows:
```bash
run_local.bat
```

### Linux/Mac:
```bash
chmod +x run_local.sh
./run_local.sh
```

Или вручную:
```bash
pip install -r requirements.txt
python main.py
```

Сервер будет доступен на `http://localhost:5001`

## API Endpoints

- `GET /books` - получить список всех книг
- `POST /books` - создать новую книгу
- `GET /books/{id}` - получить книгу по ID
- `PUT /books/{id}` - обновить книгу
- `DELETE /books/{id}` - удалить книгу

## Для Android эмулятора

В Android приложении используется адрес `http://10.0.2.2:5001`, который автоматически перенаправляется на `localhost:5001` хоста.

## Остановка

```bash
docker compose down
```

