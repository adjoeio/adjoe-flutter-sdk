import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

import 'playtime_shelf_controller.dart';
import 'playtime_shelf_models.dart';

/// Embeds the native experimental Shelf at its content-driven height.
///
/// Provide bounded width, normally inside the page's vertical scroll view.
/// Removing this widget does not dispose its independently owned controller.
class PlaytimeShelf extends StatefulWidget {
  const PlaytimeShelf({
    super.key,
    required this.controller,
    this.onStatusChanged,
    this.onAction,
    this.onError,
  });

  final PlaytimeShelfController controller;
  final ValueChanged<PlaytimeShelfStatus>? onStatusChanged;
  final ValueChanged<PlaytimeShelfActionContext>? onAction;
  final ValueChanged<Object>? onError;

  @override
  State<PlaytimeShelf> createState() => _PlaytimeShelfState();
}

class _PlaytimeShelfState extends State<PlaytimeShelf> {
  late int _attachmentId;
  StreamSubscription<void>? _changes;
  StreamSubscription<PlaytimeShelfStatus>? _statuses;
  StreamSubscription<PlaytimeShelfActionContext>? _actions;
  bool _attached = false;
  bool _active = false;
  int? _viewId;
  double? _width;

  @override
  void initState() {
    super.initState();
    _mount();
  }

  void _mount() {
    _attachmentId = widget.controller.mount();
    _active = true;
    _changes = widget.controller.changes.listen((_) {
      if (mounted) setState(() {});
    });
    _statuses = widget.controller.statuses.listen((status) {
      widget.onStatusChanged?.call(status);
    });
    _actions = widget.controller.actions.listen((action) {
      widget.onAction?.call(action);
    });
    final controller = widget.controller;
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted && _active && widget.controller == controller) {
        widget.onStatusChanged?.call(controller.status);
      }
    });
  }

  void _unmount(PlaytimeShelfController controller) {
    if (!_active) return;
    _active = false;
    unawaited(_changes?.cancel());
    unawaited(_statuses?.cancel());
    unawaited(_actions?.cancel());
    _run(controller.unmount(_attachmentId));
    _attached = false;
    _width = null;
  }

  @override
  void deactivate() {
    _unmount(widget.controller);
    super.deactivate();
  }

  @override
  void activate() {
    super.activate();
    _mount();
    final viewId = _viewId;
    if (viewId != null) {
      _run(_created(widget.controller, _attachmentId, viewId));
    }
  }

  @override
  void didUpdateWidget(PlaytimeShelf oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.controller != widget.controller) {
      _unmount(oldWidget.controller);
      _viewId = null;
      _mount();
    }
  }

  @override
  void dispose() {
    _unmount(widget.controller);
    super.dispose();
  }

  void _run(Future<void> operation) {
    unawaited(
      operation.catchError((Object error, StackTrace stack) {
        if (mounted && widget.onError != null) {
          widget.onError!(error);
        } else {
          FlutterError.reportError(
            FlutterErrorDetails(
              exception: error,
              stack: stack,
              library: 'adjoe Drop-In UI',
            ),
          );
        }
      }),
    );
  }

  Future<void> _created(
    PlaytimeShelfController controller,
    int attachment,
    int viewId,
  ) async {
    if (!mounted ||
        !_active ||
        widget.controller != controller ||
        attachment != _attachmentId)
      return;
    _viewId = viewId;
    await controller.attach(viewId, attachment);
    if (!mounted ||
        !_active ||
        widget.controller != controller ||
        attachment != _attachmentId ||
        controller.isDisposed)
      return;
    _attached = true;
    final width = _width;
    if (width != null) await controller.measure(attachment, width);
  }

  @override
  Widget build(BuildContext context) {
    if (kIsWeb ||
        (defaultTargetPlatform != TargetPlatform.iOS &&
            defaultTargetPlatform != TargetPlatform.android)) {
      throw UnsupportedError(
        'Experimental Drop-In UI supports Android and iOS only.',
      );
    }
    if (widget.controller.isDisposed) return const SizedBox.shrink();
    return LayoutBuilder(
      builder: (context, constraints) {
        final width = constraints.maxWidth;
        if (!width.isFinite || width <= 0) {
          throw FlutterError(
            'PlaytimeShelf requires a finite, positive width.',
          );
        }
        if (_width != width) {
          _width = width;
          final controller = widget.controller;
          final attachment = _attachmentId;
          WidgetsBinding.instance.addPostFrameCallback((_) {
            if (mounted &&
                _active &&
                _attached &&
                widget.controller == controller &&
                _attachmentId == attachment) {
              _run(controller.measure(attachment, width));
            }
          });
        }
        return SizedBox(
          width: width,
          height:
              defaultTargetPlatform == TargetPlatform.android &&
                  widget.controller.height == 0
              ? 1
              : widget.controller.height,
          child: _platformView(),
        );
      },
    );
  }

  Widget _platformView() {
    final gestures = <Factory<OneSequenceGestureRecognizer>>{
      Factory<HorizontalDragGestureRecognizer>(
        () => HorizontalDragGestureRecognizer()
          ..gestureSettings = defaultTargetPlatform == TargetPlatform.android
              ? const DeviceGestureSettings(touchSlop: 8)
              : null,
      ),
    };
    if (defaultTargetPlatform == TargetPlatform.android) {
      return PlatformViewLink(
        key: ValueKey(widget.controller.id),
        viewType: 'io.adjoe.playtime/shelf',
        surfaceFactory: (context, controller) => AndroidViewSurface(
          controller: controller as AndroidViewController,
          gestureRecognizers: gestures,
          hitTestBehavior: PlatformViewHitTestBehavior.opaque,
        ),
        onCreatePlatformView: (params) {
          final controller = PlatformViewsService.initSurfaceAndroidView(
            id: params.id,
            viewType: params.viewType,
            layoutDirection: Directionality.of(context),
            creationParams: widget.controller.id,
            creationParamsCodec: const StandardMessageCodec(),
            onFocus: () => params.onFocusChanged(true),
          );
          controller.addOnPlatformViewCreatedListener(
            params.onPlatformViewCreated,
          );
          controller.addOnPlatformViewCreatedListener(_creationCallback());
          unawaited(controller.create());
          return controller;
        },
      );
    }
    return UiKitView(
      key: ValueKey(widget.controller.id),
      viewType: 'io.adjoe.playtime/shelf',
      creationParams: widget.controller.id,
      creationParamsCodec: const StandardMessageCodec(),
      gestureRecognizers: gestures,
      onPlatformViewCreated: _creationCallback(),
    );
  }

  PlatformViewCreatedCallback _creationCallback() {
    final controller = widget.controller;
    return (id) {
      if (!mounted || widget.controller != controller) return;
      _viewId = id;
      if (_active) _run(_created(controller, _attachmentId, id));
    };
  }
}
