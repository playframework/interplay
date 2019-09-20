 package interplay

 import sbt.util.{CacheStore, CacheStoreFactory}
 import sjsonnew.support.scalajson.unsafe.Converter
 import sjsonnew.{JsonReader, JsonWriter}

/**
 * Creates a cache which does nothing. Used by PlaySbtCompat. Helpful for
 * working around https://github.com/sbt/sbt/issues/1614
 */
object NopCacheStoreFactory extends CacheStoreFactory {
  override def make(identifier: String): CacheStore = NopCacheStore
  /**
   * The sub-factory operation just returns the same object
   * since there's no difference between nop-factories.
   */
  override def sub(identifier: String): CacheStoreFactory = this
}

/**
 * A cache which does nothing. Used by PlaySbtCompat. Helpful for
 * working around https://github.com/sbt/sbt/issues/1614
 */
object NopCacheStore extends CacheStore {
  override def read[T: JsonReader]() = Converter.fromJsonOptionUnsafe(None)
  override def write[T: JsonWriter](value: T) = Converter.toJson(value)
  override def delete() = ()
  override def close() = ()
}