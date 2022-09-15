package requests

import play.api.libs.json.{Json, Reads}

final case class TextAnalyzeRequest(text: String)

object TextAnalyzeRequest {
  implicit val text: Reads[TextAnalyzeRequest] = Json.reads[TextAnalyzeRequest]
}
