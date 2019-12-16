----
# Welcome to Jupiter! #

----
## What is Jupiter? ##
Jupiter is the base platform needed to run the [Gravity](https://github.com/SigwoTechnologies/jupiter-gravity) framework for information storage and decentralized application building. Jupiter is built from [Nxt's](https://nxt.org) system based on cryptography and blockchain technology.

----
## Get it! ##

  - *pre-packaged* - `https://github.com/SigwoTechnologies/jupiter/releases/download/1.11.7.3/jrs-client-1.11.7.3.zip`

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

