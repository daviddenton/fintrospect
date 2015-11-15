package examples.full.main

case class Id(value: Int)
case class Username(value: String)
case class EmailAddress(value: String)
case class User(id: Id, name: Username, email: EmailAddress)
