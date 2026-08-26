struct JSONUtil {
    static let encoder = JSONEncoder()
    static let decoder = JSONDecoder()
    
    static func convertCodableToJson(_ codable: Codable) throws -> [String: Any]? {
        let data = try encoder.encode(codable)
        let json = try JSONSerialization.jsonObject(with: data) as? [String: Any]
        return json
    }

    static func convertPlaytimeOptionsCodableToJson(_ codable: Codable) throws -> [String: Any]? {
        let data = try encoder.encode(codable)
        var json = try JSONSerialization.jsonObject(with: data) as? [String: Any]
        
        guard var json else {
            return json
        }
        
        json["wrapper"] = "flutter"
        
        return json
    }
}
