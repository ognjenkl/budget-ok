# System Test (Java)

## Instructions

Open up the 'system-test' folder

```shell
cd system-test
```

Check that you have Powershell 7

```shell
$PSVersionTable.PSVersion
```

Start Docker Containers

```shell
docker compose up -d
```

Run All Tests

```shell
./mvnw test
```

Run Smoke Tests Only

```shell
./mvnw test -Dtest="com.ognjen.template.systemtest.smoketests.**"
```

Stop Docker Containers

```shell
docker compose down
```