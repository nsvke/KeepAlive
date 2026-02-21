# KeepAlive

**Automated personal relationship management assistant for Android.**

![Status](https://img.shields.io/badge/Status-Public_Release-brightgreen) ![Architecture](https://img.shields.io/badge/Architecture-MVVM-blue) ![UI](https://img.shields.io/badge/UI-Jetpack_Compose-green) ![License](https://img.shields.io/badge/License-MIT-yellow)

## 📖 Overview

**KeepAlive** is designed to address the challenge of maintaining personal connections in a fast-paced environment. Unlike traditional "to-do" lists or reminders that require constant manual input, KeepAlive operates on a "self-driving" principle.

The application seamlessly integrates with the Android telephony system to monitor call logs, automatically updating the interaction status for tracked contacts. This ensures that users receive reminders only when necessary, preventing redundant notifications for contacts recently spoken to.

## ✨ Key Features

* **Automated Call Log Synchronization:** Eliminates manual data entry by scanning device call history to update "Last Contact" timestamps intelligently.
* **Dynamic Frequency Management:** Allows users to define specific contact intervals for different individuals, adapting to the unique nature of each relationship.
* **Hybrid Interaction Tracking:** Includes a manual override feature to account for face-to-face meetings or interactions outside of phone calls.
* **Smart Scheduler:**
    * **Self-Correcting Mechanism:** Automatically reschedules reminders if a call is detected before the due date.
    * **Frictionless Action:** Notifications provide direct access to the dialer, minimizing the steps required to initiate a call.
* **Privacy-Centric Design:** Operates entirely offline. All data is stored locally on the device via Room Database, with zero cloud dependency.

## 📅 Roadmap (Upcoming Features)

- [ ] **Contact Pausing:** Temporarily stop tracking for a specific contact without deleting them (e.g., for vacations).
- [ ] **Advanced Notifications:** Add "Snooze" and "Mark as Called" actions directly to notifications.
- [ ] **Household Logic:** Link multiple contacts (like parents) as a single group for shared interaction tracking.

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
*Developed with ❤️ to keep connections alive.*
