// swift-tools-version: 5.9

import PackageDescription

let package = Package(
    name: "adjoe",
    platforms: [
        .iOS("14.0"),
    ],
    products: [
        .library(name: "adjoe", targets: ["adjoe"]),
    ],
    dependencies: [
        .package(name: "FlutterFramework", path: "../FlutterFramework"),
    ],
    targets: [
        .binaryTarget(
            name: "PlaytimeMonetize",
            url: "https://releases.adjoe.io/files/playtime/ios/monetize/5.0.0/PlaytimeSDK.zip",
            checksum: "3eb53ecfe827f3ac9702ed4f1685f314206e20ff07aad1a4a2681f22fbe1e870"
        ),
        .target(
            name: "adjoe",
            dependencies: [
                "PlaytimeMonetize",
                .product(name: "FlutterFramework", package: "FlutterFramework"),
            ]
        ),
    ]
)
