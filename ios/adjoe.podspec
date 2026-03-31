Pod::Spec.new do |s|
  s.name         = "adjoe"
  s.version      = "4.3.0"
  s.summary      = "Monetization solution for iOS"
  s.description  = "A native iOS monetization solution for app publishers aiming to boost revenue and engagement through rewarded gaming and app interactions."
  s.license      = { :type => 'Commercial', :text => 'adjoe GmbH' }
  s.homepage     = "https://adjoe.io/solutions/monetize-your-app/playtime/"
  s.platforms    = { :ios => '13.0' }
  s.source       = { :git => "https://github.com/github_account/adjoe-flutter-sdk.git", :tag => "#{s.version}" }
  s.author       = { "adjoe" => "support@adjoe.atlassian.net" }
  s.source_files = 'Classes/**/*'
  s.dependency 'Flutter'
  s.platform = :ios, '13.0'

  # Flutter.framework does not contain a i386 slice.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386' }
  s.swift_version = '5.0'

  s.dependency 'PlaytimeMonetize', '4.3.0'

end
