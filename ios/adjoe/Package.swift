// swift-tools-version: 5.9
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
    name: "adjoe",
    // PlaytimeMonetize requires iOS 14.0; SPM will not resolve a lower minimum.
    platforms: [
        .iOS("14.0")
    ],
    products: [
        .library(name: "adjoe", targets: ["adjoe"])
    ],
    dependencies: [
        // Injected by the Flutter tool at build time.
        .package(name: "FlutterFramework", path: "../FlutterFramework"),
        // Pinned to match the version in adjoe.podspec.
        .package(url: "https://github.com/adjoeio/adjoe-monetize-spm", exact: "4.5.2")
    ],
    targets: [
        .target(
            name: "adjoe",
            dependencies: [
                .product(name: "FlutterFramework", package: "FlutterFramework"),
                .product(name: "PlaytimeMonetize", package: "adjoe-monetize-spm")
            ],
            resources: [
                .copy("PrivacyInfo.xcprivacy")
            ]
        )
    ]
)
