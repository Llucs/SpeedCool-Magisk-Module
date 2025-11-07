<p align="center">
  <img src="https://raw.githubusercontent.com/Llucs/SpeedCool-Magisk-Module/main/banner.png" alt="SpeedCool Banner" width="400"/>
</p>

<p align="center">
  <!-- Badges -->
  <img src="https://img.shields.io/badge/Magisk-Compatible-brightgreen?style=for-the-badge&logo=magisk&logoColor=white" alt="Magisk">
  <img src="https://img.shields.io/badge/Termux-Compatible-blue?style=for-the-badge&logo=termux&logoColor=white" alt="Termux">
  <img src="https://img.shields.io/github/stars/Llucs/SpeedCool-Magisk-Module?style=for-the-badge" alt="GitHub Stars">
  <img src="https://img.shields.io/github/downloads/Llucs/SpeedCool-Magisk-Module/total?style=for-the-badge" alt="Downloads">
  <img src="https://img.shields.io/badge/Android-6%20to%2016-green?style=for-the-badge&logo=android&logoColor=white" alt="Android">
</p>

# 🔥 SpeedCool: Unleash Your Android's Full Potential

> **SpeedCool** is the ultimate Magisk module engineered to transform your Android device. Bid farewell to lag, overheating, and sluggishness. With intelligent optimizations, your phone will run faster, smoother, and maintain optimal temperatures for any task, from intense gaming to daily use.

---

## 🌐 Other Languages

