#!/usr/bin/env bash
# Import exact source into project/ on existing main; optional normal push.
set -euo pipefail
readonly BUNDLE_SHA=3ebeeac93a3e4d277273599dcbc67f515cfd92c59b486ca84f45855d96849bad
readonly SOURCE_COMMIT=c971161bc60ce7bf484da59e33c5c88f7554ff22
readonly SOURCE_TREE=3fcc4366b76dd377d91228b97e21a187570b276e
readonly UPSTREAM_COMMIT=4df04bdb3ff94059734ad2da7d2766de6dba2cc7
readonly UPSTREAM_TREE=9acaf0e91787e40794e3de94246cf35c2ed7985f
readonly UPSTREAM_URL=https://github.com/simplex-chat/simplex-chat.git
script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
fail() { printf 'IMPORT FAILED: %s\n' "$*" >&2; exit 1; }
[[ $# -ge 1 && $# -le 2 ]] || fail 'usage: import-full-source.sh (--upstream|BUNDLE) [--push]'
push=false
if [[ $# == 2 ]]; then [[ $2 == --push ]] || fail 'unknown argument'; push=true; fi
source=$1
if [[ $source != --upstream ]]; then
  [[ -f $source ]] || fail 'bundle not found'
  bundle=$(realpath -- "$source")
fi
[[ $(git symbolic-ref --quiet --short HEAD) == main ]] || fail 'only main is supported'
cd "$(git rev-parse --show-toplevel)"
[[ -z $(git status --porcelain --untracked-files=all) ]] || fail 'worktree/index must be clean'
if [[ $source != --upstream ]]; then
  actual=$(sha256sum -- "$bundle"); actual=${actual%% *}
  [[ $actual == "$BUNDLE_SHA" ]] || fail 'bundle SHA-256 mismatch'
fi
base=$(git rev-parse HEAD)
if $push; then
  url=$(git remote get-url --push origin)
  case "$url" in
    https://github.com/mojealterego/Cisza.git|https://github.com/mojealterego/Cisza|git@github.com:mojealterego/Cisza.git|ssh://git@github.com/mojealterego/Cisza.git) ;;
    *) fail 'origin push URL must identify mojealterego/Cisza' ;;
  esac
  remote=$(git ls-remote --exit-code "$url" refs/heads/main); remote=${remote%%$'\t'*}
  [[ $remote == "$base" ]] || fail 'remote main differs; synchronize first'
fi
if git cat-file -e HEAD:project 2>/dev/null; then
  [[ $(git rev-parse HEAD:project) == "$SOURCE_TREE" ]] || fail 'project already exists with different content'
  printf 'Already imported: %s\n' "$SOURCE_TREE"
else
  [[ ! -e project && ! -L project ]] || fail 'project path already exists'
  git var GIT_AUTHOR_IDENT >/dev/null
  git var GIT_COMMITTER_IDENT >/dev/null
  if [[ $source == --upstream ]]; then
    git fetch --no-tags --no-recurse-submodules "$UPSTREAM_URL" "$UPSTREAM_COMMIT"
    [[ $(git rev-parse FETCH_HEAD) == "$UPSTREAM_COMMIT" ]] || fail 'upstream commit mismatch'
    [[ $(git rev-parse FETCH_HEAD^{tree}) == "$UPSTREAM_TREE" ]] || fail 'upstream tree mismatch'
    tree=$(bash "$script_dir/assemble-cisza-tree.sh" "$UPSTREAM_TREE" "$base")
  else
    git bundle verify "$bundle"
    git fetch --no-tags --no-recurse-submodules "$bundle" refs/heads/main
    [[ $(git rev-parse FETCH_HEAD) == "$SOURCE_COMMIT" ]] || fail 'bundle commit mismatch'
    tree=$(git rev-parse FETCH_HEAD^{tree})
  fi
  [[ $tree == "$SOURCE_TREE" ]] || fail 'source tree mismatch'
  [[ $(git rev-parse HEAD) == "$base" ]] || fail 'local HEAD changed during import'
  [[ -z $(git status --porcelain --untracked-files=all) ]] || fail 'worktree changed during verification'
  git read-tree --prefix=project/ -u "$SOURCE_TREE"
  staged=$(git write-tree)
  [[ $(git rev-parse "$staged:project") == "$SOURCE_TREE" ]] || fail 'staged tree mismatch'
  git commit -m 'source: import exact CISZA Omega v0.1.0 full tree'
  [[ $(git rev-parse HEAD:project) == "$SOURCE_TREE" ]] || fail 'committed tree mismatch'
  [[ $(git rev-parse HEAD^1) == "$base" ]] || fail 'unexpected commit parent'
fi
printf 'Verified source tree: %s\n' "$SOURCE_TREE"
if $push; then
  # No force; concurrent remote updates will reject this push.
  git push origin HEAD:refs/heads/main
  published=$(git ls-remote --exit-code "$url" refs/heads/main); published=${published%%$'\t'*}
  [[ $published == "$(git rev-parse HEAD)" ]] || fail 'remote HEAD verification failed'
  printf 'Published main: %s\n' "$published"
fi
