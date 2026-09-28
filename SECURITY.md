# Security policy

Report security issues privately to the repository owner through GitHub's private vulnerability reporting feature. Do not publish signing material, keystore passwords, private device logs, or a working exploit in an issue.

This module runs inside Android's system notification service. Runtime failures in its lookup path are designed to fail open, but hook compatibility must be checked after ROM updates. Disable the module in LSPosed and reboot to restore normal behavior.
