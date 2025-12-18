#!/bin/bash
echo "Установка зависимостей..."
pip install -r requirements.txt

echo ""
echo "Запуск сервера на http://localhost:5001"
echo "Для остановки нажмите Ctrl+C"
echo ""

python main.py



