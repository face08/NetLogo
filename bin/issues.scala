#!/usr/bin/env scalas
!#

// This generates Markdown suitable for pasting into
// https://github.com/NetLogo/NetLogo/wiki/Release-notes

// running this is tricky because the sbt script mode stuff isn't currently maintained in sync with
// conscript so you have fiddle with it or it doesn't work. instructions:
// - install conscript if you don't have it already:
//   curl https://raw.github.com/n8han/conscript/master/setup.sh | sh
// - install sbt (and the scalas script) through conscript:
//   cs harrah/xsbt --branch v0.12.0
// - edit ~/.conscript/harrah/xsbt/scalas/launchconfig
//   and change the Scala version from `auto` to `2.9.2`
//   and change the cross-versioned settings from `true` to `false`
// - ensure that ~/bin is included in your `PATH` environment variable
//   (this is where Conscript places the scripts that it manages)
// - note: if you run the script for the first time and it appears non-responsive, do not fear;
//         wait at least a few minutes for it to try downloading the dependencies before panicking!

/***
scalaVersion := "2.9.2"

onLoadMessage := ""

scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xfatal-warnings")

libraryDependencies ++= Seq(
  "net.databinder.dispatch" %% "dispatch-core" % "0.9.2",
  "net.liftweb" % "lift-json_2.9.1" % "2.4",
  "org.slf4j" % "slf4j-nop" % "1.6.0")
*/

import dispatch._
import net.liftweb.json.JsonParser
import net.liftweb.json.JsonAST._

object Issue {
  def fromJson(j: JValue): Issue = {
    val JInt(n) = j \ "number"
    val JString(title) = j \ "title"
    val JArray(labels) = j \ "labels"
    Issue(n.toInt, title,
          labels.map(_ \ "name").collect{case JString(s) => s})
  }
}
case class Issue(number: Int, title: String, labels: List[String])

val host = :/("api.github.com").secure
val base = host / "repos" / "NetLogo" / "NetLogo" / "issues"
val req = base <<? Map("milestone" -> "13",
                       "state" -> "closed",
                       "per_page" -> "1000")
// println(req.build.getRawUrl)  useful for debugging
val stream = Http(req OK as.Response(_.getResponseBodyAsStream)).apply
val JArray(array) = JsonParser.parse(new java.io.InputStreamReader(stream))
val issues: List[Issue] =
  for (item <- array)
  yield Issue.fromJson(item)

println(issues.size + " issues fixed!")
for(Issue(n, title, labels) <- issues.sortBy(_.number)) {
  val labelsString =
    if (labels.isEmpty) ""
    else labels.mkString("", ", ", ": ")
  println(" * " + labelsString + title + " ([#" + n + "]" +
          "(https://github.com/NetLogo/NetLogo/issues/" + n + "))")
}

Http.shutdown()
