package uk.gov.hmrc.apiplatformevents.support

import org.apache.pekko.stream.Materializer
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.apiplatformevents.utils.AsyncHmrcSpec

abstract class BaseISpec extends AsyncHmrcSpec with WireMockSupport {

  def app: Application
  protected def appBuilder: GuiceApplicationBuilder

  protected implicit def materializer: Materializer = app.materializer

  private lazy val messagesApi            = app.injector.instanceOf[MessagesApi]
  private implicit def messages: Messages = messagesApi.preferred(Seq.empty[Lang])

  protected def htmlEscapedMessage(key: String): String = HtmlFormat.escape(Messages(key)).toString

  implicit def hc(implicit request: FakeRequest[_]): HeaderCarrier =
    HeaderCarrierConverter.fromRequestAndSession(request, request.session)

}
