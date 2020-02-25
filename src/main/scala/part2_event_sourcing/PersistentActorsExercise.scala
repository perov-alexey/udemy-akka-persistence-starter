package part2_event_sourcing

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence.PersistentActor

object PersistentActorsExercise extends App {

  // Commands
  case class Vote(citizenPID: String, candidate: String)
  case object PrintResults

  class VoteMachine extends PersistentActor with ActorLogging {

    var votedCitizens: Set[String] = Set()
    var poll: Map[String, Int] = Map()

    override def persistenceId: String = "vote-machine"

    override def receiveCommand: Receive = {
      case vote @ Vote(citizenPID, candidate) =>
        if (!votedCitizens.contains(citizenPID)) {
          persist(vote) { e =>
            log.info(s"Vote of $citizenPID is polled")
            handleInternalStateChange(citizenPID, candidate)
          }
        } else {
          log.warning(s"Citizen $citizenPID is already voted")
        }
      case PrintResults =>
        log.info(s"Voted citizens: ${votedCitizens.mkString(", ")}")
        log.info(s"Candidate results: ${poll.mkString(", ")}")
    }

    def handleInternalStateChange(citizenPID: String, candidate: String): Unit = {
      if (poll.contains(candidate)) {
        poll = poll + (candidate -> (poll(candidate) + 1))
      } else {
        poll = poll + (candidate -> 1)
      }
      votedCitizens = votedCitizens + citizenPID
    }

    override def receiveRecover: Receive = {
      case Vote(citizenPID, candidate) =>
        handleInternalStateChange(citizenPID, candidate)
        log.info(s"Vote of ${citizenPID} is recovered")
    }
  }

  val system = ActorSystem("VoteMachine")
  val voteMachineActor = system.actorOf(Props[VoteMachine], "voteMachine")

  voteMachineActor ! Vote("Alexey", "CandidateA")
  voteMachineActor ! Vote("Evgeny", "CandidateB")
  voteMachineActor ! Vote("Alexander", "CandidateB")
  voteMachineActor ! Vote("AlexanderG", "CandidateB")
  voteMachineActor ! Vote("Alexey", "CandidateA")
  voteMachineActor ! PrintResults
}
