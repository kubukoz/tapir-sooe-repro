package demo

import cats.effect.IO
import cats.effect.IOApp
import com.comcast.ip4s.host
import com.comcast.ip4s.port
import io.circe.Codec
import io.circe.Decoder
import io.circe.Encoder
import io.circe.Json
import io.circe.JsonObject
import org.http4s.HttpRoutes
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import sttp.tapir.Schema
import sttp.tapir.server.http4s.Http4sServerInterpreter

object Main extends IOApp.Simple {

  enum Base derives Codec.AsObject {
    case EmptyImage
    // even this case isn't necessary to reproduce the issue
    case ImageReference(hash: String)
  }

  object Base {
    implicit def s: Schema[Base] = Schema.derived

    // Manual circe coders to exclude likelihood of the issue being in there
    given Encoder[Base] = {
      case Base.EmptyImage =>
        Json.obj("EmptyImage" -> Json.obj())
      case Base.ImageReference(hash) =>
        Json.obj("ImageReference" -> Json.obj("hash" -> Json.fromString(hash)))
    }

    given Decoder[Base] = Decoder[JsonObject]
      .at("EmptyImage")
      .map(_ => Base.EmptyImage)
      .or(
        Decoder[String].at("hash").at("ImageReference").map(Base.ImageReference(_))
      )

  }

  def run: IO[Unit] =
    EmberServerBuilder
      .default[IO]
      .withHost(host"0.0.0.0")
      .withPort(port"8083")
      .withHttpApp {
        import sttp.tapir._
        import sttp.tapir.json.circe._

        Http4sServerInterpreter[IO]()
          .toRoutes(
            infallibleEndpoint
              .in("run")
              .post
              .in(jsonBody[Base])
              .serverLogicInfallible(IO.println(_))
          )
          .orNotFound
      }
      .build
      .useForever

}
