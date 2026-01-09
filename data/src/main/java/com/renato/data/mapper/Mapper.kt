package com.renato.data.mapper

/**
 * A generic interface for mapping objects from the data layer to the domain layer.
 *
 * This abstraction helps decouple external data representations (e.g., API responses or
 * database entities) from the business models by transforming them into domain-specific types.
 *
 * @param FROM The type coming from the data layer (e.g., DTO, Entity).
 * @param TO The type used in the domain layer (e.g., Domain model).
 */
interface Mapper<FROM, TO> {
    /**
     * Maps an object of type [FROM] data layer.
     * into an object of type [TO] domain layer.
     *
     * @param from The input object from the data layer.
     * @return The mapped object in the domain model format.
     */
    fun map(from: FROM, currentPage: Int?): TO
}