#!/bin/bash

# Zatrzymanie skryptu w przypadku błędu
set -e

echo "=== ROZPOCZYNAM AUTOMATYCZNĄ NAPRAWĘ I SYNCHRONIZACJĘ ==="

# 1. Sprawdzenie czy to repozytorium Git
if [ ! -d ".git" ]; then
    echo "Błąd: To nie jest katalog repozytorium Git!"
    exit 1
fi

# 2. Wykrycie aktywnej gałęzi (main/master)
BRANCH=$(git branch --show-current)
if [ -z "$BRANCH" ]; then
    BRANCH="main"
fi
echo "Wykryta gałąź: $BRANCH"

# 3. Dodanie absolutnie wszystkich plików (w tym nowe pliki kolorów i wrappera)
echo "Dodawanie wszystkich plików do Gita..."
git add -A

# 4. Sprawdzenie czy są zmiany do zatwierdzenia
if git diff-index --quiet HEAD --; then
    echo "Brak nowych zmian do zakommitowania (wszystko jest już aktualne lokalnie)."
else
    echo "Zatwierdzanie zmian (commit)..."
    git commit -m "Naprawa brakujących plików, kolorów i synchronizacja projektu"
fi

# 5. Bezpieczne pobranie zmian z serwera (rebase zapobiega konfliktom i błędom fast-forward)
echo "Pobieranie zmian z GitHub (git pull --rebase)..."
git pull origin "$BRANCH" --rebase

# 6. Wysłanie całości na GitHub
echo "Wysyłanie kodu na GitHub (git push)..."
git push origin "$BRANCH"

echo "=== GOTOWE! Wszystko zostało pomyślnie wysłane na GitHub. ==="
