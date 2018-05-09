package ch.becompany

import cats.{Eval, Monad}
import cats.implicits._
import ch.becompany.authn.Dsl.{UserRepository, UserRepositoryState}
import ch.becompany.authn.domain.AuthnError
import ch.becompany.authn.{Dsl => AuthnDsl}
import ch.becompany.shared.domain.{EmailAddress, User}
import shapeless.tag

import scala.language.higherKinds

object Main extends App {

  def registerAndLogin[F[_] : Monad](implicit authnDsl: AuthnDsl[F]):
      F[Either[AuthnError, User]] = {

    val email = tag[EmailAddress]("john@doe.com")
    val password = "swordfish"

    for {
      _ <- authnDsl.register(email, password)
      authenticated <- authnDsl.authn(email, password)
    } yield authenticated
  }

  val userRepositoryState = registerAndLogin[UserRepositoryState]

  val result = userRepositoryState.runEmpty

  val (users, authenticated) = result.value

  println("Authenticated: " + authenticated)
  println("Registered users: " + users)
}
