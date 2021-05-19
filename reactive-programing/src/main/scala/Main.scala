
import akka.actor.{Actor, ActorRef, Props, ActorSystem}
import akka.event.LoggingReceive


class App extends Actor {
  def print() = println("Hello")
  import BankAccount._
  val accountA = context.actorOf(Props[BankAccount], "accountA")
  val accountB = context.actorOf(Props[BankAccount], "accountB")

  accountA ! BankAccount.Deposit(100)

  def receive = LoggingReceive {
    case BankAccount.Done => transfer(250)
  }
  def transfer(amount: BigDecimal) = {
    val transaction = context.actorOf(Props[WireTransfer], "transfer")
    transaction ! WireTransfer.Transfer(accountA, accountB, amount)
    context.become(LoggingReceive{
      case WireTransfer.Done =>
        println("success!")
        context.stop(self)
    })
  }
  var balance = BigDecimal(0)
}

object Main  {
  def main(args: Array[String]) = {
    val system = ActorSystem("HelloSystem")
    val mainActor = system.actorOf(Props[App], "demoActor")
  }

}