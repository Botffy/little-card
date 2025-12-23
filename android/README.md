# Little Card Android app

See [the main Readme](../README.md) for an overview of the project.

## Development

To build a development version, you will need a `key.properties` file in this directory, with the YouTube Data API key:

```
YOUTUBE_API_KEY=your_api_key_here
```

Get a YouTube Data API key from the [Google Cloud Console](https://console.cloud.google.com/):

- [enable the YouTube Data API v3 for your project](https://console.cloud.google.com/apis/library/youtube.googleapis.com)
- [create an API key credential](https://console.cloud.google.com/apis/credentials). It's a good practice to restrict the key to use only the YouTube API.

You can build and run the app using Android Studio or Gradle.
