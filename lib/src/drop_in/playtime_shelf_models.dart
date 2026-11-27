import '../playtime.g.dart' as generated;
import '../sealed_values.dart';

/// Immutable options for an experimental Shelf session.
class PlaytimeShelfOptions {
  PlaytimeShelfOptions({
    required this.placement,
    this.requestedVariant,
    AppCategory? category,
  }) : _categoryValue = (category ?? AppCategory.gaming).value;

  final String placement;
  final String? requestedVariant;
  final String _categoryValue;

  AppCategory get category => AppCategory(value: _categoryValue);

  generated.ShelfOptionsMessage toMessage() => generated.ShelfOptionsMessage(
    placement: placement,
    requestedVariant: requestedVariant,
    category: _categoryValue,
  );
}

enum PlaytimeShelfStatusKind { idle, loading, ready, empty, unavailable }

enum PlaytimeShelfEmptyReason { noCampaigns, allCampaignsSkipped, unknown }

enum PlaytimeShelfUnavailableReason {
  notConfigured,
  unsupportedSdkVersion,
  network,
  invalidConfiguration,
  sdkNotInitialized,
  tosNotAccepted,
  unknown,
}

/// Current native Shelf state. Counts are present only for ready content.
class PlaytimeShelfStatus {
  const PlaytimeShelfStatus({
    required this.kind,
    this.availableCampaigns,
    this.installedCampaigns,
    this.emptyReason,
    this.unavailableReason,
    this.retryable,
    this.message,
  });

  final PlaytimeShelfStatusKind kind;
  final int? availableCampaigns;
  final int? installedCampaigns;
  final PlaytimeShelfEmptyReason? emptyReason;
  final PlaytimeShelfUnavailableReason? unavailableReason;
  final bool? retryable;
  final String? message;

  factory PlaytimeShelfStatus.fromMessage(generated.ShelfStatusMessage value) {
    final kind = PlaytimeShelfStatusKind.values.byName(value.kind.name);
    return PlaytimeShelfStatus(
      kind: kind,
      availableCampaigns: value.availableCampaigns,
      installedCampaigns: value.installedCampaigns,
      emptyReason: kind == PlaytimeShelfStatusKind.empty
          ? PlaytimeShelfEmptyReason.values.firstWhere(
              (reason) => reason.name == value.reason,
              orElse: () => PlaytimeShelfEmptyReason.unknown,
            )
          : null,
      unavailableReason: kind == PlaytimeShelfStatusKind.unavailable
          ? PlaytimeShelfUnavailableReason.values.firstWhere(
              (reason) => reason.name == value.reason,
              orElse: () => PlaytimeShelfUnavailableReason.unknown,
            )
          : null,
      retryable: value.retryable,
      message: value.message,
    );
  }
}

/// An action already handled by the SDK, not an opportunity to override it.
class PlaytimeShelfActionContext {
  const PlaytimeShelfActionContext({
    required this.campaignId,
    required this.actionName,
  });

  final String? campaignId;
  final String actionName;
}
