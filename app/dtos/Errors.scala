package dtos

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Writes, __}

sealed trait Errors
final case class RequestError(message: String) extends Errors
final case class ServerError(message: String, throwable: Throwable)
    extends Errors

object Errors {

  implicit val requestErrorWrites: Writes[RequestError] =
    (__ \ "message").write[String].contramap(_.message)

  implicit val serverErrorWrites: Writes[ServerError] = (
    (__ \ "message").write[String] and
      (__ \ "errorMessage").write[String]
  )(se => (se.message, se.throwable.getMessage))

}
