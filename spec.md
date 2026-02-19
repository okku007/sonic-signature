# Spec: IEM Tonal Mapping Engine
**Version:** 1.0  
**Status:** Draft  
**Last Updated:** 2026-02-19

---

## 1. Overview

### Problem Statement
The IEM (In-Ear Monitor) market has exploded in diversity. A listener who knows they love a particular song or genre has no reliable, personalized way to determine which IEM will match their taste. Generic reviews and marketing copy fail to bridge the gap between musical preference and acoustic hardware.

### Solution
A cross-platform application that:
1. Accepts a song or genre as input
2. Retrieves granular audio feature data from the Spotify Web API
3. Passes that data to a user-sovereign LLM (BYOM) with a structured audiophile prompt
4. Returns a ranked list of IEM recommendations with technical justifications, grounded in Crinacle's ranking database

### Target Platforms
- Android
- iOS
- Desktop (Windows, macOS, Linux)

---

## 2. User Stories

### US-01 — Song-Based Recommendation
> As a music listener, I want to search for a song I love and receive IEM recommendations that match its sonic character, so that I can make a confident hardware purchase.

**Acceptance Criteria:**
- [ ] User can type a partial song name and see autocomplete suggestions (top 5–10 results)
- [ ] Selecting a song fetches its audio features (BPM, energy, valence, acousticness, speechiness)
- [ ] The app sends a structured prompt to the configured LLM
- [ ] The LLM response includes ≥3 IEM recommendations with name, price, driver type, and justification
- [ ] Results are displayed within a reasonable time (≤10s for LLM response)

### US-02 — Budget Filtering
> As a user, I want to set a maximum budget before receiving recommendations, so that I only see IEMs I can actually afford.

**Acceptance Criteria:**
- [ ] User can select a budget tier: Ultra-Budget (<$25), Entry ($25–$80), Mid-Range ($100–$500), High-End (>$1,000)
- [ ] The LLM prompt is dynamically injected with the selected budget constraint
- [ ] Recommendations respect the budget tier

### US-03 — BYOM API Key Management
> As a privacy-conscious user, I want to provide my own LLM API key (Gemini or OpenRouter), so that my queries are never routed through a third-party server.

**Acceptance Criteria:**
- [ ] User can enter and save an API key for Gemini or OpenRouter
- [ ] The key is stored in the platform's native secure storage (Keychain, EncryptedSharedPreferences, etc.)
- [ ] The key is never logged, transmitted to a central server, or stored in plaintext
- [ ] User can update or delete their stored key at any time

### US-04 — Genre-Based Recommendation
> As a user who doesn't have a specific song in mind, I want to describe my preferred genre or mood, so that I can still receive relevant IEM recommendations.

**Acceptance Criteria:**
- [ ] User can type a genre or mood (e.g., "dark ambient", "fast metal")
- [ ] The app constructs a prompt using genre characteristics without requiring Spotify audio features
- [ ] LLM returns ≥3 recommendations with justification

### US-05 — Recommendation Detail View
> As a user, I want to tap on a recommended IEM to see a detailed breakdown of why it was suggested, so that I can understand the reasoning before purchasing.

**Acceptance Criteria:**
- [ ] Each recommendation card is tappable/clickable
- [ ] Detail view shows: IEM name, driver type, sound signature, price range, Crinacle grade (if available), and full LLM justification
- [ ] A link or reference to Crinacle's ranking page is provided

---

## 3. User Flows

### Flow A — Song Search → Recommendation
```
[Home Screen]
    → User types song name
    → Autocomplete dropdown appears (Spotify /search)
    → User selects a track
    → App fetches audio features (/audio-features/{id})
    → Budget selection prompt appears (or uses saved preference)
    → App constructs LLM prompt with metadata + budget
    → Loading indicator shown
    → Results screen: ranked IEM cards
    → User taps card → Detail view
```

### Flow B — First-Time Setup (API Key)
```
[Settings / Onboarding]
    → User selects LLM provider (Gemini / OpenRouter)
    → User enters API key
    → App validates key with a test request
    → Key saved to secure vault
    → User returned to Home Screen
```

### Flow C — Genre/Mood Input
```
[Home Screen]
    → User switches to "Genre/Mood" tab
    → User types genre or mood description
    → Budget selection
    → App constructs genre-based LLM prompt
    → Results screen: ranked IEM cards
```

---

## 4. Non-Functional Requirements

| Requirement | Constraint |
|---|---|
| **Privacy** | No user data or API keys transmitted to app servers |
| **Offline resilience** | App must gracefully handle Spotify/LLM API failures with clear error messages |
| **Security** | API keys stored exclusively in platform-native secure storage |
| **Performance** | Autocomplete results within 500ms; LLM response within 10s |
| **Accessibility** | Minimum WCAG 2.1 AA compliance on all UI targets |
| **Portability** | Shared business logic ≥80% via KMP commonMain |

---

## 5. Out of Scope (v1.0)

- Playlist-level analysis (multi-song holistic signature) — deferred to v2.0 with Koog agentic workflows
- Real-time IEM price scraping
- User accounts or cloud sync
- Audio playback within the app
- Training or fine-tuning any ML model on Spotify data (prohibited by Spotify ToS)

---

## 6. Constraints & Compliance

- **Spotify ToS:** Spotify data may only be used as a metadata source. It must not be used to train ML/AI models.
- **BYOM Principle:** The app must never hard-code or proxy LLM API keys. All inference is user-sovereign.
- **Local-First:** All sensitive data (API keys, preferences) must be stored locally on-device.
