#!/usr/bin/env python3
"""Dependency-light static acceptance checks. Does not claim an Android APK build."""
from collections import Counter
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parent.parent
MP = ROOT / 'apps/multiplatform'
MR = MP / 'common/src/commonMain/resources/MR'


def strings(locale):
    tree = ET.parse(MR / locale / 'strings.xml')
    entries = tree.getroot().findall('string')
    return {s.attrib['name']: ''.join(s.itertext()) for s in entries}


def main():
    checks = []
    base, pl = strings('base'), strings('pl')
    checks.append(('all base translation keys have Polish equivalents', set(base) <= set(pl)))
    checks.append(('no unexpected Polish keys', set(pl) <= set(base)))
    checks.append(('brand name', base.get('app_name') == 'CISZA Ω' and pl.get('app_name') == 'CISZA Ω'))
    pattern = re.compile(r'%(?:\d+\$)?(?:0?\d+)?[sdfoxeg]')
    format_errors = [k for k, v in base.items() if Counter(pattern.findall(v)) != Counter(pattern.findall(pl[k]))]
    checks.append(('all positional format placeholders preserved', not format_errors))
    gradle = (MP / 'android/build.gradle.kts').read_text()
    checks.append(('distinct Android application ID', 'applicationId = "pl.omega.cisza"' in gradle))
    manifest = MP / 'android/src/main/AndroidManifest.xml'
    ET.parse(manifest)
    manifest_text = manifest.read_text()
    checks.append(('Android backup disabled', 'android:allowBackup="false"' in manifest_text))
    cmake = (MP / 'common/src/commonMain/cpp/android/CMakeLists.txt').read_text()
    checks.append(('native hardening flags', all(x in cmake for x in ['-fstack-protector-strong','-Wformat-security','-D_FORTIFY_SOURCE=2','-Wl,-z,relro','-Wl,-z,now'])))
    checks.append(('license retained', 'GNU AFFERO GENERAL PUBLIC LICENSE' in (ROOT/'LICENSE').read_text()[:400]))
    security = MP/'common/src/androidMain/kotlin/chat/simplex/common/security'
    checks.append(('guard policy and Keystore implementation', (security/'AttemptPolicy.kt').is_file() and (security/'CiszaGuard.kt').is_file()))
    checks.append(('platform authentication integrated', 'ciszaGuardAuthenticate' in (MP/'common/src/commonMain/kotlin/chat/simplex/common/views/localauth/LocalAuthView.kt').read_text()))
    for name, okay in checks:
        print(('PASS' if okay else 'FAIL') + ' ' + name)
    print(f'Polish localization: {len(set(base)&set(pl))}/{len(base)}; placeholder mismatches: {len(format_errors)}')
    return 0 if all(x[1] for x in checks) else 1

if __name__ == '__main__':
    sys.exit(main())
