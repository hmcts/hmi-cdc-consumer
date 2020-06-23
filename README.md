Start the broker first running the pact docker-compose:
docker-compose -f docker-pactbroker-compose.yml up

You can run contract or pact tests as follows:

./gradlew clean contract
You can then publish your pact tests locally by
using it to publish your tests:

./gradlew pactPublish
Tech

It uses:

Java8
Spring boot
Junit, Mockito and SpringBootTest and Powermockito
Gradle
lombok project - Lombok project
Plugins
