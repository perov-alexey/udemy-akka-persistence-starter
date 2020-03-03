package part4_practicies

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence.PersistentActor
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.persistence.query.PersistenceQuery
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

object PersistenceQueryDemo extends App {

  val system = ActorSystem("PersistenceQueryDemo", ConfigFactory.load().getConfig("persistenceQuery"))

  // read journal
  val readJournal = PersistenceQuery(system).readJournalFor[CassandraReadJournal](CassandraReadJournal.Identifier)

  val persistenceIds = readJournal.persistenceIds()

  implicit val materializer = ActorMaterializer()(system)
  persistenceIds.runForeach { persistenceId =>
    println(s"Found persistence ID: $persistenceId")
  }

  class SimplePersistentActor extends PersistentActor with ActorLogging {
    override def persistenceId: String = "persistence-query-id-1"

    override def receiveCommand: Receive = {
      case m =>
        persist(m) { _ =>
          log.info(s"Persisted: $m")
        }
    }

    override def receiveRecover: Receive = {
      case e => log.info(s"Recovered $e")
    }
  }

  val simpleActor = system.actorOf(Props[SimplePersistentActor], "SimplePersistentActor")

  import system.dispatcher
  import scala.concurrent.duration._

  system.scheduler.scheduleOnce(5 seconds) {
    val message = "Test"
    simpleActor ! message
  }

}
