package part2_event_sourcing.adapter

import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, RecoveryCompleted}

class Processor extends PersistentActor with ActorLogging {

  override def persistenceId: String = "processor-actor"

  override def receiveCommand: Receive = {
    case msg @ ProcessorProtocol.StartProcessor(stages) =>
      log.info(msg.toString)

      context.become(receiveWithStageList(stages))
      self ! ProcessorProtocol.RunNextStage
    case msg =>
      log.info(s"Message $msg ignored")
  }

  def receiveWithStageList(stageList: List[String]): Receive = {
    case msg @ ProcessorProtocol.RunNextStage =>
      log.info(msg.toString)

      persist(ProcessorProtocol.StageStartedEvent(stageList)) { event =>
        log.info(s"Simulate stage work, current state: $stageList")
        Thread.sleep(5000)

        context.become(receiveWithStageList(stageList.tail :+ stageList.head))
        self ! ProcessorProtocol.RunNextStage
      }
    case msg =>
      log.info(s"Message $msg ignored")
  }

  override def receiveRecover: Receive = {
    case msg @ RecoveryCompleted =>
      log.info(msg.toString)
    case msg @ ProcessorProtocol.StageStartedEvent(stageList) =>
      log.info(msg.toString)

      context.become(receiveWithStageList(stageList))
  }

}
