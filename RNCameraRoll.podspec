require 'json'
package = JSON.parse(File.read('package.json'))

Pod::Spec.new do |s|
  s.name                = "RNCameraRoll"
  s.version             = package["version"]
  s.summary             = package["description"]
  s.description         = <<-DESC
                            An improved camera roll module for React Native
                          DESC
  s.homepage            = "https://github.com/chrisbianca/react-native-cameraroll"
  s.license             = package['license']
  s.author              = "Chris Bianca"
  s.source              = { :git => "https://github.com/chrisbianca/react-native-cameraroll.git", :tag => "v#{s.version}" }
  s.social_media_url    = 'http://twitter.com/chrisjbianca'
  s.platform            = :ios, "8.0"
  s.preserve_paths      = 'README.md', 'package.json', '*.js'
  s.source_files        = 'ios/RNCameraRoll/*.{h,m}'
  s.dependency          'React'
end
