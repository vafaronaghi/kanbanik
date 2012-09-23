package com.googlecode.kanbanik.db
import org.bson.types.ObjectId
import com.mongodb.casbah.commons.MongoDBObject
import com.googlecode.kanbanik.model.HasMongoConnection
import com.googlecode.kanbanik.model.DocumentField
import com.mongodb.DBObject
import com.googlecode.kanbanik.model.MidAirCollisionException
import com.googlecode.kanbanik.model.Board
import com.googlecode.kanbanik.model.DocumentField
import com.mongodb.casbah.Imports._

trait HasMidAirCollisionDetection extends HasMongoConnection {
  def versionedQuery(id: Option[ObjectId], version: Int) = {
    // the "$exists" is due to a bug in casbah - will be removed when moving to newer version
    (MongoDBObject(SimpleField.id.toString() -> id)) ++ ($or((SimpleField.version.toString() -> MongoDBObject("$exists" -> false)), (SimpleField.version.toString() -> version)))
  }

  def versionedUpdate(collection: Coll.Value, query: DBObject, update: DBObject) = {
    using(createConnection) { conn =>
      val res = coll(conn, collection).findAndModify(query, null, null, false, update, true, false)
      res.getOrElse(throw new MidAirCollisionException)
    }
  }

  def versionedDelete(collection: Coll.Value, query: DBObject) {
    using(createConnection) { conn =>
      val res = coll(conn, collection).findAndModify(query, null, null, true, null, true, false)
      if (!res.isDefined) {
        throw new MidAirCollisionException
      }
    }
  }

  object SimpleField extends DocumentField {

  }
}