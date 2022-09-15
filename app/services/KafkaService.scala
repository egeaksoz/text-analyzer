package services

import com.typesafe.config.Config
import org.apache.kafka.clients.admin.{Admin, NewTopic}
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.errors.TopicExistsException
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import org.apache.kafka.streams.scala.kstream.{KStream, KTable}
import play.api.{ConfigLoader, Configuration}
import org.apache.kafka.streams.scala.Serdes._
import org.apache.kafka.streams.scala.ImplicitConversions._

import java.time.Duration
import java.util
import java.util.Properties
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.jdk.CollectionConverters.{MapHasAsJava, SeqHasAsJava}

final case class ConsumerConfigs(
    keyDeserializer: String,
    valueDeserializer: String,
    autoOffsetReset: String,
    groupId: String
)

final case class ProducerConfigs(keySerializer: String, valueSerializer: String)

final case class TopicConfigs(
    producerName: String,
    consumerName: String,
    partition: Int,
    replicationFactor: Int
)

object ConsumerConfigs {
  implicit val configLoader: ConfigLoader[ConsumerConfigs] =
    new ConfigLoader[ConsumerConfigs] {
      def load(rootConfig: Config, path: String): ConsumerConfigs = {
        val config = rootConfig.getConfig(path)
        ConsumerConfigs(
          keyDeserializer = config.getString("key.deserializer"),
          valueDeserializer = config.getString("value.deserializer"),
          autoOffsetReset = config.getString("auto.offset.reset"),
          groupId = config.getString("group.id")
        )
      }
    }
}

object ProducerConfigs {
  implicit val configLoader: ConfigLoader[ProducerConfigs] =
    new ConfigLoader[ProducerConfigs] {
      def load(rootConfig: Config, path: String): ProducerConfigs = {
        val config = rootConfig.getConfig(path)
        ProducerConfigs(
          keySerializer = config.getString("key.serializer"),
          valueSerializer = config.getString("value.serializer")
        )
      }
    }
}

object TopicConfigs {
  implicit val configLoader: ConfigLoader[TopicConfigs] =
    new ConfigLoader[TopicConfigs] {
      def load(rootConfig: Config, path: String): TopicConfigs = {
        val config = rootConfig.getConfig(path)
        TopicConfigs(
          producerName = config.getString("producer-topic.name"),
          consumerName = config.getString("consumer-topic.name"),
          partition = config.getInt("partitions"),
          replicationFactor = config.getInt("replication-factor")
        )
      }
    }
}

@Singleton
class KafkaService @Inject() (config: Configuration) {

  val bootstrapServer: String = config.get[String]("kafka.bootstrap.servers")
  val producerTopic: String =
    config.get[TopicConfigs]("kafka.topics").producerName
  val consumerTopic: String =
    config.get[TopicConfigs]("kafka.topics").consumerName
  val partition: Int = config.get[TopicConfigs]("kafka.topics").partition
  val replicationFactor: Int =
    config.get[TopicConfigs]("kafka.topics").replicationFactor

  def setupTopics(): Unit = {
    val props = new Properties()
    props.put(
      "bootstrap.servers",
      bootstrapServer
    )
    val admin = Admin.create(props)
    try {
      val newTopics: List[NewTopic] = List(
        new NewTopic(producerTopic, partition, replicationFactor.toShort),
        new NewTopic(consumerTopic, partition, replicationFactor.toShort)
      )
      admin.createTopics(newTopics.asJava)
    } catch {
      case topicExistsException: TopicExistsException =>
        println(topicExistsException)
      case _: Throwable =>
        println("Another problem occured while creating topic")
    }
  }

  def readFromKafka(): Unit = {
    val props = new Properties()
    props.put(
      "bootstrap.servers",
      bootstrapServer
    )
    props.put(
      "key.deserializer",
      config.get[ConsumerConfigs]("kafka.consumer").keyDeserializer
    )
    props.put(
      "value.deserializer",
      config.get[ConsumerConfigs]("kafka.consumer").valueDeserializer
    )
    props.put(
      "auto.offset.reset",
      config.get[ConsumerConfigs]("kafka.consumer").autoOffsetReset
    )
    props.put("group.id", config.get[ConsumerConfigs]("kafka.consumer").groupId)
    val consumer: KafkaConsumer[String, String] =
      new KafkaConsumer[String, String](props)
    val consumerTopic = config.get[TopicConfigs]("kafka.topics").consumerName
    consumer.subscribe(util.Arrays.asList(consumerTopic))
  }

  private def writeToKafkaProducer(text: String): Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", bootstrapServer)
    props.put(
      "key.serializer",
      config
        .get[ProducerConfigs]("kafka.producer")
        .keySerializer
    )
    props.put(
      "value.serializer",
      config
        .get[ProducerConfigs]("kafka.producer")
        .valueSerializer
    )
    val producer = new KafkaProducer[String, String](props)

    val record = new ProducerRecord[String, String](producerTopic, text)
    producer.send(record)
    producer.close()
  }

  def countWords(text: String): Future[_] = {
    writeToKafkaProducer(text)

    val KStreamConfig: Properties = {
      val p = new Properties()
      p.put(StreamsConfig.APPLICATION_ID_CONFIG, "text-analyzer")
      p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer)
      p
    }

    val builder = new StreamsBuilder()
    val textLines: KStream[String, String] =
      builder.stream[String, String]("text-input")
    val wordCounts: KTable[String, Long] = textLines
      .flatMapValues(textLine => textLine.toLowerCase.split("\\W+"))
      .groupBy((_, word) => word)
      .count()
    wordCounts.toStream.to("text-output")

    val streams: KafkaStreams = new KafkaStreams(builder.build(), KStreamConfig)

    streams.cleanUp()
    streams.start()

    sys.ShutdownHookThread {
      streams.close(Duration.ofSeconds(10))
    }

    Future.successful()

  }

}
