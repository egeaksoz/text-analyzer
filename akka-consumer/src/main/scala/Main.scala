import akka.Done
import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.alpakka.slick.javadsl.SlickSession
import akka.stream.alpakka.slick.scaladsl.Slick
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{LongDeserializer, StringDeserializer}

import scala.concurrent.Future

case class WordMessage(word: String, count: Long)

object Main extends App {
  implicit val system = ActorSystem("text-analyzer")

  // Kafka settings
  val bootstrapServers = "localhost:9092"
  val topic = "text-output"

  // Slick/database settings
  implicit val session: SlickSession = SlickSession.forConfig("slick-postgres")

  //
  val config = system.settings.config.getConfig("akka.kafka.consumer")
  val consumerSettings =
    ConsumerSettings(config, new StringDeserializer, new LongDeserializer)
      .withBootstrapServers(bootstrapServers)
      .withGroupId("group1")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  // imported for ease of sqlu usage
  import session.profile.api._

  val done: Future[Done] = Consumer
    .committableSource(consumerSettings, Subscriptions.topics(topic))
    .map { msg =>
      println("This is the message: " + msg.record.key() + " and " + msg.record.value())
      WordMessage(msg.record.key(), msg.record.value())
    }.via(
    Slick.flow(wordslist => sqlu"insert into wordslist(word, count) values (${wordslist.word}, ${wordslist.count}) on conflict on constraint wordslist_word_key do update set count = EXCLUDED.count")
  ).log("sql-logs")
    .runWith(Sink.ignore)
}
