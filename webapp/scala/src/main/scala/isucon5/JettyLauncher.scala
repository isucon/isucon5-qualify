package isucon5

import javax.servlet.ServletContextListener

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.webapp.WebAppContext
import skinny.logging.LoggerProvider
import skinny.micro.SkinnyListener

object JettyLauncher extends JettyServer {
  def main(args: Array[String]): Unit = {
    if (args.length > 0) {
      val port = args(0)
      System.setProperty("skinny.port", port)
    }

    run()
  }
}

trait JettyServer extends LoggerProvider {

  def listener(listener: ServletContextListener): JettyServer = {
    this.listener = listener
    this
  }

  def port(port: Int): JettyServer = {
    _port = port
    this
  }

  def run() = {
    start()
    server.join
  }

  def start(): Unit = {
    refreshServer()
    logger.info(s"Starting Jetty server on port ${port}")
    val context = new WebAppContext()
    val contextPath = sys.env.get("SKINNY_PREFIX").orElse(getEnvVarOrSysProp("skinny.prefix")).getOrElse("/")
    context.setContextPath(contextPath)
    context.setWar({
      val domain = this.getClass.getProtectionDomain
      val location = domain.getCodeSource.getLocation
      location.toExternalForm
    })
    context.addEventListener(listener)
    context.addServlet(classOf[DefaultServlet], "/")
    server.setHandler(context)

    server.start
    logger.info(s"Started Jetty server on port ${port}")
  }

  def stop(): Unit = {
    if (server != null) {
      server.stop()
    }
  }

  private[this] var server: Server = _

  private[this] var listener: ServletContextListener = new SkinnyListener

  private[this] var _port: Int = {
    // PORT: Heroku default env varaible
    Option(System.getenv("PORT")).map(_.toInt).getOrElse(8080)
  }

  private[this] def port: Int = {
    val port = sys.env.get("SKINNY_PORT").orElse(getEnvVarOrSysProp("skinny.port")).map(_.toInt).getOrElse(_port)
    port
  }

  private[this] def newServer(port: Int): Server = new Server(port)

  private[this] def refreshServer(): Unit = {
    if (server != null) {
      stop()
      server.destroy()
      server.synchronized {
        server = newServer(port)
      }
    } else {
      server = newServer(port)
    }
  }

  private[this] def getEnvVarOrSysProp(key: String): Option[String] = {
    sys.env.get(key).orElse(sys.props.get(key))
  }

}

