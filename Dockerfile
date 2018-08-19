# Gravity docker image
#
# to use:
#
# 1. install docker, see docker.com
# 2. clone the git repo including this Dockerfile
# 3. build the container with ```docker build -t gravity .```
# 4. run the created Gravity container with ```docker run -d -p 127.0.0.1:4001 gravity```


FROM phusion/baseimage
# start off with standard ubuntu images

# Set local and enable UTF-8
ENV LANG C.UTF-8
ENV LANGUAGE C
ENV LC_ALL C.UTF-8

# add user with sudo privileges within Docker container
# without adduser input questions
# http://askubuntu.com/questions/94060/run-adduser-non-interactively/94067#94067
#RUN USER="gravity" && \
#RUN adduser --disabled-password --gecos "" $USER && \
#RUN sudo usermod -a -G sudo $USER && \
#RUN echo "$USER:abc123" | chpasswd && \
#RUN su - $USER # switch to testuser

# Install Jupiter
RUN apt-get update && apt-get install -y build-essential software-properties-common python-software-properties git curl
RUN add-apt-repository ppa:webupd8team/java -y
RUN apt-get update
RUN apt-get install -y wget unzip
RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
RUN apt-get install -y oracle-java8-installer

# compile Jupiter
RUN cd /root && git clone https://github.com/sigwotechnologies/jupiter && \
cd jupiter && chmod + compile.sh && ./compile.sh

# both Jupiter ports get exposed
EXPOSE 7874 7876

# update and install all required packages (no sudo required as root)
# https://gist.github.com/isaacs/579814#file-only-git-all-the-way-sh
RUN apt-get update -yq && apt-get upgrade -yq && \
apt-get install -yq g++ libssl-dev apache2-utils curl git python make nano

# install latest Node.js and npm
# https://gist.github.com/isaacs/579814#file-node-and-npm-in-30-seconds-sh
RUN mkdir ~/node-latest-install && cd $_ && \
curl http://nodejs.org/dist/node-latest.tar.gz | tar xz --strip-components=1 && \
./configure && \
make install && \
curl https://www.npmjs.org/install.sh

# Install dependencies for Gravity
RUN cd /root && git clone https://github.com/sigwotechnologies/jupiter-gravity && \
cd jupiter-gravity && npm install

# Expose port for web connectivity
EXPOSE 4001 4000
