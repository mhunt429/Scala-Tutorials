import akka.actor.{Actor, ActorRef}
import akka.event.LoggingReceive

object WireTransfer {
  case class Transfer(from: ActorRef, to: ActorRef, amount: BigDecimal)
  case object Done
  case object Failed
}

class WireTransfer extends Actor {
  import WireTransfer._
  def receive = LoggingReceive{
    case Transfer(from, to, amount) =>
      from ! BankAccount.Withdraw(amount)
      context.become(awaitWithdraw(to, amount, sender))
  }
  def awaitWithdraw(to: ActorRef, amount: BigDecimal, client: ActorRef): Receive = {
    case BankAccount.Done =>
      to ! BankAccount.Deposit(amount)
      context.become(awaitDeposit(client))
    case BankAccount.Failed =>
      client ! Failed
      context.stop(self)
  }

  def awaitDeposit(client: ActorRef): Receive = {
    case BankAccount.Done =>
      client ! Done
      context.stop(self)
  }
}