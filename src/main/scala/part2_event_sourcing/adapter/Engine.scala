package part2_event_sourcing.adapter

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted}

object Engine extends App {
  val system = ActorSystem("AdapterSimulation")
  val engine = system.actorOf(Props[Engine], "engine")
//  engine ! EngineProtocol.StartEngine
}

class Engine extends PersistentActor with ActorLogging {
  override def persistenceId: String = "engine-actor"

  override def receiveCommand: Receive = {
    case msg @ EngineProtocol.StartEngine =>
      log.info(msg.toString)

      val processor = context.actorOf(Props[Processor], "processor")

      persist(EngineProtocol.EngineStartedEvent(processor)) { event =>
        processor ! ProcessorProtocol.StartProcessor(List("Stage1", "Stage2", "Stage3", "Stage4", "Stage5"))

        context.become(startedReceive)
      }
  }

  def startedReceive: Receive = {
    case msg =>
      log.info(s"Engine already started, incoming message $msg ignored")
  }

  override def receiveRecover: Receive = {
    case msg @ EngineProtocol.EngineStartedEvent(processor) =>
      log.info(msg.toString)

      context.become(startedReceive)
  }
}
