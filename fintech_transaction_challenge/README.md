# Payment Engine

Plain Java implementation of a payment processing engine. No frameworks required.

## Setup

Download the JUnit 5 test runner into `lib/` (one-time):

```bash
mkdir -p lib && curl -L -o lib/junit-platform-console-standalone-1.11.3.jar \
  "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.11.3/junit-platform-console-standalone-1.11.3.jar"
```

## Compile

```bash
javac -cp lib/junit-platform-console-standalone-1.11.3.jar \
      src/model/*.java src/service/*.java test/TransactionProcessorTest.java \
      -d out/
```

## Run tests

```bash
java -jar lib/junit-platform-console-standalone-1.11.3.jar execute \
     --class-path out/ \
     --scan-class-path
```
