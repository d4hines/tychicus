FROM clojure:lein-2.8.1


# Change this to the most recent commit when you build the Dockerfile.
ENV GIT_SHA 6fdf18eb5862a8945caffb32bcffe456e02f295c

RUN git clone https://github.com/d4hines/tychicus.git

WORKDIR "/tmp/tychicus"

RUN git checkout ${GIT_SHA}
RUN lein uberjar

# Be sure to change the version number as well.
CMD java -jar ./target/uberjar/tychicus-0.1.0-SNAPSHOT-standalone.jar
