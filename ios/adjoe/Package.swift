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
            url: "https://releases.adjoe.io/files/playtime/ios/monetize/5.1.2/PlaytimeSDK.zip",
            checksum: "b63991a21022d57de1565eddceefa729db9a957d5a39346d0e26695f06e89ea8"
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
