# KillInfo - Minecraft Fabric 26.3 Modu

**KillInfo**, Minecraft 26.3 için geliştirilmiş, öldüğünüz yerin koordinatlarını otomatik olarak hafızada tutan ve `/killinfo` komutu ile **yalnızca sizin görebileceğiniz** bir chat mesajı olarak sunan istemci taraflı (client-side) bir Fabric modudur.

---

## 🎯 Özellikler

- **Ölüm Koordinatlarını Hatırlama:** Karakteriniz öldüğünde X, Y, Z koordinatları, bulunulan boyut (Overworld, Nether, End), ölüm nedeni ve zamanı otomatik olarak kaydedilir.
- **Gizli & Sadece Size Özel:** `/killinfo` komutu tamamen istemci tarafında (Fabric Client Command API) çalışır. Yazdığınız komut da, verilen yanıt da sunucuya veya diğer oyunculara gitmez; sadece sizin chat ekranınızda görünür.
- **Multiplayer & Singleplayer Desteği:**
  - Sunucuya herhangi bir mod kurulmasına gerek yoktur.
  - Hypixel, Aternos, yerel ağ (LAN) veya herhangi bir vanilla/Paper/Fabric multiplayer sunucusunda doğrudan çalışır.
  - Singleplayer dünyalarında da eksiksiz çalışır.
  - Farklı dünyaların ve sunucuların ölüm koordinatlarını ayrı ayrı hatırlar.
- **Çift Katmanlı Algılama (Fail-Safe):** Hem ölüm ekranı (`DeathScreen`), hem de sunucu savaş paketi (`ClientboundPlayerCombatKillPacket`) dinlenir. `doImmediateRespawn` açık olsa bile koordinatlar kaçırılmaz.
- **Etkileşimli Chat Arayüzü:**
  - Chatteki koordinat metnine veya `[Panoya Kopyala]` butonuna tıkladığınızda koordinatlar doğrudan sistem panonuza kopyalanır.
- **Kalıcı Hafıza:** Oyunu kapatsanız veya sunucudan ayrılsanız bile koordinatlar `.minecraft/config/killinfo.json` dosyasına kaydedilir ve kaybolmaz.

---

## ⌨️ Komutlar

| Komut | Açıklama |
|---|---|
| `/killinfo` | Son öldüğünüz yerin koordinatlarını, boyutunu ve zamanını chate yazdırır (yalnızca siz görürsünüz). |
| `/killinfo copy` | Son ölüm koordinatlarını doğrudan işletim sistemi panonuza kopyalar. |
| `/killinfo clear` | Kaydedilen ölüm koordinatlarını sıfırlar / temizler. |
| `/killinfo help` | Mod yardım menüsünü görüntüler. |

---

## 📥 Kurulum

1. **Gereksinimler:**
   - Minecraft Java Edition **26.3**
   - **Fabric Loader** (0.19.5 veya üstü)
   - **Fabric API** (Minecraft 26.3 uyumlu sürüm)
2. Hazır derlenmiş `killinfo-1.0.0.jar` dosyasını (projenin `build/libs/` klasöründe yer alır) `.minecraft/mods` klasörünüze kopyalayın.
3. Minecraft'ı Fabric profili ile başlatın!

---

## 🛠️ Projeyi Kaynak Koddan Derleme

Modu kendiniz derlemek isterseniz:

```bash
# Proje dizininde:
./gradlew build
```

Derlenen mod dosyası `build/libs/killinfo-1.0.0.jar` konumunda oluşacaktır.
