# Pełna migracja CISZA Ω v0.1.0

Stan 2026-11-17. Gałąź main. Opublikowano wszystkie 69 plików pakietu overlay, w tym 68 śledzonych pozycji składających się na archiwum pełnego projektu i instrukcję APPLY_OVERLAY.txt. Porównanie ścieżek i identyfikatorów Git blob z oryginalnym ZIP: zgodne.

## Import pełnego drzewa

Na komputerze lub w Termux, z uwierzytelnionym Git i skonfigurowanym autorem commitów:

```bash
git clone --single-branch --branch main https://github.com/mojealterego/Cisza.git
cd Cisza
bash scripts/import-full-source.sh --upstream --push
```

Alternatywa bez pobierania upstream: `bash scripts/import-full-source.sh /path/to/CISZA_OMEGA_FULL_SOURCE.bundle --push`. Bez `--push` operacja jest lokalna. Import umieszcza pełne źródła pod `project/`, zachowuje istniejące pliki migracji i nie tworzy dodatkowej gałęzi. Zmiany są publikowane zwykłym fast-forward, bez force push. Skrypt nie uruchamia GitHub Actions.

## Identyfikowalność

- Pełne archiwum ZIP: SHA-256 3b65013dc9c80f7b6df0df29a06ebb5f222bd7cf1d5bd07a60f06ce5a55d2a85.
- Odtworzony Git bundle: SHA-256 3ebeeac93a3e4d277273599dcbc67f515cfd92c59b486ca84f45855d96849bad.
- Commit lokalnego bundle: c971161bc60ce7bf484da59e33c5c88f7554ff22.
- Przypięty upstream SimpleX Chat 7.0.2: 4df04bdb3ff94059734ad2da7d2766de6dba2cc7.
- Oczekiwany hash kompletnego drzewa Git: 3fcc4366b76dd377d91228b97e21a187570b276e (3854 pozycje, w tym jeden symlink).

Pełne drzewo składa się z upstream bez wpisu gitlink apps/multiplatform/external/nanohttpd/upstream (jak w oryginalnym archiwum) oraz 68 plików overlay. Hash drzewa obliczono niezależnie z metadanych z GitHub i porównano z lokalnym drzewem utworzonym z ZIP. Zgodność hashy potwierdzona.

## Dowody

- SHA-256 i CRC obu oryginalnych ZIP: PASS.
- Porównanie 3854 trybów i blob SHA z ZIP: PASS, zero różnic.
- git fsck i git bundle verify: PASS.
- Test lokalnego importu bundle, zachowania historii, idempotencji, odrzucania uszkodzonego bundle i brudnego drzewa: PASS.
- Test składania drzewa na lokalnym fixture z gitlink, brakującym plikiem i rzeczywistym overlay: PASS.
- Rzeczywisty test PIN: 16/16 PASS; 2917/2917 kluczy PL; sprawdzanie Kotlin na testowych atrapach Android API: PASS.
- Sieciowe pobranie upstream i końcowy push całego drzewa z obecnego środowiska: BLOCKED (brak bezpośredniego transportu Git).
- Kompilacja APK, testy urządzeń i niezależny audyt bezpieczeństwa: BLOCKED / niewykonane.

Import całego drzewa na GitHub należy potwierdzić przez `git rev-parse HEAD:project` po końcowym push. Sama obecność skryptów oraz lokalnego bundle nie stanowi dowodu pełnego zdalnego importu.
