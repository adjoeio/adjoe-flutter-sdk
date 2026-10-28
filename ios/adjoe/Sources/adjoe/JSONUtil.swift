import Foundation

struct JSONUtil {
    static let encoder = JSONEncoder()
    static let decoder = JSONDecoder()

    static func convertCodableToJson(_ codable: Codable) throws -> [String: Any]? {
        let data = try encoder.encode(codable)
        let json = try JSONSerialization.jsonObject(with: data) as? [String: Any]
        return json
    }

    /// Converts the Pigeon `InitOptions` into the JSON the SDK's `InitOptions(JSONObject:)`
    /// expects, injecting the wrapper identifier and fixing up the birthday.
    static func convertInitOptionsCodableToJson(_ codable: Codable) throws -> [String: Any]? {
        guard var json = try convertCodableToJson(codable) else {
            return nil
        }

        json["wrapper"] = "flutter"

        if let userProfile = json["userProfile"] as? [String: Any] {
            json["userProfile"] = normalizedUserProfile(userProfile)
        }

        return json
    }

    /// Returns a copy of the user profile with `birthday` converted from an ISO 8601 string
    /// into a `Date`.
    ///
    /// The SDK models `birthday` as `Foundation.Date?` and its `init(JSONObject:)` casts
    /// without parsing, so forwarding the raw string throws a type mismatch. An unparseable
    /// or empty value is dropped rather than forwarded, so the SDK sees "no birthday"
    /// instead of failing initialization.
    static func normalizedUserProfile(_ userProfile: [String: Any]) -> [String: Any] {
        var profile = userProfile

        if let birthday = profile["birthday"] as? String {
            if let parsed = DateUtil.dateFromISOString(birthday) {
                profile["birthday"] = parsed
            } else {
                profile.removeValue(forKey: "birthday")
            }
        }

        return profile
    }
}
