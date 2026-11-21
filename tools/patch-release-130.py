#!/usr/bin/env python3
"""Apply the v1.3.0 expansion to the original v1.2.0 archive after patch-release-121.py."""
from pathlib import Path
import shutil, sys, xml.etree.ElementTree as ET
root=Path(sys.argv[1]).resolve()
here=Path(__file__).resolve().parent.parent
ns="http://schemas.android.com/apk/res/android"
ET.register_namespace("android",ns)
a=lambda name:"{"+ns+"}"+name
manifest=root/"app/src/main/AndroidManifest.xml"
doc=ET.parse(manifest); app=doc.getroot().find("application")
if app is None: raise RuntimeError("Missing application")
launchers=[]
for activity in app.findall("activity")+app.findall("activity-alias"):
    for f in activity.findall("intent-filter"):
        if any(x.get(a("name"))=="android.intent.action.MAIN" for x in f.findall("action")) and any(x.get(a("name"))=="android.intent.category.LAUNCHER" for x in f.findall("category")):
            launchers.append((activity,f))
if len(launchers)!=1: raise RuntimeError("Expected exactly one original launcher")
old,f=launchers[0]
if old.tag!="activity": raise RuntimeError("Unexpected launcher alias: inspect manually")
name=old.get(a("name"))
if not name: raise RuntimeError("Missing launcher class")
package_name="com.mhmmad.setramahjong"
if name.startswith("."):name=package_name+name
elif "." not in name:name=package_name+"."+name
if not name.startswith(package_name+"."):raise RuntimeError("Unexpected original package")
if f.findall("data") or len(f.findall("action"))!=1 or len(f.findall("category"))!=1:raise RuntimeError("Unexpected original launcher filter")
old.remove(f)
new=ET.SubElement(app,"activity",{a("name"):package_name+".expansion.GardenActivity",a("exported"):"true"})
f=ET.SubElement(new,"intent-filter");ET.SubElement(f,"action",{a("name"):"android.intent.action.MAIN"});ET.SubElement(f,"category",{a("name"):"android.intent.category.LAUNCHER"})
doc.write(manifest,encoding="utf-8",xml_declaration=True)
dest=root/"app/src/main/java/com/mhmmad/setramahjong/expansion"
dest.mkdir(parents=True,exist_ok=False)
for src in (here/"expansion").glob("*.java"):
    if src.name!="GardenEngineTest.java":shutil.copy2(src,dest/src.name)
(dest/"ClassicTarget.java").write_text("package com.mhmmad.setramahjong.expansion;\nfinal class ClassicTarget { static final String NAME = "+repr(name).replace("'","\"")+"; }\n",encoding="utf-8")
for rel in ["app/build.gradle.kts","tools/verify_release.py","tools/check_ads_integration.py"]:
    p=root/rel;s=p.read_text(encoding="utf-8")
    if "1.2.1" not in s:raise RuntimeError("Missing expected 1.2.1 markers in "+rel)
    s=s.replace("1.2.1","1.3.0").replace("versionCode = 4","versionCode = 5").replace("versionCode='4'","versionCode='5'").replace("'versionCode':4","'versionCode':5")
    if rel=="tools/verify_release.py":
        old_cert="7C:F0:F4:DE:26:60:DE:10:2C:F1:0A:EA:E0:D6:2F:D3:F3:3E:44:1F:47:D3:90:A7:B9:32:27:02:D6:FD:13:EC"
        new_cert="40:DB:65:6D:09:0E:22:D9:D1:CA:99:8E:F1:CC:9D:F5:6A:F5:01:F0:39:88:1A:39:2F:A5:39:EF:E8:46:A9:4B"
        s=s.replace(old_cert,new_cert).replace(old_cert.replace(":","").lower(),new_cert.replace(":","").lower())
        # First public release: verify against the explicitly authorized new signing cert.
        # Keep a strict 64-hex-digit certificate check rather than bypassing verification.
        # The inherited verifier expresses the old signature as a Python assert.
        # Locate its exact cert-vs-expected expression using Python's AST, not
        # a brittle textual match. Keep verification enabled with a fixed new digest.
        import ast
        parsed=ast.parse(s)
        candidates=[]
        for node in ast.walk(parsed):
            if not isinstance(node,ast.Assert) or not isinstance(node.test,ast.Compare): continue
            cmp=node.test
            if (isinstance(cmp.left,ast.Name) and cmp.left.id=="cert" and
                len(cmp.ops)==1 and isinstance(cmp.ops[0],ast.Eq) and
                len(cmp.comparators)==1 and isinstance(cmp.comparators[0],ast.Name) and
                cmp.comparators[0].id=="expected"):
                candidates.append(node)
        if len(candidates)!=1: raise RuntimeError("Expected exactly one certificate identity assertion")
        node=candidates[0]
        lines=s.splitlines(keepends=True)
        original=lines[node.lineno-1]
        indent=original[:len(original)-len(original.lstrip())]
        replacement=chr(10).join(indent+x for x in [
            "import re",
            "expected='40db656d090e22d9d1ca998ef1cc9df56af501f039881a392fa539efe846a94b'",
            "normalized=re.sub(r'[^0-9a-f]', '', str(cert).lower())",
            "assert len(normalized)==64 and normalized==expected, 'Signing certificate does not match the authorized Setra 1.3.0 certificate'",
        ])+chr(10)
        lines[node.lineno-1:node.end_lineno]=[replacement]
        s="".join(lines)

    p.write_text(s,encoding="utf-8")
print("Expansion integrated; original activity retained:",name)
