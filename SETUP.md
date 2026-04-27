# Getting Started — API Keys Setup

Sonic Signature needs a Last.fm key for song search and an OpenRouter key for AI recommendations.

---

## 1. Last.fm API Key (Music Search)

Used to search songs and fetch genre/mood tags. **Free, no credit card.**

1. Go to **[last.fm/api/account/create](https://www.last.fm/api/account/create)**
2. Sign in or create a free Last.fm account
3. Fill in the form:
   - **Application name:** Sonic Signature (or anything you like)
   - **Application description:** Personal IEM recommendation tool
   - **Callback URL:** leave blank
   - **Application homepage:** leave blank
4. Submit → you'll see your **API Key** (a 32-character hex string)
5. Copy it

In the app: **Settings → Music Data → paste your key → Validate & Save**

---

## 2. OpenRouter API Key (Recommendations)

The LLM analyzes your music taste and recommends IEMs. The current app settings and runtime use OpenRouter.

1. Go to **[openrouter.ai](https://openrouter.ai)** and create an account
2. Go to **[openrouter.ai/keys](https://openrouter.ai/keys)** → **Create Key**
3. Copy the key (starts with `sk-or-...`)
4. Add credits if you want paid models, or use a free model.

**Recommended free model ID:** `stepfun/step-3.5-flash:free`

In the app: **Settings → AI Provider → paste key + model ID → Validate & Save**

---

## That's It

Once both keys are saved, go back to the home screen:
- **Song tab** — search for songs you like, add them as chips
- **Artist tab** — type artist names
- **Genre/Mood tab** — describe your taste in plain text

Pick a budget tier and tap **Find My IEM** 🎧
