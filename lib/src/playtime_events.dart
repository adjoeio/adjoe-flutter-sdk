library;

import 'dart:async';

import 'playtime.g.dart' as generated;

/// The entry point of the adjoe Playtime SDK, extended with the publisher-facing
/// [sendEvent] callback API.
///
/// Events are delivered from the native SDK over the generated `PlaytimeEvents`
/// event channel. That channel exposes a `Stream`, but publishers never deal with
/// the stream directly: [sendEvent] registers a single callback, starts native
/// delivery, and forwards every [generated.PlaytimeEvent] to the callback until
/// [removeEventListener] is called.
class Playtime extends generated.Playtime {
  Playtime({super.binaryMessenger, super.messageChannelSuffix});

  StreamSubscription<generated.PlaytimeEvent>? _eventSubscription;

  /// Registers [callback] to receive every Playtime SDK event and starts native
  /// delivery.
  ///
  /// Calling it again replaces the previously registered callback. Call
  /// [removeEventListener] to stop receiving events.
  Future<void> sendEvent(
    void Function(generated.PlaytimeEvent event) callback,
  ) async {
    await _eventSubscription?.cancel();
    _eventSubscription = generated.streamEvents().listen(callback);
    await addEventListener();
  }

  /// Stops delivering Playtime SDK events and clears the callback registered via
  /// [sendEvent].
  @override
  Future<void> removeEventListener() async {
    await super.removeEventListener();
    await _eventSubscription?.cancel();
    _eventSubscription = null;
  }
}
