resolvers += "HMRC-open-artefacts-maven" at "https://open.artefacts.tax.service.gov.uk/maven2"
resolvers += Resolver.url("HMRC-open-artefacts-ivy", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)

addSbtPlugin("uk.gov.hmrc"        %  "sbt-auto-build"         % "3.20.0")
addSbtPlugin("com.typesafe.play"  %  "sbt-plugin"             % "2.9.1")
addSbtPlugin("uk.gov.hmrc"        %  "sbt-distributables"     % "2.4.0")
addSbtPlugin("org.scoverage"      %  "sbt-scoverage"          % "2.0.9")
addSbtPlugin("org.scalastyle"     %% "scalastyle-sbt-plugin"  % "1.0.0")
addSbtPlugin("ch.epfl.scala"      %  "sbt-bloop"               % "1.5.11")
addSbtPlugin("org.scalameta"      %  "sbt-scalafmt"            % "2.5.2")
addSbtPlugin("ch.epfl.scala"      %% "sbt-scalafix"           % "0.11.1")

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always