[Portuguese](https://github.com/Llucs/SpeedCool-Magisk-Module/blob/main/README-pt-br.md) | [Spanish](https://github.com/Llucs/SpeedCool-Magisk-Module/blob/main/README-es.md) | [Russian](https://github.com/Llucs/SpeedCool-Magisk-Module/blob/main/README-ru.md)

---

## ✨ Key Features

SpeedCool goes beyond basic optimization, offering a comprehensive suite of enhancements:

*   **Enhanced Performance:** Boosts overall performance and gaming experience, ensuring a fluid and responsive system.
*   **Intelligent Thermal Control:** Significantly reduces CPU and GPU overheating, extending your hardware's lifespan and maintaining peak performance.
*   **Efficient RAM Management:** Frees up RAM for a lighter, more responsive, and stable system.
*   **Adaptive System:** Optimizations that dynamically adjust to your usage patterns, guaranteeing optimal performance without compromising battery life.
*   **Interactive Menu:** Full control via Termux with a user-friendly menu to customize settings and profiles.
*   **Broad Compatibility:** Works across a wide range of devices and chipsets, including **MediaTek, Tensor, Kirin, Qualcomm, Exynos**, and more.
*   **Automatic App-Based Profiles (v2.2+):** Detects foreground apps and automatically switches performance profiles based on your defined rules [1].
*   **Full Auto-Update via GitHub (v2.2+):** SpeedCool checks for updates, downloads the `.zip`, and installs it automatically [1].
*   **Profile Backup & Import (v2.2+):** Easily save and restore your settings directly in Termux [1].
*   **Bootloop Recovery System (v2.2+):** Detects repeated reboots and automatically restores a safe "eco" profile, preventing system instability [1].
*   **Modular Script Design (v2.2+):** Separate scripts for performance, cooling, learning, app monitoring, updates, and backups, allowing independent operation and restart [1].

---

## 📱 Compatibility

*   **Android Versions:** From Android 6 to Android 16.
*   **Processors:** Compatible with **MediaTek, Tensor, Kirin, Qualcomm, Exynos** (other chipsets may also work).
*   **ROMs:** Functions on any ROM that supports Magisk.
*   **Adaptive Optimization:** Adapts to your device's brand and model for maximum efficiency.

> 💡 **Ideal for all types of devices:** from entry-level to flagships, SpeedCool ensures a noticeable improvement.

---

## 🏗️ How It Works

SpeedCool utilizes advanced scripts that optimize in real-time:

*   **CPU and GPU:** Fine-tuned adjustments for performance and temperature.
*   **I/O (Input/Output):** Accelerates data reading and writing.
*   **RAM:** Intelligent management to prevent bottlenecks.
*   **Background Services:** Optimizes processes to conserve resources.

**All this happens automatically at startup, without a graphical interface to keep the module light and efficient.** For manual control, use the Termux menu.

---

## 📦 Installation

> [!TIP]
> Follow these simple steps to install SpeedCool:

> 1.  Open **Magisk Manager**.
> 2.  Go to the **Modules** tab.
> 3.  Tap on **Install from storage**.
> 4.  Select the `SpeedCool_Vx.x.zip` file (ensure you download the latest version).
> 5.  **Reboot your device** to apply the optimizations.

### ➕ Accessing the Control Menu (via Termux)

To access SpeedCool's advanced settings and profiles:

1.  Open **Termux**.
2.  Type `su` and grant superuser permission.
3.  Execute the command:

    ```bash
    sh /data/adb/modules/speedcool/scripts/menu.sh
    ```

---

⚠️ **Important Warnings**

> [!CAUTION]
> *   💾 **Always back up** your system before installing any Magisk module.
> *   🚫 **Avoid Conflicts:** Do not use SpeedCool with other performance optimization modules (like FDE.AI or Aionix), as instabilities may occur. **We recommend using only SpeedCool to prevent issues.**
> *   ✅ **Easy Uninstallation:** You can uninstall SpeedCool at any time via Magisk Manager.
> *   ⚠️ **Use at Your Own Risk:** Although tested and functional, behavior may vary slightly depending on the device and ROM.

---

## 🚀 Upcoming Updates

We are always looking to improve SpeedCool! Your suggestions are very welcome in our update group. Join us to shape the future of the module!

---

## 📢 Update Channel

Stay informed about the latest news and versions:

🔗 [SpeedCool Releases on Telegram](https://t.me/SpeedCool_Releases)

---

## 🌐 Official Website

Visit our page for more information and news:

[Website 🌐](https://llucs.github.io/SpeedCool-Magisk-Module/)

---

## ❗ Common Errors and Solutions

*   **Error: "This is not a valid Magisk module"**
    *   **Cause:** Outdated Magisk version.
    *   **Solution:** Update Magisk to version 29.0 or higher.
    *   If the problem persists, there may be a conflict with another module or incompatibility.

---

## 🤝 Contribute to SpeedCool

SpeedCool thrives on community contributions! We welcome developers, testers, and enthusiasts to help us make this module even better. Here's how you can contribute:

*   **Report Bugs:** Found an issue? Open a detailed issue on GitHub with steps to reproduce.
*   **Suggest Features:** Have an idea for a new feature or improvement? Share it in the issues or our Telegram group.
*   **Submit Pull Requests:** Want to contribute code? Fork the repository, make your changes, and submit a pull request. Please follow our coding style and include clear commit messages.
*   **Improve Documentation:** Help us refine the READMEs, add translations, or create guides.
*   **Spread the Word:** Share SpeedCool with others! Your support helps our community grow.

---

## ⭐ Show Your Support

If you find SpeedCool useful, please consider giving us a star ⭐ on GitHub! It helps increase visibility and motivates us to continue improving the module.

[![Star History Chart](https://api.star-history.com/svg?repos=Llucs/SpeedCool-Magisk-Module&type=Date)](https://www.star-history.com/#Llucs/SpeedCool-Magisk-Module&Date)

---

## 👨‍💻 Developer

🔥 **Llucs (Leandro Lucas)**

SpeedCool © 2025 — Made with ❤️ by @Llucs

> Created by those who understand, for those who seek true control.

---

## 📚 References

[1] Llucs/SpeedCool-Magisk-Module. `changelog.txt`. Available at: [https://github.com/Llucs/SpeedCool-Magisk-Module/blob/main/changelog.txt](https://github.com/Llucs/SpeedCool-Magisk-Module/blob/main/changelog.txt)