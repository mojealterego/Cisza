#!/usr/bin/env bash
# Assemble a source tree from a base Git tree and the published CISZA overlay.
# This is index-only; no checkout or branch is created.
set -euo pipefail
[[ $# == 2 ]] || { echo 'usage: assemble-cisza-tree.sh BASE_TREE OVERLAY_COMMIT' >&2; exit 1; }
base=$(git rev-parse --verify "$1^{tree}")
overlay=$(git rev-parse --verify "$2:overlay")
[[ $(git cat-file -t "$overlay") == tree ]] || { echo 'overlay is not a tree' >&2; exit 1; }
work=$(mktemp -d)
trap 'rm -rf -- "$work"' EXIT
export GIT_INDEX_FILE="$work/index"
git read-tree "$base"
# The original archive excludes this upstream submodule commit, but retains .gitmodules.
git update-index --force-remove -- apps/multiplatform/external/nanohttpd/upstream
git ls-tree -rz "$overlay" > "$work/overlay.list"
count=0
while IFS= read -r -d '' entry; do
  meta=${entry%%$'\t'*}; path=${entry#*$'\t'}
  [[ $path != APPLY_OVERLAY.txt ]] || continue
  read -r mode kind oid <<< "$meta"
  [[ $kind == blob && ( $mode == 100644 || $mode == 100755 ) ]] || { echo 'invalid overlay member' >&2; exit 1; }
  printf '%s %s\t%s\0' "$mode" "$oid" "$path" >> "$work/index-info"
  count=$((count+1))
done < "$work/overlay.list"
[[ $count == 68 ]] || { echo "unexpected overlay file count: $count" >&2; exit 1; }
git update-index -z --index-info < "$work/index-info"
git write-tree
