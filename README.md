##Packaging
To create executable jar run "sbt one-jar"

Then deploy this to your env and java -jar <jar path>

# elder-api

In order to run unit tests in docker container

sudo docker build -t elder-api-test --file DockerFile.test .

sudo docker run --rm -i -v scala-ivy:/root/.ivy2 elder-api-test
