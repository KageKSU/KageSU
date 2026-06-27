# KageSU
<img align='right' src='../KageSU-mini.svg' width='220px' alt="KageSU logo">


[English](../README.md) | [简体中文](../zh/README.md) | **日本語** | [Türkçe](../tr/README.md) | [Русский](../ru/README.md)

Android デバイス向けのカーネルベースの root ソリューションです。

> **KageSU** は [SukiSU Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra) のフォークであり、SukiSU Ultra 自体は [KernelSU](https://github.com/tiann/KernelSU) をベースにしています。両方のアップストリームプロジェクトに感謝します。

[![Latest release](https://img.shields.io/github/v/release/KageKSU/KageSU?label=Release&logo=github)](https://github.com/KageKSU/KageSU/releases/latest)
[![Channel](https://img.shields.io/badge/Follow-Telegram-blue.svg?logo=telegram)](https://t.me/KageKSU)
[![License: GPL v2](https://img.shields.io/badge/License-GPL%20v2-orange.svg?logo=gnu)](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)
[![GitHub License](https://img.shields.io/github/license/tiann/KernelSU?logo=gnu)](/LICENSE)

## 機能

1. カーネルベースな `su` および root アクセスの管理。
2. [App Profile](https://kernelsu.org/guide/app-profile.html): root 権限をケージ内にロックします。
3. 非 GKI および GKI 1.0 に対応。
4. KPM に対応。
5. マネージャーのテーマ調整と、内蔵の susfs 管理ツール。

## 互換性の状態

- KernelSU (v1.0.0 より前) は Android GKI 2.0 のデバイス (カーネル 5.10 以降) を公式に対応しています。

- 古いカーネル (4.4 以降) も互換性がありますが、カーネルを手動で再ビルドする必要があります。

- より多くのバックポートにより、KernelSU は 3.x カーネル (3.4-3.18) にも対応可能です。

- 現在 `arm64-v8a`、`armeabi-v7a (bare)` および一部の `X86_64` に対応しています。

## インストール

[`guide/installation.md`](../guide/installation.md) を参照してください。

## 統合

[`guide/how-to-integrate.md`](../guide/how-to-integrate.md) を参照してください。

## 翻訳

マネージャーの翻訳を提出したい場合は、[Crowdin](https://crowdin.com/project/KageSU) にアクセスしてください。

## KPM に対応

- KernelPatch に基づき、KSU と重複する機能を削除し、KPM の対応のみを維持しています。
- 進行中: 追加機能を統合することで APatch の互換性を拡張し、さまざまな実装間での互換性を確保します。

**オープンソースリポジトリ**: [https://github.com/ShirkNeko/KageSU_KernelPatch_patch](https://github.com/ShirkNeko/KageSU_KernelPatch_patch)

**KPM テンプレート**: [https://github.com/udochina/KPM-Build-Anywhere](https://github.com/udochina/KPM-Build-Anywhere)

> [!Note]
>
> 1. `CONFIG_KPM=y` が必要です。
> 2. 非 GKI デバイスには `CONFIG_KALLSYMS=y` と `CONFIG_KALLSYMS_ALL=y` も必要です。
> 3. `4.19` より前のカーネルでは、`4.19` から `set_memory.h` をバックポートする必要があります。

## トラブルシューティング

1. マネージャーアプリのアンインストール時にデバイスが停止する場合は、_com.sony.playmemories.mobile_ をアンインストールしてください。

## スポンサー

- [ShirkNeko](https://afdian.com/a/shirkneko) (KageSU のメンテナー)
- [weishu](https://github.com/sponsors/tiann) (KernelSU の作者)

## ライセンス

- 「kernel」のディレクトリ内のファイルは [GPL-2.0-only](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html) のライセンスに基づいています。
- 上記のファイルまたはディレクトリを除き、その他のすべての部分は[GPL-3.0 以降](https://www.gnu.org/licenses/gpl-3.0.html)です。

## クレジット

- [SukiSU Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra): アップストリーム
- [ReSukiSU](https://github.com/ReSukiSU/ReSukiSU): マルチマネージャー対応（信頼するマネージャー証明書リスト）

<details>
<summary>SukiSU Ultra のクレジット</summary>

- [KernelSU](https://github.com/tiann/KernelSU): アップストリーム
- [MKSU](https://github.com/5ec1cff/KernelSU): Magic Mount
- [RKSU](https://github.com/rsuntk/KernelsU): 非 GKI のサポート
- [susfs](https://gitlab.com/simonpunk/susfs4ksu): KernelSU 向けの root 隠蔽カーネルパッチおよびユーザー空間モジュールのアドオン
- [KernelPatch](https://github.com/bmax121/KernelPatch): KernelPatch はカーネルモジュールの APatch 実装の重要な部分です
</details>

<details>
<summary>KernelSU のクレジット</summary>

- [Kernel-Assisted Superuser](https://git.zx2c4.com/kernel-assisted-superuser/about/): KernelSU の概念。
- [Magisk](https://github.com/topjohnwu/Magisk): パワフルな root ツール。
- [genuine](https://github.com/brevent/genuine/): APK v2 署名認証。
- [Diamorphine](https://github.com/m0nad/Diamorphine): いくつかの rootkit のスキル。
</details>
