# Jupiter docker image
#
# to use:
#
# 1. install docker, see docker.com
# 2. clone the git repo including this Dockerfile
# 3. build the container with ```docker build -t jupiter .```
# 4. run the created Gravity container with ```docker run -d -p 7876:7876 jupiter```


FROM openjdk:8
# start off with standard ubuntu images

WORKDIR /usr/jupiter

# Set local and enable UTF-8
ENV LANG C.UTF-8
ENV LANGUAGE C
ENV LC_ALL C.UTF-8

COPY . .

# compile Jupiter
RUN chmod + compile.sh && ./compile.sh

# make start script executable
RUN chmod + start.sh

# both Jupiter ports get exposed
EXPOSE 7864 7876

CMD ./run.sh