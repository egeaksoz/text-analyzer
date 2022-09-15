package models

import play.api.libs.json.Json

final case class Wordslist(id: Int, word: String, count: Long)

object Wordslist {
  implicit val wordlistFormat = Json.format[Wordslist]
}
