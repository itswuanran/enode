package org.enodeframework.infrastructure.impl

/**
 * @author anruence@gmail.com
 */
class ManyType(types: List<Class<*>>) {
    val types: List<Class<*>>

    override fun hashCode(): Int {
        return types.map { obj: Class<*> -> obj.hashCode() }.reduce { x: Int, y: Int -> x xor y }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other == null) {
            return false
        }
        if (other !is ManyType) {
            return false
        }
        return if (types.size != other.types.size) {
            false
        } else types.any { type: Class<*> ->
            other.types.any { x: Class<*> -> x == type }
        } && other.types.all { type: Class<*> -> types.any { x: Class<*> -> x == type } }
    }

    init {
        require(HashSet(types).size == types.size) {
            String.format("Invalid ManyType: %s", types.joinToString("|") { obj: Class<*> -> obj.name })
        }
        this.types = types
    }
}