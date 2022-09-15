package controllers

import dtos.{RequestError, ServerError}
import models.WordslistModel

import javax.inject.Inject
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import repositories.WordslistRepository
import requests.TextAnalyzeRequest
import responses.TextAnalyzeSucceedResponse

import scala.concurrent.ExecutionContext
import scala.util.Failure

class WordslistController @Inject() (
    wordslistRepo: WordslistRepository,
    wordslistModel: WordslistModel,
    cc: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(cc) {

  def analyze: Action[JsValue] = {
    Action(parse.json) { implicit request =>
      request.body.asOpt[TextAnalyzeRequest] match {
        case Some(req) => {
          wordslistModel.analyzeText(req).map {
            case Failure(t) =>
              InternalServerError(
                Json.toJson(
                  ServerError(
                    "An error occurred while text sent for analysis",
                    t
                  )
                )
              )
          }
          Ok(
            Json.toJson(
              TextAnalyzeSucceedResponse("Text successfully sent for analysis.")
            )
          )
        }
        case None =>
          BadRequest(
            Json.toJson(
              RequestError(
                "Invalid request, check if text parameter exists."
              )
            )
          )
      }
    }
  }

  def results: Action[AnyContent] =
    Action.async {
      wordslistRepo.list().map { wordsAndCounts =>
        Ok(Json.toJson(wordsAndCounts))
      }
    }
}
