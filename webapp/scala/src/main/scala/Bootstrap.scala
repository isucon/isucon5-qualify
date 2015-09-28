
import javax.servlet.ServletContext

import isucon5.Isucon5
import skinny.micro._

class Bootstrap extends LifeCycle {

  override def init(ctx: ServletContext) {
    Isucon5.mount(ctx)
  }
}

