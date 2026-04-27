# Constitution: IEM Tonal Mapping Engine
**Version:** 1.0  
**Status:** Ratified  
**Last Updated:** 2026-04-28  
**Authority:** This document supersedes all other documents. No task, plan, or code may violate these principles.

---

## Preamble

This constitution establishes the non-negotiable principles governing the design, development, and evolution of the IEM Tonal Mapping Engine. It exists to protect the integrity of the project as it scales across platforms, contributors, and AI-assisted development cycles. Any AI agent or human contributor working on this project must read and adhere to this document before making changes.

---

## Article I — Privacy & Data Sovereignty

### §1.1 — Local-First by Default
All user data — including preferences, API keys, search history, and recommendation results — must be stored exclusively on the user's device. No user data shall be transmitted to any server operated by this project.

### §1.2 — No Central Key Escrow
API keys (Last.fm, OpenRouter, Gemini, or any future provider) must never be:
- Sent to a project-operated server
- Logged to any logging system (console, file, crash reporter)
- Stored in plaintext on disk
- Included in analytics or telemetry payloads

### §1.3 — Secure Storage is Mandatory
Every platform target must use its native secure storage mechanism. There is no acceptable fallback to plaintext storage, even for development builds. See `plan.md §5` for the approved mechanism per platform.

### §1.4 — No Anonymous Telemetry Without Consent
The application must not collect any usage telemetry, crash reports, or behavioral analytics without explicit, informed, opt-in consent from the user. If telemetry is added in a future version, it must be disabled by default.

---

## Article II — BYOM (Bring Your Own Model) Integrity

### §2.1 — No Hard-Coded LLM Keys
No API key for any LLM provider (Gemini, OpenRouter, or otherwise) may be hard-coded in source code, build scripts, CI/CD configuration, or any version-controlled file. Violations of this rule are grounds for immediate revert.

### §2.2 — No Proxying of LLM Requests
The application must not route LLM API calls through any intermediary server operated by this project. All LLM requests must originate directly from the user's device to the provider's API endpoint.

### §2.3 — Provider Extensibility
The application may ship with one supported LLM provider, but provider selection must stay isolated behind `LLMProvider` and `LLMClientFactory` so future providers can be added without rewriting recommendation logic. The UI must not imply that a provider is supported unless the runtime can actually create and validate that provider.

### §2.4 — Model Transparency
The user must always be able to see which LLM provider and model ID is being used for their recommendations. This information must be visible in the Settings screen.

---

## Article III — Music Metadata API Compliance

### §3.1 — Metadata Only
Music metadata from external providers such as Last.fm must be used solely as input signals for IEM recommendation prompts. It must never be:
- Used to train, fine-tune, or evaluate any machine learning model
- Stored in a database for purposes beyond the immediate session
- Re-distributed or exposed via any API endpoint

Provider terms must be checked before adding or changing a music metadata source. If a provider disallows an AI-assisted use case, that provider must not be used for that flow.

### §3.2 — No Music Credential Sharing
Music API credentials must be treated as user-provided credentials, stored in `SecureVault`, and never bundled into the application binary or distributed via any channel.

### §3.3 — Graceful Degradation
If the music metadata API is unavailable or credentials are not configured, the application must remain functional via the Artist and Genre/Mood input paths. Song search is an enhancement, not a hard dependency.

---

## Article IV — Code Architecture

### §4.1 — Shared Logic First
Business logic must be implemented in `commonMain` by default. Platform-specific code is only permitted when:
- A platform API has no multiplatform equivalent (e.g., Keychain, Android Keystore)
- A platform-specific performance optimization is demonstrably necessary

The target is ≥80% shared code by line count across the `shared` module.

### §4.2 — No Hard-Coded Strings in Logic
All user-facing strings must be externalized for future localization. No string literals may appear in ViewModel, Engine, or API client code.

### §4.3 — Result-Typed Error Handling
All async operations that can fail must return `Result<T>` (defined in `util/Result.kt`). Exceptions must not be allowed to propagate to the UI layer uncaught. Every `Result.Error` must carry a user-friendly message.

### §4.4 — No Blocking the Main Thread
All network calls, file I/O, and LLM inference must be performed on a background coroutine dispatcher (`Dispatchers.IO` or `Dispatchers.Default`). UI state updates must be dispatched to `Dispatchers.Main`.

### §4.5 — Dependency Injection Over Singletons
Clients (`MusicClient`, `LLMProvider`, `SecureVault`) must be injected into ViewModels and Engines, not accessed as global singletons. This ensures testability and platform flexibility.

---

## Article V — Security Practices

### §5.1 — No Secrets in Version Control
The following must never be committed to the repository:
- API keys of any kind
- `.env` files containing credentials
- Keystore files or signing certificates
- Any file matching `*.key`, `*.pem`, `*.p12`, `*.jks`

A `.gitignore` rule enforcing this must be present and must not be removed.

### §5.2 — Input Sanitization
All user-provided text (search queries, genre descriptions, API keys) must be sanitized before being included in API requests or LLM prompts. Prompt injection attempts must be mitigated by wrapping user input in clearly delimited sections within the prompt.

### §5.3 — Minimum Permission Principle
The application must request only the permissions required for its stated functionality. On Android, no permission beyond `INTERNET` is required for core functionality. No `READ_CONTACTS`, `ACCESS_FINE_LOCATION`, or similar permissions may be requested.

---

## Article VI — Quality Standards

### §6.1 — Test Coverage for Shared Logic
All classes in `commonMain` that contain business logic must have corresponding unit tests. The minimum acceptable coverage for `engine/` and `api/` packages is 80%.

### §6.2 — No Untested LLM Prompt Changes
Any modification to prompt templates in `PromptBuilder.kt` must be accompanied by a manual verification run (T-112 or T-113 in `tasks.md`) before merging.

### §6.3 — Accessibility is Non-Negotiable
All interactive UI elements must have semantic labels, content descriptions, and sufficient color contrast (WCAG 2.1 AA minimum). This applies to all three platform targets.

### §6.4 — Graceful Error States
Every screen that performs a network or LLM operation must handle and display all error states defined in `plan.md §7`. A blank screen or unhandled exception is never an acceptable error state.

---

## Article VII — Amendments

This constitution may be amended only by explicit, deliberate decision — not by task implementation or AI agent action. Any proposed amendment must:
1. Be documented as a pull request modifying this file
2. Reference the specific article and section being changed
3. Provide a rationale for the change
4. Be reviewed by the project owner before merging

AI coding agents must not modify this file unless explicitly instructed to do so by the project owner.
