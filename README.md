# 👻 GhostCoords

**Never lose your stuff again!**  
**GhostCoords** is a lightweight, 100% **client-side** Fabric mod for **Minecraft 26.3** that automatically remembers where you died and displays your exact death coordinates privately in chat—visible **only to you**.

Works seamlessly in **Singleplayer** and on **any Multiplayer server** (Vanilla, Paper, Spigot, Fabric, Realms, LAN) with zero server-side installation required.

---

## ✨ Features

- **Automatic Death Tracking:** Records your exact **X, Y, Z** coordinates, **dimension** (`Overworld`, `The Nether`, `The End`, or custom dimensions), **world/server**, **cause of death**, and **timestamp** the instant you die.
- **100% Private & Client-Side:** Commands run strictly on your client via Fabric's Client Command API. No packets are sent to the server and no other player can see your command or death location.
- **Works Everywhere:** Full support for Singleplayer and Multiplayer, keeping separate death records per world and per server (with a warning if a fallback coordinate comes from another world/server).
- **Fail-Safe Detection + Vanilla Recovery:** Listens to both the death screen (`DeathScreen`) and combat kill packets (`ClientboundPlayerCombatKillPacket`), so even servers with `doImmediateRespawn` enabled are covered. Also recovers vanilla Minecraft's client-side `lastDeathLocation` (Recovery Compass data) if available.
- **Interactive Chat Buttons:**
  - Click coordinates or **`[Copy to Clipboard]`** to copy `X Y Z` directly to your clipboard.
  - Click **`[Teleport /tp]`** to pre-fill a cross-dimensional `/execute in <dimension> run tp @s X Y Z` command in your chat box.
- **Persistent Storage:** Saved atomically to `.minecraft/config/ghostcoords.json` so your coordinates survive restarts and disconnects.
- **Clean Localization:** Uses Minecraft's standard `en_us.json` language system so resource packs and translations can easily extend it.

---

## ⌨️ Commands

All commands are **client-side only** and completely invisible to other players:

| Command | Description |
|---|---|
| `/ghostcoords` *(or `/killinfo`)* | Privately displays your last death location, dimension, cause, and interactive buttons. |
| `/ghostcoords copy` | Copies your last death coordinates (`X Y Z`) directly to your clipboard. |
| `/ghostcoords clear` | Clears your saved death locations. |
| `/ghostcoords help` | Shows the command help menu. |

**Aliases:** `/killinfo`, `/deathcoords`, `/deathinfo`, `/lastdeath` (all support `copy`, `clear`, and `help`).

---

## 📥 Installation

1. Download the mod from **CurseForge** or **Modrinth**.
2. Install **Minecraft 26.3** with **Fabric Loader (`>=0.19.5`)** and **Fabric API (`26.3`)**.
3. Drop `ghostcoords-1.0.0+26.3.jar` into your `.minecraft/mods` folder.
4. Launch the game and play!

---

## 🛠️ Building from Source

Requires **Java 25**.

```bash
./gradlew build
```

The compiled mod jar will be generated in `build/libs/ghostcoords-1.0.0+26.3.jar`.

---

## 📄 License

Licensed under the [MIT License](LICENSE).
