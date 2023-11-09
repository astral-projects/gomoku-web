package gomoku.http.controllers

/**
 * Annotation that marks a handler method as requiring authentication to be accessed.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class RequiresAuthentication
