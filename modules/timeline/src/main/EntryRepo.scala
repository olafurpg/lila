package lila.timeline

import lila.db.dsl._
import org.joda.time.DateTime
import reactivemongo.bson._

private[timeline] final class EntryRepo(coll: Coll, userMax: Int) {

  import Entry._

  def userEntries(userId: String): Fu[List[Entry]] =
    userEntries(userId, userMax)

  def moreUserEntries(userId: String, nb: Int): Fu[List[Entry]] =
    userEntries(userId, nb)

  private def userEntries(userId: String, max: Int): Fu[List[Entry]] =
    coll.find($doc("users" -> userId)).sort($doc("date" -> -1)).cursor[Entry]().gather[List](max)

  def findRecent(typ: String, since: DateTime) =
    coll
      .find($doc(
          "typ" -> typ,
          "date" -> $doc("$gt" -> since)
        ))
      .cursor[Entry]()
      .gather[List]()

  def channelUserIdRecentExists(channel: String, userId: String): Fu[Boolean] =
    coll.count(
      $doc(
        "users" -> userId,
        "chan" -> channel,
        "date" $gt DateTime.now.minusDays(7)
      ).some) map (0 !=)

  def insert(entry: Entry) = coll insert entry void
}
