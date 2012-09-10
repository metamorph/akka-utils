package net.gnejs.akka.monitor.internal

import org.osgi.framework.{ServiceReference, BundleContext, BundleActivator}
import org.osgi.util.tracker.ServiceTracker
import akka.actor.ActorSystem

class Activator extends BundleActivator {

  var actorSystemTracker: Option[ServiceTracker[ActorSystem, Any]] = None

  def start(ctx: BundleContext) {
    actorSystemTracker = Some(new ServiceTracker[ActorSystem, Any](ctx, classOf[ActorSystem], null){
      override def addingService(reference: ServiceReference[ActorSystem]): Any = super.addingService(reference)
      override def removedService(reference: ServiceReference[ActorSystem], service: Any) {
        super.removedService(reference, service)
      }
    })
    actorSystemTracker.foreach(_.open())
  }
  def stop(p1: BundleContext) {
    actorSystemTracker.foreach(_.close())
  }
}