package repositories

import models.Wordslist
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

/**
  * A repository for wordslist.
  *
  * @param dbConfigProvider The Play db config provider. Play will inject this for you.
  */
@Singleton
class WordslistRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(
    implicit ec: ExecutionContext
) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class WordslistTable(tag: Tag)
      extends Table[Wordslist](tag, "wordslist") {

    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def word = column[String]("word")

    def count = column[Long]("count")

    def * = (id, word, count) <> ((Wordslist.apply _).tupled, Wordslist.unapply)
  }

  private val wordslist = TableQuery[WordslistTable]

  def list(): Future[Seq[Wordslist]] = {
    db.run {
      wordslist.result
    }
  }
}
