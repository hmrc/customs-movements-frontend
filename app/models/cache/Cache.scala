package models.cache

import play.api.libs.json.{Json, OFormat}

case class Cache(eori: String, answers: Answers)

object Cache {
  implicit val format: OFormat[Cache] = Json.format[Cache]
}

