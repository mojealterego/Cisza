# CISZA Ω — Secure Messenger v0.1.0

Nieoficjalny fork **SimpleX Chat 7.0.2**, Android application ID `pl.omega.cisza`. Repozytorium jest publiczne i rozwijane wyłącznie na gałęzi `main`.

## Co już opublikowano na GitHub

- [Pakiet zmian CISZA Ω](source/CISZA_OMEGA_v0.1.0_PATCH.zip) — oryginalne archiwum poprawek (529 599 bajtów; SHA-256: `365b34b53cadc861e98cb0d579a4ab87c5224f8e377a02fd47730f4929d323bb`).
- [Skrypt odtwarzania pełnego źródła](scripts/restore_cisza.py) — ekstrakcja z kontrolą integralności SHA-256, odrzucaniem niebezpiecznych wpisów ZIP i bez nadpisywania katalogu.
- [Testy odtwarzania](tests/test_restore.py) — przypadki poprawnego odtworzenia, błędnej sumy SHA-256, traversal i ochrony danych.
- [Stan źródeł i testów](docs/SOURCE_STATUS.md).

**Ważne:** ten stan repozytorium *nie zawiera jeszcze całego* ponad 200-megabajtowego rozpakowanego drzewa kodu. Pakiet zmian nie jest samodzielnym projektem Android.

## Jak odtworzyć pełną kopię

Pobierz osobno `CISZA_OMEGA_v0.1.0_FULL_SOURCE.zip` z pakietu dostarczonego w konwersacji. Oczekiwana suma kontrolna:

```text
3b65013dc9c80f7b6df0df29a06ebb5f222bd7cf1d5bd07a60f06ce5a55d2a85
```

```bash
git clone https://github.com/mojealterego/Cisza.git
cd Cisza
python3 -m unittest discover -s tests -v
python3 scripts/restore_cisza.py \
  /sciezka/CISZA_OMEGA_v0.1.0_FULL_SOURCE.zip \
  ./CISZA-OMEGA-v0.1.0
cd CISZA-OMEGA-v0.1.0
bash scripts/test-cisza-guard.sh
python3 scripts/verify-cisza-source.py
```

Skrypt nie wymaga GitHub Actions. Testy lokalne: **16/16 testów polityki PIN**, **5/5 testów odtwarzania** i **2917/2917 kluczy PL** — PASS. Pełna kompilacja APK, natywne biblioteki Haskell, testy sprzętowe i podpisanie wydania **nie zostały potwierdzone**.

## Ostrzeżenia i licencja

Zabezpieczenie trzech błędnych prób PIN jest domyślnie wyłączone. Po świadomym włączeniu może spowodować nieodwracalną utratę danych prywatnych aplikacji. Nie testować na profilu produkcyjnym. Zmiana application ID oznacza osobną instalację, nie aktualizację oficjalnego SimpleX Chat.

**Licencja kodu bazowego: GNU AGPL-3.0.** Zachować dokument `LICENSE` i informacje o prawach autorskich w pełnym drzewie źródeł. Projekt CISZA Ω nie jest oficjalnym produktem SimpleX.
