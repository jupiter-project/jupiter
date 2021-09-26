---
description: The people's blockchain
---

# Welcome

### What is Jupiter?

Our main product is our L1 blockchain, Jupiter. Jupiter is the base platform needed to run the [Gravity](https://github.com/jupiter-project/gravity) framework for information storage and decentralized application building. Jupiter is built from [Nxt's](https://nxt.org) system based on cryptography and blockchain technology. On top of this anyone can have assets, currencies, aliases, data, messaging, a DEX, and NFTs.

Currently, our primary decentralized application is [Metis](https://getmetis.io), a decentralized messaging system that is double encrypted and requires no personally identifiable information \(PII\) to create an account. We are going through rebranding now with new logo and website presence.

### Get it!

* _prepackaged_ - `https://github.com/jupiter-project/jupiter/releases` and download the latest release.
* _dependencies_:
  * _General_ - Java 8
  * _Ubuntu_ - `apt install openjdk-8-jdk-headless`
  * _Debian_ - `http://www.webupd8.org/2014/03/how-to-install-oracle-java-8-in-debian.html`
  * _FreeBSD_ - `pkg install openjdk8`
  * _MacOS_ \(from Terminal\)
    * `brew tap AdoptOpenJDK/openjdk` 
    * `brew install --cask adoptopenjdk8` 
    * To check, `java -version`
* _Repository_ - `git clone https://github.com/jupiter-project/jupiter/`

### Run it!

* Click on the Jupiter icon, or start from the command line:
  * Unix: `bash start.sh`
  * Window: `run.bat`
* Wait for the JavaFX wallet window to open.
* On platforms without JavaFX, open [http://localhost:7876/](http://localhost:7876/) in a browser.

### Compile it!

* If necessary with: `./compile.sh`.
* You need jdk-8 as well.

### Improve it!

* We love **pull requests**
* We love issues \(resolved ones actually ;-\) \)
* In any case, make sure you leave **your ideas** at GitHub
* Assist others on the issue tracker
* **Review** existing code and pull requests

### Troubleshooting the JRS \(Jupiter Reference Software\)

* How to Stop the JRS Server?
  * Click on Jupiter Stop icon, or run `bash stop.sh`.
  * Or if started from command line, ctrl+c or close the console window.
* UI Errors or Stacktraces?
  * Report on Github
* Permissions Denied?
  * No spaces and only latin characters in the path to the JRS installation directory.
  * Known jetty issue.

