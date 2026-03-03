package mct.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
private data class IntRangeDelegate(val startInclusive: Int, val endInclusive: Int)

object IntRangeSerializer : KSerializer<IntRange> {
    private val delegated = IntRangeDelegate.serializer()
    override val descriptor get() = delegated.descriptor

    override fun serialize(encoder: Encoder, value: IntRange) =
        delegated.serialize(encoder, IntRangeDelegate(value.first, value.last))

    override fun deserialize(decoder: Decoder): IntRange =
        delegated.deserialize(decoder).run { startInclusive..endInclusive }

}