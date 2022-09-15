package models

import requests.TextAnalyzeRequest
import services.KafkaService

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

/**
  * Wordslist model for text analysis
  */
@Singleton
class WordslistModel @Inject() (kafkaService: KafkaService) {

  def analyzeText(request: TextAnalyzeRequest): Future[_] = {
    val text = request.text

    kafkaService.countWords(text)
  }

}
