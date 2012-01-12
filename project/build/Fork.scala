import sbt._

trait Fork extends DefaultProject {
  
  def forkConfiguration = new ForkScalaRun {
    override def scalaJars = {
      val si = buildScalaInstance
      si.libraryJar :: si.compilerJar :: Nil
    }
    override def runJVMOptions = Seq(
      "-XX:MaxPermSize=128m",
      "-Xss16m",
      "-Xmx1024m",
      "-Dfile.encoding=UTF-8",
      "-Djava.ext.dirs=",
      "-Dapple.awt.graphics.UseQuartz=true",
      // these will pick up the default language and country
      // from the JVM or OS, but they can be overridden.
      // See Internationalization.scala in this directory.
      "-Duser.language=" + System.getProperty("user.language"),
      "-Duser.country=" + System.getProperty("user.country")) ++
    (if(System.getProperty("os.name").startsWith("Mac"))
      Seq("-Xdock:name=NetLogo")
     else Seq()) ++
    (if(System.getProperty("org.nlogo.noGenerator") == "true")
      Seq("-Dorg.nlogo.noGenerator=true")
     else Seq())
  }

}
