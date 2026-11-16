# Setra Mahjong — Android 1.2.0

هذا المستودع مخصص لبناء نسخة **Setra Mahjong 1.2.0 (3)** الموقّعة بنفس شهادة الإصدار السابق.

## حالة المستودع
- ملف GitHub Actions جاهز في `.github/workflows/build-release.yml`.
- **ملف مصدر المشروع ZIP لم يُرفع بعد**. يجب رفع `SetraMahjong_120_Project.zip` إلى جذر المستودع، ثم تبدأ مهمة البناء تلقائيًا.
- لا يوجد APK جديد حتى ينجح البناء والتوقيع ويظهر ملف `app-release.apk` ضمن Artifacts.

## رفع المشروع من الهاتف
1. افتح https://github.com/mhmmad444410aa-ai/SetraMahjong/upload/main
2. اضغط `choose your files` واختر `SetraMahjong_120_Project.zip` الذي نزّلته من ChatGPT.
3. اضغط `Commit changes`.
4. اذهب إلى https://github.com/mhmmad444410aa-ai/SetraMahjong/actions

## Secrets المطلوبة
من `Settings → Secrets and variables → Actions → New repository secret`، أضف:
- `SETRA_KEYSTORE_BASE64`
- `SETRA_STORE_PASSWORD`
- `SETRA_KEY_ALIAS`
- `SETRA_KEY_PASSWORD`
- `SETRA_DEVELOPER_NAME`
- `SETRA_CONTACT_EMAIL`
- `SETRA_PRIVACY_URL` (صفحة HTTPS عامة لسياسة الخصوصية)

**تحذير:** لا ترفع ملف `.jks` أو حزمة المفتاح الخاصة أو كلمات المرور أو نص base64 إلى المستودع العام؛ خزّنها في GitHub Secrets فقط.

## هوية التطبيق
- Package: `com.mhmmad.setramahjong`
- Version name: `1.2.0`
- Version code: `3`
- Banner: `l8o7yoez3b`
- Interstitial: `c6r832l0lr`
- Expected signing certificate SHA-256: `7C:F0:F4:DE:26:60:DE:10:2C:F1:0A:EA:E0:D6:2F:D3:F3:3E:44:1F:47:D3:90:A7:B9:32:27:02:D6:FD:13:EC`

## تنزيل النسخة النهائية بعد نجاح البناء
`Actions → Build Setra Mahjong Signed Release → [latest successful run] → Artifacts → SetraMahjong-1.2.0-Release-production`.

الملف الناتج يجب التحقق منه على جهاز فعلي ومراجعة ظهور الإعلانات ومطابقة سياسة الخصوصية قبل النشر في AppGallery.
