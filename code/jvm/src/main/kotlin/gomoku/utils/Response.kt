package gomoku.utils

import org.springframework.web.bind.annotation.RequestBody
import java.lang.Exception
import java.net.http.HttpResponse

data class Response (val status : Int,val body: String = "",val reasonException: String="")