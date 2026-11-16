#!/usr/bin/env python3
"""Setra Mahjong v1.2.1: English-first, remove advertising choice popups and settings.
Consent rules are never bypassed: no ads if Huawei reports consent required and unknown.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1])

def edit(rel,changes):
    p=root/rel
    t=p.read_text(encoding="utf-8")
    for old,new in changes:
        n=t.count(old)
        if n!=1: raise RuntimeError(f"{rel}: expected one match, got {n}: {old[:60]!r}")
        t=t.replace(old,new)
    p.write_text(t,encoding="utf-8")

def cut(t,start,end):
    i=t.index(start)
    j=t.index(end,i+len(start))
    return t[:i]+t[j:]

rel="app/src/main/java/com/mhmmad/setramahjong/ui/Screens.kt"
p=root/rel
s=p.read_text(encoding="utf-8")
s=cut(s,'            var consentDialog by remember{mutableStateOf(false)}\n','            val rootPages=')
s=cut(s,'                                "adprivacy"->InfoPage(vm,R.string.privacy_choices) {\n','                            }\n                        }\n                        if(ui.page in rootPages)')
s=cut(s,'                if(adPrivacy.review)AlertDialog(onDismissRequest=', '            }\n        }\n    }\n}\n@Composable fun GoldButton')
s=s.replace('    val device=Locale.getDefault()\n','')
s=s.replace('val tag=when(prefs.language){"ar"->"ar";"pt-BR"->"pt-BR";"en-GB"->"en-GB";else->when(device.language){"ar"->"ar";"pt"->"pt-BR";"en"->"en-GB";else->"en-GB"}}','val tag=when(prefs.language){"ar"->"ar";"pt-BR"->"pt-BR";else->"en-GB"}')
s=s.replace('    val adPrivacy by adProvider.privacy.collectAsStateWithLifecycle()\n','')
s=s.replace('adProvider.permission(prefs.consent=="granted")','adProvider.permission(prefs.consent!="declined")')
s=s.replace('"privacy","adprivacy"->vm.navigate("about")','"privacy"->vm.navigate("about")')
s=s.replace(';if(BuildConfig.PETAL_ENABLED)TextButton(onClick={vm.navigate("adprivacy")}){Text(title(R.string.privacy_choices))}','')
s=s.replace('listOf("system" to R.string.language_system,"en-GB" to R.string.language_english','listOf("en-GB" to R.string.language_english')
s=s.replace('title==R.string.privacy||title==R.string.privacy_choices','title==R.string.privacy')
assert all(tok not in s for tok in ('adPrivacy.review','consentDialog','adprivacy','"system" to R.string.language_system'))
assert s.count('@Composable fun GoldButton')==1
assert 'val rootPages=' in s and 'if(ui.page in rootPages)' in s
p.write_text(s,encoding='utf-8')
edit('app/src/main/java/com/mhmmad/setramahjong/data/Storage.kt', [
 ('val language: String="system"','val language: String="en-GB"'),
 ('p[stringPreferencesKey("language")]?:"system"','p[stringPreferencesKey("language")]?:"en-GB"')])
edit('app/src/petal/java/com/mhmmad/setramahjong/ads/PetalProvider.kt', [
 ('// Called only after explicit initial permission, or previously stored permission.',
  '// Query the Huawei consent requirement without silently claiming that the user consented.'),
 ('''                    if(permissionGranted&&status==ConsentStatus.NON_PERSONALIZED&&!needConsent){initialise()}
                    else if(permissionGranted&&status==ConsentStatus.NON_PERSONALIZED){initialise()}
                    else privacyState.value=AdPrivacy(partners=partners,review=true)''',
  '''                    // Never show our own advertisement-choice dialogue.
                    // If consent is mandatory and unknown, advertising stays OFF.
                    if(permissionGranted&&(!needConsent||status==ConsentStatus.NON_PERSONALIZED||status==ConsentStatus.PERSONALIZED))initialise()
                    else {enabled=false;privacyState.value=AdPrivacy()}''')])
edit('app/build.gradle.kts', [('versionCode = 3','versionCode = 4'),('versionName = "1.2.0"','versionName = "1.2.1"')])
edit('tools/verify_release.py', [
 ('"versionCode=\\'3\\'" in badging and "versionName=\\'1.2.0\\'" in badging','"versionCode=\\'4\\'" in badging and "versionName=\\'1.2.1\\'" in badging'),
 ("'versionName':'1.2.0','versionCode':3","'versionName':'1.2.1','versionCode':4")])
edit('tools/check_ads_integration.py', [
 ('versionCode = 3','versionCode = 4'),
 ('versionName = "1.2.0"','versionName = "1.2.1"'),
 ("versionCode='3'","versionCode='4'"),
 ("versionName='1.2.0'","versionName='1.2.1'"),
 ('Static Setra Mahjong 1.2.0 integration checks','Static Setra Mahjong 1.2.1 integration checks')])
petal=(root/'app/src/petal/java/com/mhmmad/setramahjong/ads/PetalProvider.kt').read_text()
assert 'review=true' not in petal
assert 'setNonPersonalizedAd(1)' in petal
assert 'if(permissionGranted&&(!needConsent||' in petal
assert 'val tag=when(prefs.language){"ar"->"ar";"pt-BR"->"pt-BR";else->"en-GB"}' in s
print('PASS: v1.2.1 UK English default, no custom advertising messages, Huawei consent gate retained')
