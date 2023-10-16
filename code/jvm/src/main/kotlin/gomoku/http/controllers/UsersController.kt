package gomoku.http.controllers

import gomoku.domain.user.AuthenticatedUser
import gomoku.domain.user.User
import gomoku.domain.user.UserRankInfo
import gomoku.http.Uris
import gomoku.http.model.Problem
import gomoku.http.model.token.UserTokenCreateOutputModel
import gomoku.http.model.user.UserCreateInputModel
import gomoku.http.model.user.UserCreateTokenInputModel
import gomoku.http.model.user.UserHomeOutputModel
import gomoku.services.user.TokenCreationError
import gomoku.services.user.UserCreationError
import gomoku.services.user.UsersService
import gomoku.utils.Failure
import gomoku.utils.Success
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UsersController(
    private val userService: UsersService
) {

    @PostMapping(Uris.Users.REGISTER)
    fun create(@RequestBody input: UserCreateInputModel): ResponseEntity<*> {
        logger.info("POST ${Uris.Users.REGISTER}")
        val res = userService.createUser(input.username, input.email, input.password)
        return when (res) {
            is Success -> ResponseEntity.status(201)
                .header(
                    "Location",
                    Uris.Users.byId(res.value).toASCIIString()
                ).body("User created With Success. Welcome!")

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
        logger.info("POST ${Uris.Users.TOKEN}")
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
    fun logout(user: AuthenticatedUser) {
        logger.info("POST ${Uris.Users.LOGOUT}")
        userService.revokeToken(user.token)
    }

    @GetMapping(Uris.Users.HOME)
    fun getUserHome(userAuthenticatedUser: AuthenticatedUser): UserHomeOutputModel {
        logger.info("GET ${Uris.Users.HOME}")
        return UserHomeOutputModel(
            id = userAuthenticatedUser.user.id,
            username = userAuthenticatedUser.user.username
        )
    }

    @GetMapping(Uris.Users.GET_BY_ID)
    fun getById(@PathVariable id: String) {
        logger.info("GET ${Uris.Users.GET_BY_ID}")
        TODO("TODO")
    }

    @GetMapping(Uris.Users.RANKING)
    fun getUserRanking(): List<UserRankInfo> {
        logger.info("GET ${Uris.Users.RANKING}")
        TODO("Not yet implemented")
    }

    @PutMapping(Uris.Users.EDIT_USER_PROFILE)
    fun editUser(user: User): User {
        logger.info("PUT ${Uris.Users.EDIT_USER_PROFILE}")
        TODO("Not yet implemented")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UsersController::class.java)
    }
}
