package ch.becompany.authn

import cats.data.State
import cats.implicits._
import ch.becompany.authn.domain.{AuthnError, RegistrationError}
import ch.becompany.shapelessext._
import ch.becompany.shared.domain.{EmailAddress, User}
import shapeless.tag.@@

import scala.language.higherKinds

trait Dsl[F[_]] {

  def register(email: String @@ EmailAddress, password: String):
      F[Either[RegistrationError, User]]

  def authn(email: String @@ EmailAddress, password: String):
      F[Either[AuthnError, User]]

}

object Dsl {

  type UserRepository = List[User]

  type UserRepositoryState[A] = State[UserRepository, A]

  implicit object StateInterpreter extends Dsl[UserRepositoryState] {

    override def register(email: String @@ EmailAddress, password: String):
        UserRepositoryState[Either[RegistrationError, User]] =
      State { users =>
        if (users.exists(_.email === email))
          (users, RegistrationError("User already exists").asLeft)
        else {
          val user = User(email, password)
          (users :+ user, user.asRight)
        }
      }

    override def authn(email: String @@ EmailAddress, password: String):
        UserRepositoryState[Either[AuthnError, User]] =
      State.inspect(_
        .find(user => user.email === email && user.password === password)
        .toRight(AuthnError("Authentication failed")))
  }

}