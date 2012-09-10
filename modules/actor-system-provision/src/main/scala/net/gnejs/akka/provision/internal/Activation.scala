package net.gnejs.akka.provision.internal

import org.osgi.framework.{BundleContext, BundleActivator}

/**
 * Gateway to the OSGi environment.
 * Registers the ActorSystem service-factory and wires up the configuration
 * against the OSGi ConfigurationAdmin.
 */
class Activator extends BundleActivator {
  def start(ctx: BundleContext) {
    println("START")
  }
  def stop(ctx: BundleContext) {
    println("STOP")
  }
}