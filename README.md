`LinkoraLocalizationServer` powers [Linkora](https://github.com/sakethpathike/Linkora)'s remote strings, which can be
updated without requiring an app update.

---

# How to Contribute Translations

All translations are now handled directly in this repo as JSON files.

## Requirements

- A GitHub account.

## Step 1: Fork the Repository

Click the **Fork** button at the top-right of the repo page. That's your own copy to work on.

## Step 2: Get Your Language Code

You'll need the 3-letter ISO 639-2 code for your language.

1. Go to this page: [Wikipedia – ISO 639 Codes](https://en.wikipedia.org/wiki/List_of_ISO_639_language_codes)
2. Use the code from the `Set 3` column (e.g., `eng`, `fra`, `spa`)
3. We'll call this `YOUR_LANGUAGE_CODE` from now on.

## Step 3: Use the Web Translation Editor

### 1. Check if the file already exists

Try this URL (replace with your language code):  
`https://linkoralocalizationserver.onrender.com/contribute?lang=YOUR_LANGUAGE_CODE`

- If you see a 404 or similar error, the file doesn't exist yet.
- If the editor loads, the file already exists.

### 2. Open the editor

- If the file **exists**:  
  Use -> `https://linkoralocalizationserver.onrender.com/contribute?lang=YOUR_LANGUAGE_CODE`
- If the file **does not exist**:  
  Use -> `https://linkoralocalizationserver.onrender.com/contribute?newLang=true`

Using `?lang=...` for a non-existent file won't load the editor.

### 3. Translate

In the editor you'll see:

- `key`: the internal ID
- `value`: the English source string
- a text box: where you enter your translation

If you're editing an existing language, the box will be pre-filled. If it's a new one, it'll be empty.

All `>{0}`, `>{1}`, etc. placeholders represent dynamic values the app needs at runtime. The number inside the braces
tells the app which value belongs at that position.

| Case     | Source                           | Injected values               | Translation                    |
|----------|----------------------------------|-------------------------------|--------------------------------|
| Single   | `Deleted the >{0} strings pack.` | `>{0} = "हिंदी"`              | `删除了 >{0} 字符串包。`               |
| Multiple | `>{0} of >{1} links refreshed.`  | `>{0} = "94"`, `>{1} = "100"` | `>{1} 件のリンクのうち >{0} 件を更新しました。` |

Placement depends on your target language. As long as the indices are preserved, reordering is safe.

Keep the `>{N}` syntax exactly as written. Do not rename, renumber, or remove indices, and do not introduce new ones.
Anything the app can't match to an injected value is shown as literal text.

### 4. Copy the JSON

Once done, click the `Copy JSON` button to copy your work to clipboard.

## Step 4: Add or Update the File in Your Fork

In your fork, go to:  
`src/main/resources/raw/`

### File Format

All JSON files need to start with the `localizedName` field. This represents the actual name of the language in the app.

The structure should be:

```
localizedName : [Native language name]
---
[Your copied JSON from the translation editor]
```

The `localizedName` value should be in the native script of your language (like `हिन्दी` for Hindi, `Français` for
French). There are no double quotes around this value.

This isn't a typical JSON file, but the server handles this format just fine.

### For a new language

- Create a new file named `YOUR_LANGUAGE_CODE.json`
- Add the `localizedName` field with your language name in its native script
- Add the `---` line
- Paste the JSON you copied

### For an existing language

- Open `YOUR_LANGUAGE_CODE.json`
- Make sure it starts with `localizedName : [Native Language Name]`
- Make sure there's a `---` separator line
- Replace the JSON content below the separator with your updated JSON

### Example:

```
localizedName : Italiano
---
{
  "Settings": "Impostazioni",
  "Theme": "Tema",
  "General": "Generale",
  "Advanced": "Avanzato",
  "Layout": "Impaginazione",
  "Language": "Lingua",
  // ... rest of your translations from the editor
}
```

## Step 5: Create a Pull Request

1. Commit your changes with a clear message, like  
   `feat: add French (fra) translation`  
   or  
   `fix: update Spanish (spa) translations`
2. Push to your fork
3. On GitHub, you'll see a prompt to open a Pull Request
4. Click **Compare & pull request**, fill in the title/description
5. Submit the PR

Thank you to all the translators for taking the time to add valuable translations (yk I really appreciate it 😺🤝).