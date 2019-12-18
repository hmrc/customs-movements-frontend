package connectors.exchanges

case class Query(value: String, eori: String)
case class QueryResult(value: String, eori: String, id: String) // ID is NOT the conversation ID. Lets not let that seep into our services again.
