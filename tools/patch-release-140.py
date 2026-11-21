#!/usr/bin/env python3
"""Bump the expanded and overlay-corrected Setra build to 1.4.0 (versionCode 7)."""
from pathlib import Path
import sys
root=Path(sys.argv[1]).resolve()
for rel in ("app/build.gradle.kts","tools/verify_release.py","tools/check_ads_integration.py"):
    p=root/rel
    s=p.read_text(encoding="utf-8")
    assert "1.3.1" in s, rel
    s=s.replace("1.3.1","1.4.0")
    if rel=="app/build.gradle.kts":
        assert s.count("versionCode = 6")==1
        s=s.replace("versionCode = 6","versionCode = 7")
    if rel=="tools/verify_release.py":
        s=s.replace("versionCode='6'","versionCode='7'").replace("'versionCode':6","'versionCode':7")
    p.write_text(s,encoding="utf-8")
print("Setra 1.4.0 / versionCode 7 ready")
