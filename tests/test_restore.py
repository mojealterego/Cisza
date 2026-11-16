import hashlib
import tempfile
import unittest
import zipfile
from pathlib import Path

from scripts.restore_cisza import RestoreError, restore_archive


class RestoreSourceTests(unittest.TestCase):
    def make_zip(self, target: Path, entries: dict[str, bytes]) -> str:
        with zipfile.ZipFile(target, "w") as zf:
            for name, content in entries.items():
                zf.writestr(name, content)
        return hashlib.sha256(target.read_bytes()).hexdigest()

    def test_valid_archive_is_extracted_without_wrapper_directory(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            archive = root / "source.zip"
            sha = self.make_zip(archive, {"CISZA-OMEGA-v0.1.0/README.md": b"ok"})
            target = root / "copy"
            count = restore_archive(archive, target, expected_sha256=sha)
            self.assertEqual(count, 1)
            self.assertEqual((target / "README.md").read_bytes(), b"ok")

    def test_checksum_mismatch_does_not_create_destination(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            archive = root / "source.zip"
            self.make_zip(archive, {"CISZA-OMEGA-v0.1.0/README.md": b"ok"})
            with self.assertRaises(RestoreError):
                restore_archive(archive, root / "copy", expected_sha256="0" * 64)
            self.assertFalse((root / "copy").exists())

    def test_traversal_is_rejected_before_any_file_is_written(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            archive = root / "source.zip"
            sha = self.make_zip(archive, {"CISZA-OMEGA-v0.1.0/../evil": b"bad"})
            with self.assertRaises(RestoreError):
                restore_archive(archive, root / "copy", expected_sha256=sha)
            self.assertFalse((root / "evil").exists())
            self.assertFalse((root / "copy").exists())

    def test_existing_destination_is_not_overwritten(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            archive = root / "source.zip"
            sha = self.make_zip(archive, {"CISZA-OMEGA-v0.1.0/README.md": b"new"})
            dest = root / "copy"
            dest.mkdir()
            (dest / "README.md").write_text("original")
            with self.assertRaises(RestoreError):
                restore_archive(archive, dest, expected_sha256=sha)
            self.assertEqual((dest / "README.md").read_text(), "original")

    def test_wrong_prefix_is_rejected(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            archive = root / "source.zip"
            sha = self.make_zip(archive, {"other/file.txt": b"ok"})
            with self.assertRaises(RestoreError):
                restore_archive(archive, root / "copy", expected_sha256=sha)


if __name__ == "__main__":
    unittest.main()
