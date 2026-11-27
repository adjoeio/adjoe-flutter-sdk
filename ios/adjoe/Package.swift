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
    targets: [
        .binaryTarget(
            name: "PlaytimeMonetize",
            url: "https://releases.adjoe.io/files/playtime/ios/monetize/5.1.5/PlaytimeSDK.zip",
            checksum: "8d32a08be9f5ffd3571626da2b6cf01db800eb4383296d4c27a842662ff4a8c9"
        ),
        .target(
            name: "adjoe",
            dependencies: [
                "PlaytimeMonetize",
            ]
        ),
    ]
)
