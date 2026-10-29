## ADDED Requirements

### Requirement: Playback uses system media notification eligible for HyperOS Super Island

While audio is actively playing with a valid current song, the system SHALL publish a foreground media notification that uses `MediaStyle` bound to the active `MediaSession`, so that HyperOS devices supporting media Super Island can display playback on the island.

#### Scenario: Playing on HyperOS with Super Island support

- **WHEN** the user starts or resumes playback of a valid song on a HyperOS device that supports media Super Island
- **THEN** the app presents a `MediaStyle` media notification linked to the current `MediaSession` with title, artist (and album when available), and album art when loaded
- **AND** system media controls for play/pause and skip previous/next remain available via the session

#### Scenario: Only during real playback

- **WHEN** there is no valid current song, or the user has stopped playback and dismissed the playing notification
- **THEN** the app MUST NOT keep a marketing or idle media notification for the purpose of occupying Super Island or the notification shade

### Requirement: Fallback to notification shade when Super Island is unavailable

When Super Island media presentation is unavailable, the app SHALL continue to show the existing playing media notification in the notification shade without breaking playback controls.

#### Scenario: Non-HyperOS or unsupported device

- **WHEN** playback is active on a device that is not HyperOS or does not support media Super Island
- **THEN** the playing notification still appears in the notification shade (or as the standard system media notification)
- **AND** play/pause and skip previous/next actions remain usable

#### Scenario: Detection failure

- **WHEN** HyperOS capability detection fails or is inconclusive
- **THEN** the app MUST use the existing notification implementation selection logic and MUST NOT crash or suppress the playing notification

### Requirement: Prefer MediaStyle over classic RemoteViews on HyperOS

On HyperOS, when a MediaStyle path is available, the app SHALL prefer the MediaStyle playing notification over the classic custom RemoteViews notification so that Super Island eligibility is not blocked by the classic style preference.

#### Scenario: Classic notify preference on HyperOS

- **WHEN** the user has enabled classic notification style and starts playback on HyperOS
- **THEN** the app still uses the MediaStyle playing notification path for that session
- **AND** playback controls remain functional

#### Scenario: Classic notify on non-HyperOS

- **WHEN** the user has enabled classic notification style and starts playback on a non-HyperOS device
- **THEN** the app MAY continue using the classic RemoteViews notification as today

### Requirement: MediaSession playback state stays aligned with notification controls

The app SHALL keep `MediaSession` metadata and `PlaybackState` consistent with the current song and playing/paused state whenever the playing notification is shown, including the standard actions required for system media controls.

#### Scenario: Track change while playing

- **WHEN** the current song changes while the playing notification is visible
- **THEN** both the notification content and `MediaSession` metadata update to the new song
- **AND** `PlaybackState` reflects playing or paused accurately

#### Scenario: Pause and resume

- **WHEN** the user pauses or resumes from the notification, Super Island, or in-app controls
- **THEN** `PlaybackState` and the playing notification icon/state update to match the actual player state
