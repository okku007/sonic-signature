# Sonic Signature 2.0 — UI Enhancement & Differentiation Specification

Source: :contentReference[oaicite:0]{index=0}

---

## Overview

**Sonic Signature** is an AI-powered IEM recommendation app that maps a user's musical taste (songs, artists, genres) to In-Ear Monitors using Last.fm metadata, community tags, and LLM reasoning.  
This specification is a production-ready Markdown file intended to be fed directly into Antigravity IDE to scaffold new UI designs, features, and implementation tasks.

---

# 1. Product Vision

Transform Sonic Signature from an IEM recommender into:

> **A Personal Audiophile Intelligence Engine**  
> — persistent sonic fingerprints, explainable AI recommendations, and price-aware upgrade paths.

---

# 2. Design System

## 2.1 Visual Identity

- Dark-first (OLED optimized)
- Minimal, audiophile-focused layouts
- Glassmorphism surfaces + subtle waveform micro-animations
- Material 3 base with a custom theme overlay

## 2.2 Color Palette

| Role | Hex |
|------|-----|
| Background | #0E0F13 |
| Surface | #16181F |
| Primary Accent | #6C5CE7 |
| Secondary Accent | #00D4FF |
| Highlight | #FFB800 |
| Error | #FF4D4D |

## 2.3 Typography & Motion

- Headings: SemiBold
- Body: Regular
- Numeric/Price: Monospaced emphasis
- Motion: Reduce option for accessibility; subtle transitions and loaders otherwise

---

# 3. Navigation

## Android
- Bottom navigation: Discover | Sonic Profile | Compare | History | Settings

## Desktop
- Persistent left rail with same sections; keyboard shortcuts enabled

---

# 4. Core Screens & Flows

## 4.1 Discover (Primary Flow)

**Purpose:** Main path to build a listening profile and request recommendations.

**Components:**
- Debounced search bar (Song/Artist)
- Selected chips list (removable)
- Budget slider (₹1,000 → ₹100,000)
- Tuning preference toggle (Neutral, V, Warm, etc.)
- Tag cloud preview (aggregated Last.fm tags)
- Animated CTA: **Find My IEM**

**Behavior Enhancements:**
- Real-time sonic profile preview (before LLM call)
- Skeleton loaders while fetching tags/recommendations
- Animated "AI analyzing..." spectral loader

## 4.2 Sonic Profile (NEW)

**Purpose:** Visualize user’s sonic fingerprint over time.

**Components:**
- Radar chart: Bass, Mids, Treble, Warmth, Stage, Detail
- Tag frequency heatmap
- Dominant genres list
- Mood polarity slider (Relaxed ↔ Energetic)
- Shareable "Sonic Signature" card (image export)

**Example Card Text:**

- Warm-Neutral
- Sub-bass Elevated
- Vocal Forward
- Detail Sensitive


## 4.3 Compare (NEW)

**Purpose:** Side-by-side comparison tuned to user profile.

**Features:**
- Add up to 3 IEMs
- Visual tonal graphs (overlay)
- Compatibility score (0–100)
- Budget alignment indicator
- Strengths, trade-offs, and LLM justification highlights
- Auto-highlight best match for the current profile

## 4.4 IEM Detail (Enhanced)

**Sections:**
- Hero image with tonality badge
- Price + budget-fit meter
- Compatibility score with breakdown
- "Why this matches you" (LLM explanation)
- Strengths & Trade-offs
- Best-for / Not-ideal-for genres
- Related upgrades and sidegrades

---

# 5. Differentiating Product Features

## 5.1 Sonic DNA Engine (Persistent Profile)
- Aggregate searches & selections over time
- Maintain weighted tag model and normalized sonic attributes
- Local encrypted storage with optional opt-in cloud sync

## 5.2 Compatibility Scoring
- Multi-factor scoring: tonal match, technical match, budget fit, genre synergy
- Expose a score (0–100) and breakdown for transparency

## 5.3 Budget Intelligence Mode
- Replace static tiers with continuous slider
- Include price-performance and upgrade potential in LLM prompt weighting

## 5.4 Upgrade Path Advisor
- If user owns X model, recommend:
  - Direct upgrade (same signature, better technicalities)
  - Technical upgrade (better staging/detail)
  - Signature shift (move to a different tonality)
  - Sidegrade (better value for money)

## 5.5 Tuning Preference Control
- User-selectable bias: Neutral, Harman, V-Shaped, Warm, Analytical, Basshead
- Used as a weighting parameter for the RecommendationEngine prompt

## 5.6 Explainability Mode
- Toggle to show:
  - Last.fm tags used and weights
  - Generated LLM prompt (collapsible)
  - Raw parsed JSON from LLM
  - Compatibility calculation

