#!/usr/bin/env bash
set -euo pipefail
script=$(realpath "$1")
bundle=$(realpath "$2")
tmp=$(mktemp -d)
trap 'rm -rf -- "$tmp"; rm -f -- "$tmp.reject.log" "$tmp.bad.bundle"' EXIT
cd "$tmp"
git init -q -b main
git config user.name 'Import test'
git config user.email 'test@example.invalid'
git config core.autocrlf false
printf 'migration metadata\n' > README.md
git add README.md
git commit -qm baseline
base=$(git rev-parse HEAD)
expect_reject() { if bash "$script" "$@" > "$tmp.reject.log" 2>&1; then echo 'unexpected success' >&2; exit 1; fi; }
# Keep the worktree clean when exercising the actual import.
rm -f rejected.log
expect_reject "$tmp/no.bundle"
rm -f rejected.log
printf 'altered\n' > bad.bundle
# The malformed bundle is outside the worktree, because untracked files must cause a rejection.
mv bad.bundle "$tmp.bad.bundle"
expect_reject "$tmp.bad.bundle"
rm -f "$tmp.bad.bundle" rejected.log
printf 'untracked\n' > dirty.txt
expect_reject "$bundle"
rm -f dirty.txt rejected.log
[[ $(git rev-parse HEAD) == "$base" ]]
bash "$script" "$bundle"
[[ $(git rev-parse HEAD^1) == "$base" ]]
[[ $(git rev-parse HEAD:project) == 3fcc4366b76dd377d91228b97e21a187570b276e ]]
[[ $(git ls-tree -r HEAD:project | wc -l) == 3854 ]]
[[ $(git show HEAD:README.md) == 'migration metadata' ]]
head=$(git rev-parse HEAD)
bash "$script" "$bundle"
[[ $(git rev-parse HEAD) == "$head" ]]
[[ -z $(git status --porcelain) ]]
git fsck --full
printf 'PASS: invalid input, dirty tree, complete tree, preserved history, idempotence, fsck\n'
