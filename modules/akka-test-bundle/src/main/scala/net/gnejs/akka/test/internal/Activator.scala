/**
 * Created with IntelliJ IDEA.
 * User: anders
 * Date: 6/9/12
 * Time: 11:08 PM
 * To change this template use File | Settings | File Templates.
 */

package net.gnejs.akka.test.internal

import org.osgi.framework.{BundleContext, BundleActivator}
import akka.actor.{Props, ActorSystem, ActorLogging, Actor}

case class Tick(createdAt: Long = System.currentTimeMillis())

class Ticker extends Actor with ActorLogging {


  override def preStart() {
    super.preStart()
    log.info("Starting actor")
    context.system.registerOnTermination(log.info("ActorSystem shutting down"))
  }


  override def postStop() {
    super.postStop()
    log.info("Actor was stopped")
  }

  def scheduleNextTick() {
    import akka.util.duration._
    context.system.scheduler.scheduleOnce(5.seconds)(if (!context.system.isTerminated) context.system.eventStream.publish(Tick()))
  }

  protected def receive = {
    case evt: Any => {
      log.info("Event received {}", evt)
      scheduleNextTick()
    }
  }
}

class ExampleActivator extends BundleActivator {

  var actorSystem: Option[ActorSystem] = None

  def start(ctx: BundleContext) {
    println("Starting bundle " + ctx.getBundle.getSymbolicName)

    actorSystem = Some(ActorSystem("test-bundle"))

    // Register the Ticker actor as a listener to the eventStream
    val ticker = actorSystem.map(_.actorOf(Props[Ticker])).getOrElse(throw new Exception("Just weird"))
    actorSystem.foreach(sys => sys.eventStream.subscribe(ticker, classOf[Tick]))
    // Send a message to the bus.
    ticker ! "TickerKicker"
  }
  def stop(ctx: BundleContext) {
    actorSystem.foreach(_.shutdown())
    actorSystem = None
    println("Stopped bundle " + ctx.getBundle.getSymbolicName)
  }
}


