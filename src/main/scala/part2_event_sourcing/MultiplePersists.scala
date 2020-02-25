package part2_event_sourcing

import java.util.Date

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.persistence.PersistentActor

object MultiplePersists extends App {

  // COMMANDS
  case class Invoice(recipient: String, date: Date, amount: Int)

  // EVENTS
  case class TaxRecord(taxId: String, recordId: Int, date: Date, totalAmount: Int)
  case class InvoiceRecord(invoiceRecordId: Int, recipient: String, date: Date, amount: Int)

  object DiligentAccountant {
    def props(taxId: String, taxAuthority: ActorRef) = Props(new DiligentAccountant(taxId: String, taxAuthority: ActorRef))
  }

  class DiligentAccountant(taxId: String, taxAuthority: ActorRef) extends PersistentActor with ActorLogging {

    var latestTaxRecordId = 0
    var latestInvoiceRecordId = 0

    override def persistenceId: String = "diligent-accountant"

    override def receiveCommand: Receive = {
      case Invoice(recipient, date, amount) =>
        persist(TaxRecord(taxId, latestTaxRecordId, date, amount / 3)) { record =>
          taxAuthority ! record
          latestTaxRecordId += 1
          persist("I here to declare tax record to be true and complete") { declaration =>
            taxAuthority ! declaration
          }
        }
        Thread.sleep(3000)
        persist(InvoiceRecord(latestInvoiceRecordId, recipient, date, amount)) { invoiceRecord =>
          taxAuthority ! invoiceRecord
          latestInvoiceRecordId += 1
          persist("I here to declare invoice record to be true and complete") { declaration =>
            taxAuthority ! declaration
          }
        }
    }

    override def receiveRecover: Receive = {
      case event => log.info(s"Recovered: $event")
    }
  }

  class TaxAuthority extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(s"Received $message")
    }
  }

  val system = ActorSystem("MultiplePersistsDemo")
  val taxAuthority = system.actorOf(Props[TaxAuthority], "HMRC")
  val diligentAccountant = system.actorOf(DiligentAccountant.props("qwe_123", taxAuthority), "accountant")

  diligentAccountant ! Invoice("Sofa company", new Date, 2000)
}
