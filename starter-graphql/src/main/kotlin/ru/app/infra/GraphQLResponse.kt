package ru.app.infra

import ru.app.api.UserInfo

data class GraphQLResponse(
    val data: UsersData?
){
//    companion object {
//        fun create(data: EmployeesData): GraphQLResponse = GraphQLResponse(data)
//    }
}

data class UsersData(
    val users: List<UserGql>?
)

data class UserGql(
    val id: String,
    val lastName: String,
    val middleName: String?,
    val firstName: String,
    val displayName: String,
    val email: String,
    val login: String
) {
    fun toDto() = UserInfo(
        id = id,
        lastName = lastName,
        middleName = middleName,
        firstName = firstName,
        displayName = displayName,
        email = email,
        login = login
    )
}