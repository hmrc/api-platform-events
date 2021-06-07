package uk.gov.hmrc.apiplatformevents.support

import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

abstract class ServerBaseISpec
  extends BaseISpec with GuiceOneServerPerSuite with TestApplication {

  override implicit lazy val app: Application = appBuilder.build()

   override protected def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "mongodb.uri" -> s"mongodb://127.0.0.1:27017/test-${this.getClass.getSimpleName}"
      )
}
