# Dowód kompletności drzewa — 2026-11-17

Źródła: SimpleX Chat 7.0.2 (commit 4df04bdb3ff94059734ad2da7d2766de6dba2cc7) plus overlay CISZA Ω. Oczekiwana liczba pozycji: 3854. Pełne drzewo: 3fcc4366b76dd377d91228b97e21a187570b276e.

Plik [source-tree.tsv](source-tree.tsv) jest kompletnym manifestem: ścieżka, tryb Git, SHA-1 obiektu blob, UTF-8, znak TAB między kolumnami, LF między rekordami. Zawiera 3854 wiersze, 413861 bajtów. SHA-256: aec154ba9cde9a6c816e1cce3863e733bde912bbc842732e11021aeb206cbc10. Git blob: 7e4b982b711c18214e5010a1f5f1a99ffb4fb711.

Manifest dla lokalnego Git bundle został wygenerowany przez git ls-tree -r -z --full-tree main i niezależnie odtworzony z metadanych GitHub dla przypiętego upstream i 68 plików overlay. Identyczny identyfikator blob jest warunkiem publikacji manifestu.

Weryfikacja lokalna 2026-11-17:
- git bundle verify: PASS, pełna historia, ref main c971161bc60ce7bf484da59e33c5c88f7554ff22.
- git fsck --full po pobraniu bundle do pustego repozytorium: PASS.
- git rev-parse main^{tree}: zgodny oczekiwany SHA.
- 3854 unikatowe ścieżki: PASS.
- SHA-256 bundle: 3ebeeac93a3e4d277273599dcbc67f515cfd92c59b486ca84f45855d96849bad.

## Próba transportu GitHub Actions

Commit 278e229efce1e9a0c980d61031dbdbcf7f240f2e uruchomił jednorazowy import na standardowym runnerze publicznego repozytorium. Run 44017354165, job 133818368916: completed/failure, bez uruchomionych kroków. Log joba zwrócił 404 BlobNotFound; dokładna przyczyna odmowy uruchomienia niepotwierdzona. Nie jest to dowód błędu kodu importera. Nie dokonano zmian ustawień rozliczeń, nie uruchamiano płatnej infrastruktury. Wyłączono automatyczny trigger, workflow pozostaje opcjonalny (workflow_dispatch). Preferowaną ścieżką jest nadal zwykły Git z uwierzytelnionego urządzenia lub niezależnej infrastruktury.

Kontrola git ls-remote z aktualnego kontenera: BLOCKED (Could not resolve host: github.com).

Stan pełnego importu zdalnego: BLOCKED. Manifest identyfikuje wszystkie pliki, ale nie stanowi ich kopii. Nie deklaruje się ukończenia migracji ani kompilacji APK.
