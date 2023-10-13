package gomoku.http

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import gomoku.domain.AuthenticatedUser
import gomoku.http.model.Problem
import gomoku.http.model.UserCreateInputModel
import gomoku.http.model.UserCreateTokenInputModel
import gomoku.http.model.UserHomeOutputModel
import gomoku.http.model.UserTokenCreateOutputModel
import gomoku.services.TokenCreationError
import gomoku.services.UserCreationError
import gomoku.services.UsersService
import gomoku.utils.Failure
import gomoku.utils.Success

@RestController
class UsersController(
    private val userService: UsersService
) {

    @PostMapping(Uris.Users.CREATE)
    fun create(@RequestBody input: UserCreateInputModel): ResponseEntity<*> {
        val res = userService.createUser(input.username, input.password)
        return when (res) {
            is Success -> ResponseEntity.status(201)
                .header(
                    "Location",
                    Uris.Users.byId(res.value).toASCIIString()
                ).build<Unit>()

            is Failure -> when (res.value) {
                UserCreationError.InsecurePassword -> Problem.response(400, Problem.insecurePassword)
                UserCreationError.UserAlreadyExists -> Problem.response(400, Problem.userAlreadyExists)
            }
        }
    }

    @PostMapping(Uris.Users.TOKEN)
    fun token(
        @RequestBody input: UserCreateTokenInputModel
    ): ResponseEntity<*> {
        val res = userService.createToken(input.username, input.password)
        return when (res) {
            is Success ->
                ResponseEntity.status(200)
                    .body(UserTokenCreateOutputModel(res.value.tokenValue))

            is Failure -> when (res.value) {
                TokenCreationError.UserOrPasswordAreInvalid ->
                    Problem.response(400, Problem.userOrPasswordAreInvalid)
            }
        }
    }

    @PostMapping(Uris.Users.LOGOUT)
    fun logout(
        user: AuthenticatedUser
    ) {
        userService.revokeToken(user.token)
    }

    @GetMapping(Uris.Users.GET_BY_ID)
    fun getById(@PathVariable id: String) {
        TODO("TODO")
    }

    @GetMapping(Uris.Users.HOME)
    fun getUserHome(userAuthenticatedUser: AuthenticatedUser): UserHomeOutputModel {
        return UserHomeOutputModel(
            id = userAuthenticatedUser.user.id,
            username = userAuthenticatedUser.user.username
        )
    }
}
