# Monolith (Java)

This is a sample monolithic application written in Java.

## Instructions

Open up the 'monolith' folder

```shell
cd monolith
```

Check that you have Powershell 7

```shell
$PSVersionTable.PSVersion
```

Ensure you have JDK 21 installed

```shell
java -version
```

Check that JAVA_HOME is set correctly & points to your JDK 21 installation

```shell
echo $env:JAVA_HOME
```

Build the application using Maven

```shell
./mvnw clean package
```

Run the application

```shell
./mvnw spring-boot:run
```

Rebuild and restart the application

```shell
./mvnw clean package && ./mvnw spring-boot:run
```

App should now be running on:
http://localhost:8080/
