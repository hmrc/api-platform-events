package uk.gov.hmrc.apiplatformevents.support

import org.scalatestplus.play.OneAppPerSuite
import play.api.Application
import uk.gov.hmrc.apiplatformevents.stubs.AuthStubs

abstract class AppBaseISpec extends BaseISpec with OneAppPerSuite with TestApplication with AuthStubs {

  override implicit lazy val app: Application = appBuilder.build()

}
