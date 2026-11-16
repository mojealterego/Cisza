#!/usr/bin/env python3
"""Restore a SHA-256-pinned CISZA Ω v0.1.0 source archive without unsafe ZIP paths.

No dependency on GitHub Actions or third-party Python packages.
"""

from __future__ import annotations

import argparse
import hashlib
import os
import shutil
import stat
import sys
import tempfile
import zipfile
from pathlib import Path, PurePosixPath

ARCHIVE_SHA256 = "3b65013dc9c80f7b6df0df29a06ebb5f222bd7cf1d5bd07a60f06ce5a55d2a85"
PREFIX = "CISZA-OMEGA-v0.1.0"
# Single link in the original upstream source archive; intentionally pinned.
ALLOWED_SYMLINK = "apps/simplex-directory-service/web/directory.js"
ALLOWED_SYMLINK_TARGET = "../../../website/src/js/directory.js"


class RestoreError(Exception):
    """The input source cannot be safely restored."""


def _sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as src:
        for chunk in iter(lambda: src.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def _validate_members(zf: zipfile.ZipFile) -> list[tuple[zipfile.ZipInfo, Path, bool]]:
    validated: list[tuple[zipfile.ZipInfo, Path, bool]] = []
    seen: set[str] = set()
    for info in zf.infolist():
        name = info.filename
        if "\\" in name or "\x00" in name or not name.startswith(PREFIX + "/"):
            raise RestoreError(f"Unexpected or unsafe ZIP member: {name!r}")
        pieces = name.rstrip("/").split("/")
        if any(piece in ("", ".", "..") for piece in pieces):
            raise RestoreError(f"Unsafe ZIP member: {name!r}")
        rel = "/".join(pieces[1:])
        if not rel:
            continue
        if rel in seen:
            raise RestoreError(f"Duplicate ZIP member: {name!r}")
        seen.add(rel)
        p = PurePosixPath(rel)
        if p.is_absolute():
            raise RestoreError(f"Absolute path: {name!r}")
        mode = (info.external_attr >> 16) & 0o170000
        is_link = mode == stat.S_IFLNK
        if mode not in (0, stat.S_IFREG, stat.S_IFDIR, stat.S_IFLNK):
            raise RestoreError(f"Unsupported ZIP member type: {name!r}")
        if is_link:
            if rel != ALLOWED_SYMLINK:
                raise RestoreError(f"Unexpected symlink: {name!r}")
            if zf.read(info).decode("utf-8") != ALLOWED_SYMLINK_TARGET:
                raise RestoreError(f"Unexpected symlink target: {name!r}")
        validated.append((info, Path(*p.parts), is_link))
    if not validated:
        raise RestoreError("No source files in the ZIP archive")
    return validated


def restore_archive(
    archive: Path, destination: Path, *, expected_sha256: str = ARCHIVE_SHA256
) -> int:
    """Copy verified source files atomically into a previously nonexistent directory.

    Return count of regular files and trusted symlinks extracted. Never overwrite
    an existing destination. A failed extraction does not leave a partial source.
    """
    archive = archive.expanduser().resolve(strict=True)
    if not archive.is_file():
        raise RestoreError(f"Not a file: {archive}")
    actual = _sha256(archive)
    if actual != expected_sha256.lower():
        raise RestoreError(f"SHA-256 mismatch: expected {expected_sha256}, got {actual}")

    destination = destination.expanduser().absolute()
    if destination.exists() or destination.is_symlink():
        raise RestoreError(f"Destination already exists: {destination}")
    destination.parent.mkdir(parents=True, exist_ok=True)

    with zipfile.ZipFile(archive) as zf:
        members = _validate_members(zf)
        with tempfile.TemporaryDirectory(prefix=".cisza-extract-", dir=destination.parent) as tmp:
            root = Path(tmp) / "source"
            root.mkdir()
            count = 0
            for info, rel, is_link in members:
                target = root / rel
                if info.is_dir():
                    target.mkdir(parents=True, exist_ok=True)
                    continue
                target.parent.mkdir(parents=True, exist_ok=True)
                if is_link:
                    symlink_target = (target.parent / ALLOWED_SYMLINK_TARGET).resolve()
                    if not symlink_target.is_relative_to(root.resolve()):
                        raise RestoreError(f"Symlink target escapes extraction root: {rel}")
                    os.symlink(ALLOWED_SYMLINK_TARGET, target)
                else:
                    with zf.open(info) as source, target.open("xb") as dest:
                        shutil.copyfileobj(source, dest, length=1024 * 1024)
                    if (info.external_attr >> 16) & 0o111:
                        target.chmod(target.stat().st_mode | 0o111)
                count += 1
            if destination.exists() or destination.is_symlink():
                raise RestoreError(f"Destination appeared during extraction: {destination}")
            root.rename(destination)
    return count


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("archive", type=Path, help="Original CISZA_OMEGA_v0.1.0_FULL_SOURCE.zip")
    parser.add_argument("destination", type=Path, help="New, nonexistent output directory")
    args = parser.parse_args()
    try:
        n = restore_archive(args.archive, args.destination)
    except (RestoreError, OSError, zipfile.BadZipFile, UnicodeError) as exc:
        print(f"RESTORE FAILED: {exc}", file=sys.stderr)
        return 1
    print(f"OK: restored {n} files to {args.destination}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