---

# 6. Data Models (Additions & Examples)

```kotlin
data class SonicAttributes(
    val bass: Float,
    val mids: Float,
    val treble: Float,
    val warmth: Float,
    val stage: Float,
    val detail: Float
)

data class UserSonicProfile(
    val aggregatedTags: Map<String, Int>,
    val attributes: SonicAttributes,
    val dominantGenres: List<String>,
    val moodVector: MoodVector,
    val lastUpdated: Instant
)

data class IEMComparisonItem(
    val recommendation: IEMRecommendation,
    val compatibilityScore: Float
)
```

# 7. Prompt Engineering (v2)

System instruction: You are a professional audiophile consultant.

User Sonic Profile: Top genres, dominant mood, bass preference, detail sensitivity, tuning bias.

Budget: Range from UI slider.

Task: Return 3 IEMs as STRICT JSON with:

compatibility_score (0–100)

tonal_category (e.g., neutral, v-shaped)

strengths

trade_offs

upgrade_path_recommendation

Notes: Always validate JSON schema on receipt; fallback to smallest valid set if LLM returns noisy output.


# 8. Architecture & Modules (KMP-friendly)
New/updated modules (suggested)
shared/
  profile/          # Sonic DNA aggregation + serialization
  comparison/       # Comparison logic + models
  visualization/    # radar/tag heatmap builders
  analytics/        # local analytics and opt-in telemetry
Libraries & infra

Compose Charts or custom Canvas charts

SQLDelight for history and profiles

Ktor client with caching & retry

Kotlinx.serialization for prompt/result contracts

AES-256-GCM secure storage (platform expect/actual already present)

# 9. UX Micro-Interactions

Animated chip add/remove

Haptic feedback on Android for key actions

Button glow/active states when ready

Audio-spectrum loader during LLM processing

Smooth transitions & subtle desktop hover effects

# 9. UX Micro-Interactions

Animated chip add/remove

Haptic feedback on Android for key actions

Button glow/active states when ready

Audio-spectrum loader during LLM processing

Smooth transitions & subtle desktop hover effects

# 11. Privacy, Security & Storage

Continue storing API keys in platform secure stores (EncryptedSharedPreferences, Keychain, encrypted JVM prefs)

Local profile data stored encrypted; cloud sync must be explicit opt-in

Crowd intelligence must be opt-in + anonymized; do not send personally identifying data

# 12. Roadmap (Phased)

Phase 1 — UI Overhaul

Dark theme & theme system

Budget slider

Sonic profile visualization

Improved loaders & micro-interactions

Phase 2 — Intelligence

Sonic DNA persistence

Compatibility scoring

Compare screen & upgrade advisor

Phase 3 — Differentiation & Monetization

Crowd intelligence & trends

Premium features (unlimited queries, advanced graphs)

Affiliate link integration & analytics

# 13. Monetization Hooks

Affiliate linking support per IEM

Premium tier:

Unlimited / priority recommendations

Advanced tonal analysis & export

Personal upgrade advisor

Exportable sonic profile / shareable cards

# 14. Success Metrics

Recommendation completion rate (click-to-result)

Multi-song usage increase

Compare screen adoption

Repeat sessions per user

Average session duration

# 15. Implementation Checklist (Ready-to-assign tasks)
UI

 Discover: Search, chips, budget slider, CTA

 Sonic Profile: Radar chart & heatmap

 Compare: Add/remove IEMs, overlay graphs

 IEM Detail: New sections + compatibility breakdown

Engine

 ProfileAggregator: persist aggregated tags → attributes

 CompatibilityScorer: implement multi-factor scoring

 PromptBuilder v2: structured prompt with weights and budget

Storage & Infra

 SQLDelight history & profile storage

 Ktor caching + retry

 Secure local vault hooks for profile & keys

Testing & QA

 LLM JSON schema validation tests

 Edge cases when Last.fm key missing (graceful degradation)

 Accessibility checks and a11y audit

# 16. Appendix — Example LLM JSON Response Schema
```json
{
  "recommendations": [
    {
      "name": "Model Name",
      "brand": "Brand",
      "price_in_inr": 12000,
      "compatibility_score": 87,
      "tonal_category": "Warm-Neutral",
      "strengths": ["vocal clarity", "rich sub-bass"],
      "trade_offs": ["narrow soundstage"],
      "why_matches": "Concise natural-language explanation tied to user's top tags/attributes"
    }
  ],
  "profile_summary": {
    "dominant_genres": ["indie rock", "acoustic"],
    "attributes": {
      "bass": 0.7,
      "mids": 0.8,
      "treble": 0.6
    }
  }
}
```

