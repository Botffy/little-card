# yt2ig

## Problem statement

Sometimes you want to share a YouTube video with their Instagram followers: a song stuck in your head, an insightful speech, a news clip, or even your own freshly uploaded video.

It's _technically_ possible to do this: you paste the YouTube link into a Story, and your followers can tap it.
However, that's just a text link.
You're left to manually create a preview image, write in the video title, and you also have to manually attribute the uploader.

There is no frictionless way to turn a YouTube link into a good-looking Instagram Story, and that's what we're set out to create.

## User needs

- have a reasonably meaningful preview of the YouTube video in my Instagram story
- complete with an image, the video title, and uploader attribution
- plus a clickable link to the video
- and I should be able to create all this in at most three clicks/taps

## Solution idea

YouTube offers a fairly limited set of public API that nonetheless lets us get basic information about a video.
A private API does exist, but using that risks an instant ban for your Google account.

Instagram is also making things a bit difficult: you can send an image to a story, that's fine, but you can't actually add a link sticker, the user has to do that manually.
Oh and, it only works from mobile.

What we _can_ do is to create a small Android application with the following workflow:

1. you share the video with the application
2. the app uses your credentials to fetch video title, uploader, cover image from the API, and collates these into a single image.
3. you can see the preview of the image, and can hit "make it a story", which sends it to the IG story endpoint. You are also given a URL that you can copy and paste in the story as a link.

## Never-goals

- Playing or embedding the video inside Instagram: this is against YouTube ToS and kinda impossible.
- Downloading or rehosting the video content: against YouTube ToS.
- Automating the creation of the Instagram link sticker: not supported by Instagram.

## MVP scope

- Platform: Android.
- Input: YouTube video URLs shared via the system Share menu.
- Data source: YouTube public API
- Output:
  - A single, fixed layout "card" image containing:
    - thumbnail
    - title
    - uploader attribution
    - small YouTube indicator (logo/text)
  - The original YouTube URL copied to the clipboard.
- Actions:
  - "Share to Instagram Story" -> opens the IG Story composer with the generated image as background.
  - "Copy link".
- No login, no backend, no analytics, no persistent user data.

## Design

### Entry points

- User shares a YouTube link to the application
- User opens the application and pastes a YouTube link the textbox offered

### User flow

- App receives URL
  - Parses the URL, extracts video ID.
  - Calls the YouTube API → fetches title, uploader, thumbnail
- App generates preview card
  - Title, uploader, thumbnail composed into a single image
  - Prepares canonical YouTube link
- Preview Screen
  - Shows generated image
  - Explains manual linking
  - Actions:
    - "Share to Instagram Story" (with link auto-copied)
    - "Copy link" (in case auto-copying having issues, and for control)
- "Share to Instagram Story"
  - App sends Android intent to Instagram with the generated image
- Instagram opens Story composer with the generated image
  - User creates the link sticker
  - User posts the story

### Error states

- Invalid URL / not a YouTube link: legal error state
  - URL does not match expected YouTube patterns
  - Behaviour:
    - Show the error message: "This doesn’t look like a YouTube video link."
    - Redirect to start screen

- Failure to parse video ID: illegal state
  - Behaviour:
    - Show the error message: "Couldn’t read this YouTube link."
    - Action: give up

- Network / API failure: legal error state
  - Detection:
    - HTTP error, timeout, or no connectivity.
  - Behaviour:
    - Show error: "Couldn’t load video details (network issue?)."
    - Actions: retry or cancel

- YouTube API quota exceeded / API error: legal error state
  - Detection:
    - Valid network, API returns error.
  - Behaviour:
    - Show error: "Couldn’t load video info."
    - Log error.
    - Action: maybe retry?

- Instagram is not installed or does not handle Story intent
  - Detection:
    - Stories Intent fails / no handler.
  - Behaviour:
    - Show error: "Instagram Stories not available on this device."
    - Stay on the page, keep the image preview visible (user can still save/share elsewhere).

- Clipboard failure
  - Detection:
    - Clipboard write throws an error.
  - Behaviour:
    - Still proceed to Instagram.
    - Show a warning

## Success criteria (MVP)

- From YouTube "Share" to Instagram Story composer in **≤ 3 taps**:
  - Share → select app → "Make it a Story".

- For a valid YouTube URL:
  - Correct video title is shown on the card.
  - Correct uploader is shown on the card.
  - Thumbnail matches the YouTube video.
  - A YouTube indicator (logo/text) is visible on the card.
  - The original video URL is correctly copied to the clipboard.

- Instagram flow:
  - Tapping "Make it a Story" opens the Instagram Story composer with:
    - the generated image as the background.
  - User can manually add a Link sticker and paste the URL from the clipboard.

- Robustness:
  - Non-YouTube links show a clear error and do not crash the app.
  - Network/API errors are surfaced with a friendly message and a Retry option.
  - If Instagram is not available, the app fails gracefully and keeps the image accessible.

- Privacy / complexity:
  - No login required.
  - No backend required.
  - No persistent user data stored.
