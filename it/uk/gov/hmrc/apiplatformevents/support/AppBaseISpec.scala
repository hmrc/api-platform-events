package uk.gov.hmrc.apiplatformevents.support

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application

abstract class AppBaseISpec extends BaseISpec with GuiceOneAppPerSuite with TestApplication {

  override implicit lazy val app: Application = appBuilder.build()

}
