# KageSU
<img align='right' src='../KageSU-mini.svg' width='220px' alt="KageSU logo">


[English](../README.md) | [简体中文](../zh/README.md) | [日本語](../ja/README.md) | **Türkçe** | [Русский](../ru/README.md)

Android cihazlar için çekirdek tabanlı bir root çözümü.

> **KageSU**, kendisi [KernelSU](https://github.com/tiann/KernelSU) tabanlı olan [SukiSU Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra) projesinin bir çatalıdır (fork). Her iki üst (upstream) projeye de teşekkürler.

[![Latest release](https://img.shields.io/github/v/release/KageKSU/KageSU?label=Release&logo=github)](https://github.com/KageKSU/KageSU/releases/latest)
[![Channel](https://img.shields.io/badge/Follow-Telegram-blue.svg?logo=telegram)](https://t.me/KageKSU)
[![License: GPL v2](https://img.shields.io/badge/License-GPL%20v2-orange.svg?logo=gnu)](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)
[![GitHub License](https://img.shields.io/github/license/tiann/KernelSU?logo=gnu)](/LICENSE)

## Özellikler

1. Çekirdek tabanlı `su` ve root erişim yönetimi.
2. [App Profile](https://kernelsu.org/guide/app-profile.html): root yetkilerini bir kafeste kilitleyin.
3. non-GKI ve GKI 1.0 desteği.
4. KPM desteği.
5. Yönetici temasında iyileştirmeler ve dahili susfs yönetim aracı.

## Uyumluluk Durumu

- KernelSU (v1.0.0 öncesi) resmi olarak Android GKI 2.0 cihazlarını destekler (çekirdek 5.10+).

- Eski çekirdekler (4.4+) de uyumludur, ancak çekirdeğin manuel olarak derlenmesi gerekir.

- Daha fazla geri taşıma (backport) ile KernelSU, 3.x çekirdeğini (3.4-3.18) destekleyebilir.

- Şu anda yalnızca `arm64-v8a`, `armeabi-v7a (bare)` ve bazı `X86_64` desteklenmektedir.

## Kurulum

[`guide/installation.md`](../guide/installation.md) dosyasına bakın.

## Entegrasyon

[`guide/how-to-integrate.md`](../guide/how-to-integrate.md) dosyasına bakın.

## Çeviri

Yönetici için bir çeviri göndermek istiyorsanız, lütfen [Crowdin](https://crowdin.com/project/KageSU) adresine gidin.

## KPM Desteği

- KernelPatch tabanlı olarak, KSU ile çakışan işlevleri kaldırdık ve yalnızca KPM desteğini koruduk.
- Devam ediyor: Farklı uygulamalar arasında uyumluluğu sağlamak için ek işlevler entegre ederek APatch uyumluluğunu genişletmek.

**Açık kaynak deposu**: [https://github.com/ShirkNeko/KageSU_KernelPatch_patch](https://github.com/ShirkNeko/KageSU_KernelPatch_patch)

**KPM şablonu**: [https://github.com/udochina/KPM-Build-Anywhere](https://github.com/udochina/KPM-Build-Anywhere)

> [!Note]
>
> 1. `CONFIG_KPM=y` gerektirir
> 2. non-GKI cihazlar `CONFIG_KALLSYMS=y` ve `CONFIG_KALLSYMS_ALL=y` gerektirir
> 3. `4.19` altındaki çekirdekler için `4.19` sürümünden `set_memory.h` geri taşınması gerekir.

## Sorun Giderme

1. Yönetici uygulamasının kaldırılması sırasında cihaz takılıyor mu?
   _com.sony.playmemories.mobile_ uygulamasını kaldırın.

## Sponsor

- [ShirkNeko](https://afdian.com/a/shirkneko) (KageSU geliştiricisi)
- [weishu](https://github.com/sponsors/tiann) (KernelSU'nun yazarı)

## Lisans

- `kernel` dizinindeki dosyalar [GPL-2.0-only](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html) lisansı altındadır.
- Anime karakter ifadeleri içeren `ic_launcher(?!.*alt.*).*` dosyalarının görüntüleri [怡子曰曰](https://space.bilibili.com/10545509) tarafından telif hakkıyla korunmaktadır, görüntülerdeki Marka Fikri Mülkiyeti [明风 OuO](https://space.bilibili.com/274939213)'ye aittir ve vektörleştirme @MiRinChan tarafından yapılmıştır. Bu dosyaları kullanmadan önce, [Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International](https://creativecommons.org/licenses/by-nc-sa/4.0/legalcode.txt) ile uyumlu olmanın yanı sıra, bu sanatsal içerikleri kullanmak için iki yazarın yetkilendirmesine de uymanız gerekir.
- Yukarıda belirtilen dosyalar veya dizinler hariç, diğer tüm parçalar [GPL-3.0 veya üzeri](https://www.gnu.org/licenses/gpl-3.0.html)'dir.

## Katkıda Bulunanlar

- [SukiSU Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra): üst proje (upstream)

<details>
<summary>SukiSU Ultra'nın katkıda bulunanları</summary>

- [KernelSU](https://github.com/tiann/KernelSU): üst proje (upstream)
- [MKSU](https://github.com/5ec1cff/KernelSU): Magic Mount
- [RKSU](https://github.com/rsuntk/KernelsU): non-GKI desteği
- [susfs](https://gitlab.com/simonpunk/susfs4ksu): KernelSU için root gizleme çekirdek yamaları ve kullanıcı alanı modülü olan bir eklenti.
- [KernelPatch](https://github.com/bmax121/KernelPatch): KernelPatch, çekirdek modülünün APatch uygulamasının kilit bir parçasıdır
</details>

<details>
<summary>KernelSU'nun katkıda bulunanları</summary>

- [Kernel-Assisted Superuser](https://git.zx2c4.com/kernel-assisted-superuser/about/): KernelSU fikri.
- [Magisk](https://github.com/topjohnwu/Magisk): Güçlü root aracı.
- [genuine](https://github.com/brevent/genuine/): APK v2 imza doğrulama.
- [Diamorphine](https://github.com/m0nad/Diamorphine): Bazı rootkit becerileri.
</details>
