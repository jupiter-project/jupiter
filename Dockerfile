# Jupiter docker image
#
# to use:
#
# 1. install docker, see docker.com
# 2. clone the git repo including this Dockerfile
# 3. build the container with ```docker build -t jupiter .```
# 4. run the created Gravity container with ```docker run -d -p 127.0.0.1:7876 jupiter```


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
RUN apt-get update && apt-get install -y build-essential software-properties-common python-software-properties g++ libssl-dev apache2-utils curl git python make nano wget unzip 
RUN apt install -y openjdk-8-jre-headless openjdk-8-jdk

# compile Jupiter
RUN cd /root && git clone https://github.com/sigwotechnologies/jupiter && \
cd jupiter && chmod + compile.sh && ./compile.sh && ./run.sh

# both Jupiter ports get exposed
EXPOSE 7874 7876
