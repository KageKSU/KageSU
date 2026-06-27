# KageSU
<img align='right' src='../KageSU-mini.svg' width='220px' alt="логотип KageSU">


[English](../README.md) | [简体中文](../zh/README.md) | [日本語](../ja/README.md) | [Türkçe](../tr/README.md) | **Русский**

Решение для получения root-прав на уровне ядра для устройств Android.

> **KageSU** — это форк проекта [SukiSU Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra), который, в свою очередь, основан на [KernelSU](https://github.com/tiann/KernelSU). Спасибо обоим вышестоящим проектам.

[![Latest release](https://img.shields.io/github/v/release/KageKSU/KageSU?label=Release&logo=github)](https://github.com/KageKSU/KageSU/releases/latest)
[![Channel](https://img.shields.io/badge/Follow-Telegram-blue.svg?logo=telegram)](https://t.me/KageKSU)
[![License: GPL v2](https://img.shields.io/badge/License-GPL%20v2-orange.svg?logo=gnu)](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)
[![GitHub License](https://img.shields.io/github/license/tiann/KernelSU?logo=gnu)](/LICENSE)

## Особенности

1. Управление доступом `su` и root на уровне ядра.
2. [App Profile](https://kernelsu.org/guide/app-profile.html): закройте root-права для конкретных приложений.
3. Поддержка non-GKI и GKI 1.0.
4. Поддержка KPM.
5. Доработки темы менеджера и встроенный инструмент управления susfs.

## Статус совместимости

- KernelSU (до v1.0.0) официально поддерживает устройства Android GKI 2.0 (ядро 5.10+).

- Более старые ядра (4.4+) также совместимы, но ядро придется собирать вручную.

- С дополнительными бэкпортами KernelSU может поддерживать ядра серии 3.x (3.4–3.18).

- На данный момент поддерживаются только архитектуры `arm64-v8a`, `armeabi-v7a (bare)` и некоторые `X86_64`.

## Установка

См. [`guide/installation.md`](../guide/installation.md)

## Интеграция

См. [`guide/how-to-integrate.md`](../guide/how-to-integrate.md)

## Перевод

Если вы хотите предложить перевод для менеджера, пожалуйста, воспользуйтесь [Crowdin](https://crowdin.com/project/KageSU).

## Поддержка KPM

- На базе KernelPatch: мы удалили функции, дублирующие возможности KSU, оставив только поддержку KPM.
- В разработке: расширение совместимости с APatch путем интеграции дополнительных функций для обеспечения работы в различных реализациях.

**Open-source репозиторий**: [https://github.com/ShirkNeko/KageSU_KernelPatch_patch](https://github.com/ShirkNeko/KageSU_KernelPatch_patch)

**Шаблон KPM**: [https://github.com/udochina/KPM-Build-Anywhere](https://github.com/udochina/KPM-Build-Anywhere)

> [!Note]
>
> 1. Требуется `CONFIG_KPM=y`
> 2. Для non-GKI устройств требуются `CONFIG_KALLSYMS=y` и `CONFIG_KALLSYMS_ALL=y`
> 3. Для ядер ниже `4.19` требуется бэкпорт `set_memory.h` из версии `4.19`.

## Устранение неполадок

1. Если устройство зависает при удалении менеджера:
   Удалите _com.sony.playmemories.mobile_

## Спонсоры

- [ShirkNeko](https://afdian.com/a/shirkneko) (сопровождающий KageSU)
- [weishu](https://github.com/sponsors/tiann) (автор KernelSU)

## Лицензия

- Файлы в директории «kernel» находятся под лицензией [GPL-2.0-only](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)
- За исключением вышеуказанных файлов и директорий, все остальные части находятся под лицензией [GPL-3.0 or later](https://www.gnu.org/licenses/gpl-3.0.html)

## Благодарности

- [SukiSU Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra): вышестоящий проект
- [ReSukiSU](https://github.com/ReSukiSU/ReSukiSU): поддержка нескольких менеджеров (список доверенных сертификатов менеджеров)

<details>
<summary>Благодарности SukiSU Ultra</summary>

- [KernelSU](https://github.com/tiann/KernelSU): вышестоящий проект
- [MKSU](https://github.com/5ec1cff/KernelSU): Magic Mount
- [RKSU](https://github.com/rsuntk/KernelsU): поддержка non-GKI
- [susfs](https://gitlab.com/simonpunk/susfs4ksu): дополнение для скрытия root в ядре и модуль пространства пользователя для KernelSU.
- [KernelPatch](https://github.com/bmax121/KernelPatch): ключевая часть реализации модулей ядра в APatch.
</details>

<details>
<summary>Благодарности команды KernelSU</summary>

- [Kernel-Assisted Superuser](https://git.zx2c4.com/kernel-assisted-superuser/about/): Идея KernelSU.
- [Magisk](https://github.com/topjohnwu/Magisk): Мощный инструмент для получения root-прав.
- [genuine](https://github.com/brevent/genuine/): Проверка подписи APK v2.
- [Diamorphine](https://github.com/m0nad/Diamorphine): Некоторые техники руткитов.
</details>
