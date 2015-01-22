package io.megl.router

import japgolly.scalajs.react.{ReactElement, ReactComponentB}
import japgolly.scalajs.react.extra.router.{BaseUrl, Redirect, RoutingRules}
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react._

object AppRouter {

  object AppPage extends RoutingRules {


    // *************
    // Static Routes
    // *************

    val root       = register(rootLocation(RootComponent))
    val hello: Loc = register(location("#hello", HelloComponent))

    register(redirection("#hey", hello, Redirect.Replace))

    // **************
    // Dynamic Routes
    // **************

    // This example matches /name/<anything>

    private val namePathMatch = "^#name/(.+)$".r
    register(parser { case namePathMatch(n) => n }.location(n => NameComponent(n)))
    val name = dynLink[String](n => s"#name/$n")

    // This example matches /person/<number>
    //     and redirects on /person/<not-a-number>

    private val personPathMatch = "^/person/(.+)$".r
    register(parser { case personPathMatch(p) => p }.thenMatch {
      case matchNumber(idStr)     => render(PersonComponent(PersonId(idStr.toLong)))
      case _ /* non-numeric id */ => redirect(root, Redirect.Push)
    })
    val person = dynLink[PersonId](id => s"/person/${id.value}")

    // *******
    // General
    // *******

    register(removeTrailingSlashes)

    override protected val notFound = redirect(root, Redirect.Replace)

    override protected def interceptRender(i: InterceptionR): ReactElement =
      if (i.loc == root)
        i.element
      else
        <.div(
          <.div(i.router.link(root)("Back", ^.cls := "back")),
          i.element)
  }

  val RootComponent = ReactComponentB[AppPage.Router]("Root")
    .render(router =>
    <.div(
      <.h2("Router Demonstration"),
      <.p("This is the root page. Click on a link below to view routes within this page."),
      <.div(router.link(AppPage.hello)("The 'hello' route", ^.cls := "hello")),
      <.div(router.link(AppPage.name("bob"))("Name('bob')", ^.cls := "n1")),
        <.div(router.link(AppPage.name("crap"))("Name('crap')", ^.cls := "n2")))
    ).build


  val HelloComponent = ReactComponentB[Unit]("Hello")
    .render(_ =>
      <.div(
        <.h3("Hello there!" ),
        <.a(^.href := AppPage.name("bob").path.abs(baseUrl).value )("fuck it" )
      )
      )
    .buildU


  val NameComponent = ReactComponentB[String]("Name")
    .render(name =>
      <.h3(s"I believe your name is '$name'."))
    .build

  case class PersonId(value: Long)
  val PersonComponent = ReactComponentB[PersonId]("Person by ID")
    .render(p => <.h3(s"Person #${p.value} Details..."))
    .build


  val baseUrl = BaseUrl.fromWindowOrigin / "admin"
  val C = AppPage.router(baseUrl)

}
