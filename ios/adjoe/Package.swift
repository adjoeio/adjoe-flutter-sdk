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
            url: "https://releases.adjoe.io/files/playtime/ios/monetize/5.1.0/PlaytimeSDK.zip",
            checksum: "0d0f893a0fbd5bae0e9838112c2b1d7d8afec5f1c079edf72eaf18df9bb981d7"
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
