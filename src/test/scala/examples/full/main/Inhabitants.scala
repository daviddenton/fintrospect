package examples.full.main

class Inhabitants {
  private var users: Seq[Username] = Nil

  def add(user: Username): Boolean = {
    if (users.contains(user)) false
    else {
      users = users :+ user
      true
    }
  }

  def remove(user: Username): Boolean = {
    if (users.contains(user)) {
      users = users.filterNot(_ == user)
      true
    } else false
  }
}
