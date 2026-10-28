library;

import 'playtime.g.dart' as generated;

/// The placement where Studio campaigns will be shown.
///
/// Use one of the predefined placements, or [custom] for a publisher-supplied value.
class StudioPlacement extends generated.StudioPlacement {
  StudioPlacement({required super.value, super.isCustom});

  StudioPlacement._predefined(String value)
    : super(value: value, isCustom: false);

  /// Campaigns placed in the navigation bar.
  static StudioPlacement get navbar => StudioPlacement._predefined('navbar');

  /// Campaigns placed as an icon.
  static StudioPlacement get icon => StudioPlacement._predefined('icon');

  /// Campaigns placed in the earn tab.
  static StudioPlacement get earnTab => StudioPlacement._predefined('earn_tab');

  /// Campaigns placed in the game tab.
  static StudioPlacement get gameTab => StudioPlacement._predefined('game_tab');

  /// Campaigns placed on the home page.
  static StudioPlacement get homePage =>
      StudioPlacement._predefined('home_page');

  /// Campaigns placed in the notification inbox.
  static StudioPlacement get notificationInbox =>
      StudioPlacement._predefined('notification_inbox');

  /// Campaigns placed in a notification.
  static StudioPlacement get notification =>
      StudioPlacement._predefined('notification');

  /// Campaigns placed in a mini game.
  static StudioPlacement get miniGame =>
      StudioPlacement._predefined('mini_game');

  /// Campaigns placed on the order page.
  static StudioPlacement get orderPage =>
      StudioPlacement._predefined('order_page');

  /// Campaigns placed in the redeem section.
  static StudioPlacement get redeemSection =>
      StudioPlacement._predefined('redeem_section');

  /// Campaigns placed in the scan receipt flow.
  static StudioPlacement get scanReceipt =>
      StudioPlacement._predefined('scan_receipt');

  /// Campaigns placed in the step converter.
  static StudioPlacement get stepConverter =>
      StudioPlacement._predefined('step_converter');

  /// A user-supplied custom value.
  static StudioPlacement custom(String value) =>
      StudioPlacement(value: value, isCustom: true);

  /// Every predefined placement, in schema order.
  static List<StudioPlacement> get values => <StudioPlacement>[
    navbar,
    icon,
    earnTab,
    gameTab,
    homePage,
    notificationInbox,
    notification,
    miniGame,
    orderPage,
    redeemSection,
    scanReceipt,
    stepConverter,
  ];
}

/// The placement where the catalog will be shown.
///
/// Use one of the predefined placements, or [custom] for a publisher-supplied value.
class CatalogPlacement extends generated.CatalogPlacement {
  CatalogPlacement({required super.value, super.isCustom});

  CatalogPlacement._predefined(String value)
    : super(value: value, isCustom: false);

  /// Catalog placed in the navigation bar.
  static CatalogPlacement get navbar => CatalogPlacement._predefined('navbar');

  /// Catalog placed as an icon.
  static CatalogPlacement get icon => CatalogPlacement._predefined('icon');

  /// Catalog placed in the earn tab.
  static CatalogPlacement get earnTab =>
      CatalogPlacement._predefined('earn_tab');

  /// Catalog placed in the game tab.
  static CatalogPlacement get gameTab =>
      CatalogPlacement._predefined('game_tab');

  /// Catalog placed on the home page.
  static CatalogPlacement get homePage =>
      CatalogPlacement._predefined('home_page');

  /// Catalog placed in the notification inbox.
  static CatalogPlacement get notificationInbox =>
      CatalogPlacement._predefined('notification_inbox');

  /// Catalog placed in a notification.
  static CatalogPlacement get notification =>
      CatalogPlacement._predefined('notification');

  /// Catalog placed in a mini game.
  static CatalogPlacement get miniGame =>
      CatalogPlacement._predefined('mini_game');

  /// Catalog placed on the order page.
  static CatalogPlacement get orderPage =>
      CatalogPlacement._predefined('order_page');

  /// Catalog placed in the redeem section.
  static CatalogPlacement get redeemSection =>
      CatalogPlacement._predefined('redeem_section');

  /// Catalog placed in the scan receipt flow.
  static CatalogPlacement get scanReceipt =>
      CatalogPlacement._predefined('scan_receipt');

  /// Catalog placed in the step converter.
  static CatalogPlacement get stepConverter =>
      CatalogPlacement._predefined('step_converter');

  /// A user-supplied custom value.
  static CatalogPlacement custom(String value) =>
      CatalogPlacement(value: value, isCustom: true);

  /// Every predefined placement, in schema order.
  static List<CatalogPlacement> get values => <CatalogPlacement>[
    navbar,
    icon,
    earnTab,
    gameTab,
    homePage,
    notificationInbox,
    notification,
    miniGame,
    orderPage,
    redeemSection,
    scanReceipt,
    stepConverter,
  ];
}

/// The display format for campaign rendering.
///
/// Use one of the predefined formats, or [custom] for a publisher-supplied value.
class DisplayFormat extends generated.DisplayFormat {
  DisplayFormat({required super.value, super.isCustom});

  DisplayFormat._predefined(String value)
    : super(value: value, isCustom: false);

  /// Renders campaigns as icons.
  static DisplayFormat get icon => DisplayFormat._predefined('icon');

  /// Renders campaigns as notifications.
  static DisplayFormat get notification =>
      DisplayFormat._predefined('notification');

  /// Renders campaigns in a notification inbox.
  static DisplayFormat get notificationInbox =>
      DisplayFormat._predefined('notification_inbox');

  /// Renders campaigns in a horizontally scrolling carousel.
  static DisplayFormat get carousel => DisplayFormat._predefined('carousel');

  /// Renders campaigns in a custom catalog layout.
  static DisplayFormat get customCatalog =>
      DisplayFormat._predefined('custom_catalog');

  /// Renders campaigns as a banner.
  static DisplayFormat get banner => DisplayFormat._predefined('banner');

  /// Renders campaigns in a popup.
  static DisplayFormat get popup => DisplayFormat._predefined('popup');

  /// Renders campaigns as an interstitial.
  static DisplayFormat get interstitial =>
      DisplayFormat._predefined('interstitial');

  /// A user-supplied custom value.
  static DisplayFormat custom(String value) =>
      DisplayFormat(value: value, isCustom: true);

  /// Every predefined format, in schema order.
  static List<DisplayFormat> get values => <DisplayFormat>[
    icon,
    notification,
    notificationInbox,
    carousel,
    customCatalog,
    banner,
    popup,
    interstitial,
  ];
}

/// The app category for campaign targeting.
class AppCategory extends generated.AppCategory {
  AppCategory({required super.value});

  /// Gaming apps.
  static AppCategory get gaming => AppCategory(value: 'gaming');

  /// Non-gaming apps.
  static AppCategory get nonGaming => AppCategory(value: 'non-gaming');

  /// Both gaming and non-gaming apps.
  static AppCategory get both => AppCategory(value: 'both');

  /// Every valid category, in schema order. This set is exhaustive.
  static List<AppCategory> get values => <AppCategory>[gaming, nonGaming, both];
}
