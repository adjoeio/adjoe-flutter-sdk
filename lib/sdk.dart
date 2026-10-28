/// Playtime SDK — the single entry point publishers import.
///
/// ```dart
/// import 'package:adjoe/sdk.dart';
///
/// await Playtime.initPlaytime(InitOptions(sdkHash: 'your-sdk-hash'));
/// await Playtime.showCatalog(CatalogOptions(placement: CatalogPlacement.earnTab));
/// ```
library;

import 'package:adjoe/src/playtime.g.dart' as generated;
import 'package:adjoe/src/playtime_events.dart' as events;

/// The generated models and transport types, minus the API client classes, the
/// generated sealed classes and the raw event-channel stream, which are superseded
/// by the ready-to-use instances, the hand-written sealed value types and the
/// `Playtime.sendEvent` callback below.
export 'package:adjoe/src/playtime.g.dart'
    hide
        Playtime,
        PlaytimeStudio,
        StudioPlacement,
        CatalogPlacement,
        DisplayFormat,
        AppCategory,
        streamEvents;

/// The hand-written sealed value types that carry the predefined constants and
/// the `custom()` factories.
export 'package:adjoe/src/sealed_values.dart'
    show StudioPlacement, CatalogPlacement, DisplayFormat, AppCategory;

/// The ready-to-use Playtime SDK instance.
// ignore: non_constant_identifier_names
final Playtime = events.Playtime();

/// The ready-to-use Playtime Studio SDK instance.
// ignore: non_constant_identifier_names
final PlaytimeStudio = generated.PlaytimeStudio();
