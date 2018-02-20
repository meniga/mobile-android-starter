![Meniga](https://github.com/meniga/mobile-sdk-ios/raw/master/logo.png)

# mobile-android-starter
A simple starter project that demonstrates authentication and fetching transactions as well as a generic http GET request.

## Getting started
The project currently connects to the internal Meniga UMW server. To make it work for your set up, you need to edit the app's gradle file and edit the API_BASE_URL buildConfigField.

## Installation
To be able to use the Meniga Android SDK, you will need to add the following section to your app's gradle file

```groovy
repositories {
    flatDir {
        dirs 'libs'
    }
    maven { url 'https://maven.fabric.io/public' }
    mavenCentral()
    mavenLocal()
    google()
    maven { url 'https://zendesk.jfrog.io/zendesk/repo' }
    maven {
        url 'http://mobiledev.meniga.com/artifactory/meniga/'
        credentials {
            username 'sdk-user'
            password 'AKCp2V5pNJe2ioDcpZ9KkxLAqh7436kczKk1Xp4a6C3joM4w9N5KbRK7YEG7HppvQmmBn8sRc'
        }
    }
}
```

# Dependency

```groovy
dependencies {
  api('com.meniga:sdk:1.1.32') { changing = true }
}
```

## License
The Meniga Mobile Starter for Android is published under the MIT license.
