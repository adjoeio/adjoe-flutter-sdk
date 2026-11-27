import 'dart:async';

import 'package:flutter/foundation.dart';

import '../playtime.g.dart' as generated;
import 'playtime_shelf_models.dart';

/// Owns an experimental Shelf session independently of its mounted widget.
///
/// Retain across rebuilds and call [dispose] when finished. Only one widget may
/// mount this controller at a time. Create a new controller to change options.
class PlaytimeShelfController {
  PlaytimeShelfController._(this.id);

  static final _callbacks = _ShelfCallbacks();
  static final _host = generated.ShelfHostApi();
  // Avoid reusing native session IDs when Flutter hot-restarts the Dart isolate.
  static int _nextId = DateTime.now().microsecondsSinceEpoch;
  static int _nextAttachmentId = 0;
  final int id;
  final _changes = StreamController<void>.broadcast(sync: true);
  final _statuses = StreamController<PlaytimeShelfStatus>.broadcast(sync: true);
  final _actions = StreamController<PlaytimeShelfActionContext>.broadcast(
    sync: true,
  );
  PlaytimeShelfStatus _status = const PlaytimeShelfStatus(
    kind: PlaytimeShelfStatusKind.idle,
  );
  int? _attachmentId;
  double _height = 0;
  bool _disposed = false;
  Future<void>? _disposal;

  PlaytimeShelfStatus get status => _status;
  bool get isDisposed => _disposed;
  Stream<PlaytimeShelfStatus> get statuses => _statuses.stream;
  Stream<PlaytimeShelfActionContext> get actions => _actions.stream;

  /// Creates a session. Initialize Playtime first.
  /// Android hosts must use FlutterFragmentActivity.
  static Future<PlaytimeShelfController> create({
    required PlaytimeShelfOptions options,
  }) async {
    if (kIsWeb ||
        (defaultTargetPlatform != TargetPlatform.iOS &&
            defaultTargetPlatform != TargetPlatform.android)) {
      throw UnsupportedError(
        'Experimental Drop-In UI supports Android and iOS only.',
      );
    }
    if (options.placement.trim().isEmpty) {
      throw ArgumentError.value(
        options.placement,
        'placement',
        'Must not be blank',
      );
    }
    final category = options.category.value;
    if (!const ['gaming', 'non-gaming', 'both'].contains(category)) {
      throw ArgumentError.value(category, 'category', 'Unknown app category');
    }
    generated.ShelfFlutterApi.setUp(_callbacks);
    final controller = PlaytimeShelfController._(++_nextId);
    _callbacks.controllers[controller.id] = controller;
    try {
      await _host.create(controller.id, options.toMessage());
      if (controller.isDisposed) {
        throw StateError('The SDK was torn down while creating the Shelf.');
      }
      return controller;
    } catch (_) {
      controller._finishDisposal();
      rethrow;
    }
  }

  Future<void> prefetch() {
    _checkActive();
    return _host.prefetch(id);
  }

  /// Requests a reload. Completion is reported by [statuses], not this Future.
  Future<void> refresh() {
    _checkActive();
    return _host.refresh(id);
  }

  /// Releases native ownership. Safe to call repeatedly, including after teardown.
  Future<void> dispose() {
    if (_disposal != null) return _disposal!;
    if (_disposed) return Future<void>.value();
    _finishDisposal();
    return _disposal = _host.dispose(id);
  }

  void _checkActive() {
    if (_disposed) throw StateError('This Shelf controller has been disposed.');
  }

  void _finishDisposal() {
    if (_disposed) return;
    _disposed = true;
    _height = 0;
    _callbacks.controllers.remove(id);
    _changes.add(null);
    unawaited(_changes.close());
    unawaited(_statuses.close());
    unawaited(_actions.close());
  }

  // Package-internal widget hooks. Attachment IDs reject late size callbacks.
  Stream<void> get changes => _changes.stream;
  double get height => _height;

  int mount() {
    _checkActive();
    if (_attachmentId != null) {
      throw StateError(
        'A Shelf controller can only be mounted once at a time.',
      );
    }
    _height = 0;
    return _attachmentId = ++_nextAttachmentId;
  }

  Future<void> attach(int viewId, int attachmentId) async {
    if (_disposed || _attachmentId != attachmentId) return;
    await _host.attach(id, viewId, attachmentId);
  }

  Future<void> measure(int attachmentId, double width) async {
    if (_disposed || _attachmentId != attachmentId) return;
    await _host.measure(id, attachmentId, width);
  }

  Future<void> unmount(int attachmentId) async {
    if (_attachmentId != attachmentId) return;
    _attachmentId = null;
    _height = 0;
    if (!_disposed) await _host.detach(id, attachmentId);
  }
}

class _ShelfCallbacks implements generated.ShelfFlutterApi {
  final controllers = <int, PlaytimeShelfController>{};

  @override
  void statusChanged(int controllerId, generated.ShelfStatusMessage status) {
    final controller = controllers[controllerId];
    if (controller == null) return;
    controller._status = PlaytimeShelfStatus.fromMessage(status);
    controller._statuses.add(controller._status);
  }

  @override
  void sizeChanged(int controllerId, int attachmentId, double height) {
    final controller = controllers[controllerId];
    if (controller == null ||
        controller._attachmentId != attachmentId ||
        !height.isFinite ||
        height < 0 ||
        controller._height == height)
      return;
    controller._height = height;
    controller._changes.add(null);
  }

  @override
  void actionPerformed(
    int controllerId,
    String? campaignId,
    String actionName,
  ) {
    controllers[controllerId]?._actions.add(
      PlaytimeShelfActionContext(
        campaignId: campaignId,
        actionName: actionName,
      ),
    );
  }

  @override
  void disposed(int controllerId) =>
      controllers[controllerId]?._finishDisposal();
}
