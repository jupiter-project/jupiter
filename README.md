----
# Welcome to Jupiter! # [![pipeline status] (data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMTYiIGhlaWdo%0D%0AdD0iMjAiPgogIDxsaW5lYXJHcmFkaWVudCBpZD0iYiIgeDI9IjAiIHkyPSIxMDAlIj4KICAgIDxz%0D%0AdG9wIG9mZnNldD0iMCIgc3RvcC1jb2xvcj0iI2JiYiIgc3RvcC1vcGFjaXR5PSIuMSIvPgogICAg%0D%0APHN0b3Agb2Zmc2V0PSIxIiBzdG9wLW9wYWNpdHk9Ii4xIi8+CiAgPC9saW5lYXJHcmFkaWVudD4K%0D%0ACiAgPG1hc2sgaWQ9ImEiPgogICAgPHJlY3Qgd2lkdGg9IjExNiIgaGVpZ2h0PSIyMCIgcng9IjMi%0D%0AIGZpbGw9IiNmZmYiLz4KICA8L21hc2s+CgogIDxnIG1hc2s9InVybCgjYSkiPgogICAgPHBhdGgg%0D%0AZmlsbD0iIzU1NSIKICAgICAgICAgIGQ9Ik0wIDAgaDYyIHYyMCBIMCB6Ii8+CiAgICA8cGF0aCBm%0D%0AaWxsPSIjNGMxIgogICAgICAgICAgZD0iTTYyIDAgaDU0IHYyMCBINjIgeiIvPgogICAgPHBhdGgg%0D%0AZmlsbD0idXJsKCNiKSIKICAgICAgICAgIGQ9Ik0wIDAgaDExNiB2MjAgSDAgeiIvPgogIDwvZz4K%0D%0ACiAgPGcgZmlsbD0iI2ZmZiIgdGV4dC1hbmNob3I9Im1pZGRsZSI+CiAgICA8ZyBmb250LWZhbWls%0D%0AeT0iRGVqYVZ1IFNhbnMsVmVyZGFuYSxHZW5ldmEsc2Fucy1zZXJpZiIgZm9udC1zaXplPSIxMSI+%0D%0ACiAgICAgIDx0ZXh0IHg9IjMxIiB5PSIxNSIgZmlsbD0iIzAxMDEwMSIgZmlsbC1vcGFjaXR5PSIu%0D%0AMyI+CiAgICAgICAgcGlwZWxpbmUKICAgICAgPC90ZXh0PgogICAgICA8dGV4dCB4PSIzMSIgeT0i%0D%0AMTQiPgogICAgICAgIHBpcGVsaW5lCiAgICAgIDwvdGV4dD4KICAgICAgPHRleHQgeD0iODkiIHk9%0D%0AIjE1IiBmaWxsPSIjMDEwMTAxIiBmaWxsLW9wYWNpdHk9Ii4zIj4KICAgICAgICBwYXNzZWQKICAg%0D%0AICAgPC90ZXh0PgogICAgICA8dGV4dCB4PSI4OSIgeT0iMTQiPgogICAgICAgIHBhc3NlZAogICAg%0D%0AICA8L3RleHQ+CiAgICA8L2c+CiAgPC9nPgo8L3N2Zz4K)](https://gitlab.com/sigwotechnologies/jupiter/commits/master)

----
## What is Jupiter? ##
Jupiter is the base platform needed to run the [Gravity](https://github.com/SigwoTechnologies/jupiter-gravity) framework for information storage and decentralized application building. Jupiter is built from [Nxt's](https://nxt.org) system based on cryptography and blockchain technology.

----
## Get it! ##

=======
  - *pre-packaged* - `https://github.com/SigwoTechnologies/jupiter/releases` and download the latest release.

  - *dependencies*:
    - *General* - Java 8
    - *Ubuntu* - `apt install openjdk-8-jre-headless`
    - *Debian* - `http://www.webupd8.org/2014/03/how-to-install-oracle-java-8-in-debian.html`
    - *FreeBSD* - `pkg install openjdk8`

  - *Repository* - `git clone https://github.com/SigwoTechnologies/jupiter/`
  
----
## Run it! ##

  - Click on the Jupiter icon, or start from the command line:
     - Unix: `bash start.sh`
     - Window: `run.bat`

  - Wait for the JavaFX wallet window to open.
  - On platforms without JavaFX, open http://localhost:7876/ in a browser.

----
## Compile it! ##

  - If necessary with: `./compile.sh`.
  - You need jdk-8 as well.

----
## Improve it! ##

  - We love **pull requests**
  - We love issues (resolved ones actually ;-) )
  - In any case, make sure you leave **your ideas** at GitHub
  - Assist others on the issue tracker
  - **Review** existing code and pull requests

----
## Troubleshooting the JRS (Jupiter Reference Software) ##

  - How to Stop the JRS Server?
    - Click on Jupiter Stop icon, or run `bash stop.sh`.
    - Or if started from command line, ctrl+c or close the console window.

  - UI Errors or Stacktraces?
    - Report on Github

  - Permissions Denied?
    - No spaces and only latin characters in the path to the JRS installation directory.
    - Known jetty issue.

----
## Further Reading (WIP)##

  - In this repository: 
    - USERS-GUIDE.md
    - DEVELOPERS-GUIDE.md
    - OPERATORS-GUIDE.md
    
----

