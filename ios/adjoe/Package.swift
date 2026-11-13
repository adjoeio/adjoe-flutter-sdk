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
            url: "https://releases.adjoe.io/files/playtime/ios/monetize/5.1.3/PlaytimeSDK.zip",
            checksum: "c8d28c80db22a28f854ceddfb11bf5b35a1501efc30916b3575d5fbf7ef9ad0d"
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
