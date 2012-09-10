package net.gnejs.akka.provision.internal

import org.osgi.framework._
import akka.actor.ActorSystem
import akka.event.{Logging, LoggingBus}
import com.typesafe.config.ConfigFactory

/**
 * Gateway to the OSGi environment.
 * Registers the ActorSystem service-factory and wires up the configuration
 * against the OSGi ConfigurationAdmin.
 */
class Activator extends BundleActivator {
  var sharedActorSystem: Option[ActorSystem] = None
  def start(ctx: BundleContext) {

    System.setProperty("akka.loglevel", "DEBUG")
    System.setProperty("akka.log-config-on-start", "off")

    // Sneaky - since the configuration isn't visible from outside of the akka bundle - we need to create the
    // actor-system with a class-loader that points to the akka bundle.
    // TODO: This will change when the configuration mechanism is in place.
    sharedActorSystem = Some(ActorSystem("sharedActorSystem", ConfigFactory.load(classOf[ActorSystem].getClassLoader)))
    sharedActorSystem.foreach(sys => ctx.registerService(classOf[ActorSystem].getName, new ActorSystemProvider(sys), null))
  }
  def stop(ctx: BundleContext) {
    sharedActorSystem.foreach(_.shutdown())
  }
}

/**
 * Simple provider. At the moment there is only one actor-system bound to the
 * lifecycle of this bundle.
 *
 * Things to add:
 * + dedicated actor-system per client.
 * + configuration per actor-system.
 * + shared actor-system for several clients (lifecycle reference-counter)
 *
 */
class ActorSystemProvider(sharedActorSystem: ActorSystem) extends ServiceFactory[ActorSystem] {
  val log = Logging(sharedActorSystem, classOf[ActorSystemProvider])

  def getService(client: Bundle, serviceReg: ServiceRegistration[ActorSystem]): ActorSystem = {
    log.info("Client [{}] asking for ActorSystem", client.getSymbolicName)
    sharedActorSystem
  }
  def ungetService(client: Bundle, serviceReg: ServiceRegistration[ActorSystem], service: ActorSystem) {
    log.info("Client [{}] is returning ActorSystem {}", client.getSymbolicName, service)
  }
}