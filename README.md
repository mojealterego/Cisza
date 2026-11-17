# CISZA Ω — Secure Messenger v0.1.0

Nieoficjalny fork SimpleX Chat 7.0.2, Android package `pl.omega.cisza`. Gałąź: main. Licencja [GNU AGPL-3.0](LICENSE).

## Kod i pełny import

- [overlay/](overlay/) — 69 oryginalnych plików pakietu modyfikacji.
- [source/](source/) — oryginalny pakiet ZIP poprawek.
- [Instrukcja pełnego importu](docs/FULL_SOURCE_MIGRATION.md).
- [Stan źródeł](docs/SOURCE_STATUS.md) — wcześniejszy raport.
- [Skrypt importu](scripts/import-full-source.sh) i [test](tests/test_import.sh).

Pełne drzewo zostało odtworzone lokalnie i zweryfikowane (3854 pozycje, SHA drzewa Git `3fcc4366b76dd377d91228b97e21a187570b276e`). Na GitHubie opublikowano overlay i mechanizm importu; **pełny katalog project/ nie jest jeszcze opublikowany**.

Aby dokończyć import z przypiętego upstream w uwierzytelnionym Git:

```bash
git clone --single-branch --branch main https://github.com/mojealterego/Cisza.git
cd Cisza
bash scripts/import-full-source.sh --upstream --push
```

Import nie wymaga GitHub Actions, nie tworzy nowych gałęzi, nie stosuje force push. Alternatywnie obsługuje zweryfikowany lokalny Git bundle. Przed commitem weryfikuje dokładny SHA drzewa.

## Weryfikacja

16/16 testów polityki PIN, 2917/2917 kluczy PL, kontrola typów Kotlin na testowych atrapach Android API, lokalny import Git bundle i porównanie 3854 pozycji: PASS. Pełna kompilacja APK, testy na urządzeniach oraz niezależny audyt kryptograficzny: niepotwierdzone.

Opcja usunięcia danych po trzech błędnych PIN jest domyślnie wyłączona; może spowodować nieodwracalną utratę danych. Testować wyłącznie na profilu testowym. Projekt jest niezależnym forkiem i nie stanowi oficjalnego wydania SimpleX.

## Dowód kompletności i stan transportu

[Manifest wszystkich 3854 pozycji](docs/source-tree.tsv) pozwala niezależnie sprawdzić integralność plików. [Raport weryfikacji i próby transportu](docs/MIGRATION_EVIDENCE.md): lokalny bundle PASS, GitHub Actions #44017354165 zakończone błędem przed pierwszym krokiem. Pełny import nadal nie jest ukończony.
