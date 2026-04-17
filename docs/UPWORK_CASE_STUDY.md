# Upwork Case Study

## Suggested Title

Cross-Platform Tower Defense Game Built With Kotlin Multiplatform

## Suggested Cover Image

Gameplay screenshot with HUD, towers, enemies, and a caption about a shared gameplay core with a production Android runtime.

## Challenge

Build a feature-rich tower defense game that could share gameplay systems across mobile platforms without relying on a heavyweight third-party engine.

## Solution

I built a Kotlin Multiplatform architecture with a shared gameplay core and native platform frontends. Android is the primary supported runtime today, using Canvas and SurfaceView, while the iOS side already has a SwiftUI/SpriteKit shell and a documented migration path for full shared-module portability.

## Impact

This project proves cross-platform architecture, maintainable separation between simulation and rendering, and the ability to manage a large gameplay surface while staying honest about delivery status across platforms.

## Why This Matters To Clients

It shows rapid framework adaptation, strong separation between core logic and presentation, and the ability to deliver complex products without forcing a one-size-fits-all toolchain.

## Suggested Upwork Tags

- Kotlin
- Android
- Kotlin Multiplatform
- Mobile App Development
- Game Development
