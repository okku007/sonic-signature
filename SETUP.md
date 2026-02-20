# Getting Started — API Keys Setup

Sonic Signature needs two free API keys to work. Here's how to get them in under 5 minutes.

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

## 2. AI Provider Key (Recommendations)

The LLM analyzes your music taste and recommends IEMs. Pick one:

### Option A — Gemini (Google) · Free tier available

1. Go to **[aistudio.google.com/app/apikey](https://aistudio.google.com/app/apikey)**
2. Click **Create API key**
3. Select a Google Cloud project (or create one — it's free)
4. Copy the key

> **Free tier:** 15 requests/minute, 1,500/day on `gemini-2.0-flash-lite`. More than enough for personal use.

In the app: **Settings → AI Provider → select Gemini → paste key → Validate & Save**

---

### Option B — OpenRouter (Highly Recommended) · Access 200+ models

1. Go to **[openrouter.ai](https://openrouter.ai)** and create an account
2. Go to **[openrouter.ai/keys](https://openrouter.ai/keys)** → **Create Key**
3. Copy the key (starts with `sk-or-...`)
4. Add credits if you want paid models, or use free models (e.g. `google/gemini-2.0-flash-exp:free , stepfun/step-3.5-flash:free`)

**Recommended free model ID:** `stepfun/step-3.5-flash:free`

In the app: **Settings → AI Provider → select OpenRouter → paste key + model ID → Validate & Save**

---

## That's It

Once both keys are saved, go back to the home screen:
- **Song tab** — search for songs you like, add them as chips
- **Artist tab** — type artist names
- **Genre/Mood tab** — describe your taste in plain text

Pick a budget tier and tap **Find My IEM** 🎧
