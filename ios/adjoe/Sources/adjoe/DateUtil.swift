import Foundation

/// Parses the ISO 8601 date strings that cross the Pigeon bridge.
///
/// There is no date type on the bridge, so `PlaytimeUserProfile.birthday` is sent as a
/// string while the SDK models it as `Foundation.Date?`. The SDK's `init(JSONObject:)`
/// casts to `Date` without parsing and throws a type mismatch on a string, so the
/// conversion has to happen here, in the wrapper.
enum DateUtil {
    /// The formatters are created once and reused: `ISO8601DateFormatter` is comparatively
    /// expensive to construct, and these are hit on every init call.
    private static let withFractionalSeconds: ISO8601DateFormatter = {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        return formatter
    }()

    private static let withoutFractionalSeconds: ISO8601DateFormatter = {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime]
        return formatter
    }()

    private static let dateOnly: ISO8601DateFormatter = {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withFullDate]
        return formatter
    }()

    /// Parses an ISO 8601 string into a `Date`, or returns nil when it cannot be parsed.
    ///
    /// Accepts, in order: the fractional-seconds form (`2002-12-02T00:00:00.000Z`, which is
    /// what `DateTime.toIso8601String()` produces), the plain form (`2002-12-02T00:00:00Z`),
    /// an explicit UTC offset (`2002-12-02T00:00:00.000+01:00`), and a bare calendar date
    /// (`2002-12-02`), which is the natural way to express a birthday.
    ///
    /// Returns nil rather than throwing, so a malformed value degrades to "no date" instead
    /// of failing the whole call.
    static func dateFromISOString(_ value: String?) -> Date? {
        guard let value, !value.isEmpty else {
            return nil
        }

        for formatter in [withFractionalSeconds, withoutFractionalSeconds, dateOnly] {
            if let date = formatter.date(from: value) {
                return date
            }
        }

        return nil
    }
}
