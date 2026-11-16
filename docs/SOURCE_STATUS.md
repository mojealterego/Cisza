# CISZA Ω v0.1.0 — źródła, integralność i bramki odbiorowe

Data kontroli lokalnej: 2026-11-16. Repozytorium docelowe: `mojealterego/Cisza`, gałąź `main`.

## Artefakty wejściowe

| Plik | Bajty | SHA-256 |
|---|---:|---|
| `CISZA_OMEGA_v0.1.0_FULL_SOURCE.zip` | 172097594 | `3b65013dc9c80f7b6df0df29a06ebb5f222bd7cf1d5bd07a60f06ce5a55d2a85` |
| `CISZA_OMEGA_v0.1.0_PATCH.zip` | 529599 | `365b34b53cadc861e98cb0d579a4ab87c5224f8e377a02fd47730f4929d323bb` |

Pełne archiwum zawiera 4786 pozycji ZIP, po rozpakowaniu 3854 pliki i jeden zachowany symlink; ma licencję `LICENSE` (GNU AGPLv3) oraz źródła modyfikacji SimpleX Chat 7.0.2. W bieżącym repozytorium przechowywany jest pakiet zmian `source/CISZA_OMEGA_v0.1.0_PATCH.zip`. Sam pakiet zmian **nie zastępuje pełnego drzewa źródłowego**. Aby odtworzyć pełne źródła, wymagane jest kompletne archiwum źródłowe z dokładnie zgodnym SHA-256.

## Odzyskanie pełnego projektu

```bash
python3 scripts/restore_cisza.py \
  /path/to/CISZA_OMEGA_v0.1.0_FULL_SOURCE.zip \
  ./CISZA-OMEGA-v0.1.0
cd CISZA-OMEGA-v0.1.0
bash scripts/test-cisza-guard.sh
python3 scripts/verify-cisza-source.py
```

Proces odtwarzania odrzuca niezgodną sumę kontrolną, niedozwolone nazwy wpisów ZIP, nieoczekiwane linki symboliczne i istniejący katalog docelowy. Wykonuje ekstrakcję do tymczasowego katalogu i publikuje go dopiero po pomyślnym zakończeniu.

## Testy faktycznie wykonane

- `bash scripts/test-cisza-guard.sh` — 16/16 testów polityki PIN zakończonych PASS.
- `python3 scripts/verify-cisza-source.py` — 2917/2917 par kluczy zasobów językowych; pozostałe kontrole statyczne PASS.
- `python3 -m unittest discover -s tests -v` na skrypcie odtwarzającym repozytorium — 5/5 testów PASS.
- Próba odtworzenia z całego archiwum — PASS (3854 pliki), a kontrola źródła na odtworzonym drzewie — PASS.

## Niepotwierdzone

- Kompilacja Android APK/AAB i podpisanie — **NIE WYKONANO**.
- Testy end-to-end na fizycznych urządzeniach — **NIE WYKONANO**.
- Natywna kompilacja bibliotek Haskell dla ABI Android — **NIE WYKONANO**.
- Audyt kryptograficzny niezależnej jednostki — **NIE WYKONANO**.

System bazowy nadal wymaga zależności zgodnych z oryginalnym SimpleX Chat, Gradle, Android SDK/NDK oraz bibliotek `libsimplex.so` i `libsupport.so` dla docelowego ABI. Patrz `docs/CISZA_BUILD.md` w pełnym archiwum.

## Ostrzeżenia bezpieczeństwa

Opcja usuwania danych aplikacji po trzech kolejnych błędnych PIN jest domyślnie wyłączona. Może powodować bezpowrotną utratę danych aplikacji. Przed testowaniem wykonaj eksport i kopię zapasową, używaj wyłącznie profili testowych. Inny Android application ID `pl.omega.cisza` oznacza osobną aplikację, a nie automatyczną migrację z oficjalnego SimpleX.

## Licencja

CISZA Ω to niezależna modyfikacja kodu na licencji GNU AGPL-3.0. Przy dystrybucji zachować LICENSE, informacje o prawach autorskich i obowiązki licencyjne SimpleX Chat.
