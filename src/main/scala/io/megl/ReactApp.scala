package io.megl

import io.megl.router.AppRouter
import japgolly.scalajs.react.React
import org.scalajs.dom

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

object ReactApp  extends JSApp{
  @JSExport
  override def main(): Unit = React.render(AppRouter.C() ,dom.document.body)
//  override def main(): Unit =  dom.document.body.innerHTML = " fuck "
}
