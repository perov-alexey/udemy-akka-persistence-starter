package part2_event_sourcing

import akka.actor.ActorRef

package object adapter {

  object EngineProtocol {
    case object StartEngine

    case class EngineStartedEvent(processor: ActorRef)
  }

  object ProcessorProtocol {
    case class StartProcessor(actorList: List[String])
    case object RunNextStage
    case object StopProcessor

    case class StageStartedEvent(stageList: List[String])
  }

}
