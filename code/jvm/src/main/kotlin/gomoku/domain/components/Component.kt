package gomoku.domain.components

/**
 * Marker interface for domain components. Domain components are the building blocks of domain objects
 * that require validation.
 * Typically, they are classes with private constructors and a companion object that
 * provides a factory method for creating instances of the class using the **invoke** operator.
 */
interface Component
