package responses

import play.api.libs.json.{Writes, __}

final case class TextAnalyzeSucceedResponse(message: String)

object TextAnalyzeSucceedResponse {

  implicit val textAnalyzeSucceedResponseWrites
      : Writes[TextAnalyzeSucceedResponse] =
    (__ \ "message").write[String].contramap(_.message)

}
