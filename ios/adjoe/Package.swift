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
            url: "https://releases.adjoe.io/files/playtime/ios/monetize/5.1.1/PlaytimeSDK.zip",
            checksum: "1b1a19521e9c7bbd201477d425a312b0cbca058c2691099b096bcec9665b50f0"
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
