package fs2.kafka.vulcan

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient
import org.scalatest.funspec.AnyFunSpec
import vulcan.{AvroError, Codec}

final class AvroDeserializerSpec extends AnyFunSpec {
  describe("AvroDeserializer") {
    it("can create a deserializer") {

      assert(
        AvroDeserializer[Int].forKey(avroSettings).use(IO.pure).attempt.unsafeRunSync().isRight
      )
      assert(
        AvroDeserializer[Int].forValue(avroSettings).use(IO.pure).attempt.unsafeRunSync().isRight
      )
    }

    it("raises schema errors") {
      val codec: Codec[Int] =
        Codec.instance(
          Left(AvroError("error")),
          _ => Left(AvroError("encode")),
          (_, _) => Left(AvroError("decode"))
        )

      assert(
        avroDeserializer(codec).forKey(avroSettings).use(IO.pure).attempt.unsafeRunSync().isLeft
      )
      assert(
        avroDeserializer(codec).forValue(avroSettings).use(IO.pure).attempt.unsafeRunSync().isLeft
      )
    }

    it("toString") {
      assert {
        avroDeserializer[Int].toString() startsWith "AvroDeserializer$"
      }
    }
  }

  val schemaRegistryClient: MockSchemaRegistryClient =
    new MockSchemaRegistryClient()

  val schemaRegistryClientSettings: SchemaRegistryClientSettings[IO] =
    SchemaRegistryClientSettings[IO]("baseUrl")
      .withAuth(Auth.Basic("username", "password"))
      .withMaxCacheSize(100)
      .withCreateSchemaRegistryClient { (_, _, _) =>
        IO.pure(schemaRegistryClient)
      }

  val avroSettings: AvroSettings[IO] =
    AvroSettings(schemaRegistryClientSettings)
}
