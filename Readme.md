# Q-Rapids Kafka Connector for Sonarqube ![](https://img.shields.io/badge/License-Apache2.0-blue.svg)

An Apache Kafka Connector that collects Issues and Measures from Sonarqube.

This component has been created as a result of the Q-Rapids project funded by the European Union Horizon 2020 Research and Innovation programme under grant agreement No 732253.

## Running the Connector

### Prerequisites

* Kafka has to be setup and running (see [Kafka Connect](https://docs.confluent.io/current/connect/index.html))
* If you want your data to be transfered to Elasticsearch, Elasticsearch has to be setup and running. (see [Set up Elasticsearch](https://www.elastic.co/guide/en/elasticsearch/reference/current/setup.html))

### Build the connector
```
mvn package assembly:single
```
After build, you'll find the generated jar in the target folder

### Configuration files

Example Configuration for kafka standalone connector (standalone.properties)

```properties 
bootstrap.servers=<kafka-ip>:9092

key.converter=org.apache.kafka.connect.storage.StringConverter
value.converter=org.apache.kafka.connect.json.JsonConverter

key.converter.schemas.enable=true
value.converter.schemas.enable=true

internal.key.converter=org.apache.kafka.connect.json.JsonConverter
internal.value.converter=org.apache.kafka.connect.json.JsonConverter
internal.key.converter.schemas.enable=false
internal.value.converter.schemas.enable=false

offset.storage.file.filename=/tmp/connect-sonarqube.offsets

offset.flush.interval.ms=1000
rest.port=8088
```
#### Sonarqube Configuration
Configuration for Sonarqube Source Connector Worker (sonarqube.properties)

```properties
name=kafka-sonar-source-connector
connector.class=connect.sonarqube.SonarqubeSourceConnector
tasks.max=1

# sonarqube server url
sonar.url=http://<your-sonarqube-address>:9000

#authenticate, user need right to Execute Analysis
sonar.user=<sonaruser>
sonar.pass=<sonarpass>

# key for measure collection
sonar.basecomponent.key=<key of application under analysis>

#projectKeys for issue collection
sonar.project.key=<key of application under analysis>

# kafka topic names
sonar.measure.topic=sonar.metrics
sonar.issue.topic=sonar.issues

# measures to collect, since sonarqube6 max 15 metrics
# see https://docs.sonarqube.org/latest/user-guide/metric-definitions/
sonar.metric.keys=ncloc,lines,comment_lines,complexity,violations,open_issues,code_smells,new_code_smells,sqale_index,new_technical_debt,bugs,new_bugs,reliability_rating,classes,functions

#poll interval (86400 secs = 24 h)
sonar.interval.seconds=86400

#set snapshotDate manually, format: YYYY-MM-DD
sonar.snapshotDate=
```

Configuration for Elasticsearch Sink Connector Worker (elasticsearch.properties)

```properties
name=kafka-sonarqube-elasticsearch
connector.class=io.confluent.connect.elasticsearch.ElasticsearchSinkConnector
tasks.max=1
topics=sonarqube.measures,sonarqube.issues
key.ignore=true
connection.url=http://<elasticsearch>:9200
type.name=sonarqube

```

#### Mantis Configuration
Configuration for Sonarqube Source Connector Worker (sonarqube.properties)

```properties
name=kafka-mantis-source-connector
connector.class=connect.mantis.MantisSourceConnector
tasks.max=1

# Mantis Data Base Url
mantis.url=jdbc:mysql://<your-mantis-address>:3307/mantis

# Mantis Data Base Credidential
mantis.user=
mantis.pass=

# Monitored Project
mantis.project=Modelio

# kafka topic names
topic.newissue=mantis.issues
topic.updatedissue=mantis.update
topic.stat=mantis.stat

#poll interval (86400 secs = 24 h)
poll.interval.seconds=86400

```

Configuration for Elasticsearch Sink Connector Worker (elasticsearch.properties)

```properties
name=kafka-elasticsearch-mantis
connector.class=io.confluent.connect.elasticsearch.ElasticsearchSinkConnector
tasks.max=1
topics=mantis.issues,mantis.update,mantis.stat
key.ignore=true
connection.url=http://<elasticsearch>:9200
type.name=mantis
```

#### OpenProject Configuration
Configuration for Sonarqube Source Connector Worker (sonarqube.properties)

```properties
name=kafka-openproject-ModelioNG-source-connector
connector.class=connect.openproject.OpenProjectSourceConnector
tasks.max=1

# OpenProject Server Url
openproject.url=http://<your-openproject-address>/openproject

# OpenProject Api Key
openproject.apikey=

#Name of the monitored Project
openproject.project=

# kafka topic names
openproject.topic=openproject
openproject.stat.topic=openproject.stat

#poll interval (86400 secs = 24 h)
poll.interval.seconds=86400

```

Configuration for Elasticsearch Sink Connector Worker (elasticsearch.properties)

```properties
name=kafka-openproject-elasticsearch
connector.class=io.confluent.connect.elasticsearch.ElasticsearchSinkConnector
tasks.max=1
topics=openproject,openproject.stat
key.ignore=true
connection.url=http://<elasticsearch>:9200
type.name=openproject
```

End with an example of getting some data out of the system or using it for a little demo


## Running the Connector

```
<path-to-kafka>/bin/connect-standalone standalone.properties sonarqube.properties elasticsearch.properties
```

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management


## Authors

* **Axel Wickenkamp, Fraunhofer IESE**

